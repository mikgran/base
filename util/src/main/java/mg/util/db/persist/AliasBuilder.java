package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MySQL/MariaDB behvior only!
 * Creates new aliases and caches the created aliases in a LinkedHashMap of LinkedHashMaps.
 *
 * The aliases are organized in the following manner: Map&lt;familyKey, Map&lt;memberKey, memberValue&gt;&gt;.
 * Typical contents of an AliasBuilder for example are:
<pre>
(
  (
    "c",
    (
      ("contacts", "c1"),
      ("consumers", "c2"),
      ("contracts", "c3")
    )
  ),
  (
    "d",
    (
      ("deliveries", "d1")
    )
  )
)
<pre>
 */
public class AliasBuilder {

    private Map<String, Map<String, String>> aliases = new LinkedHashMap<>();

    public int aliasCount() {

        return aliases.entrySet()
                      .stream()
                      .map(entry -> entry.getValue())
                      .map(entry -> entry.size())
                      .mapToInt(Integer::intValue)
                      .sum();
    }

    /**
     * Builds or gets an alias for a string. Any new aliases will be cached
     * within the builder. The builder uses the first character of the provided
     * String to create the alias. Any consecutive calls for the same starting character
     * will create a new alias with an incremented index, starting with one then two then
     * three, etc. I.e: aliasBuilder.aliasOf("caa") -&gt; "c1", aliasBuilder.aliasOf("cab")
     * -&gt; "c2", etc.
     *
     * @param string the String to create the alias for. Must be not null and not empty.
     * @return the existing or build alias for the string.
     */
    public String aliasOf(String string) {

        validateNotNullOrEmpty("tableName", string);

        String firstChar = firstCharacterOf(string);
        String alias = "";

        Map<String, String> familyMap = aliases.get(firstChar);

        if (familyMap != null) {

            String aliasCandidate = familyMap.get(string);
            if (aliasCandidate != null) {

                alias = aliasCandidate;

            } else {

                alias = buildAlias(firstChar, familyMap);
                familyMap.put(string, alias);
            }

        } else {

            Map<String, String> newFamilyMap = new LinkedHashMap<>();
            alias = buildAlias(firstChar, newFamilyMap);
            newFamilyMap.put(string, alias);
            aliases.put(firstChar, newFamilyMap);
        }
        return alias;
    }

    // Map<familyKey, LinkedHashMap<memberKey, member>>
    public void clear() {
        aliases.entrySet()
               .stream()
               .forEach(entry -> entry.getValue()
                                      .clear());
        aliases.clear();
    }

    // Map<familyKey, LinkedHashMap<memberKey, member>>
    public int familyCount() {
        return aliases.size();
    }

    private String buildAlias(String charAsString, Map<String, String> familyMap) {
        return charAsString + (familyMap.size() + 1);
    }

    private String firstCharacterOf(String string) {
        if (string != null && string.length() > 0) {
            return String.valueOf(string.charAt(0));
        }
        return "";
    }

    /**
     * Returns an alias for the provided key.
     * @param key The key to use in fetching the alias.
     * @return Returns the alias corresponding the key, or null if no matching alias is
     * found in the cache.
     */
    @SuppressWarnings("unused")
    private String getAlias(String key) {

        if (key != null && key.length() > 0) {

            Map<String, String> familyMap = aliases.get(firstCharacterOf(key));

            if (familyMap != null) {

                return familyMap.get(key);
            }
        }
        return null;
    }

}
