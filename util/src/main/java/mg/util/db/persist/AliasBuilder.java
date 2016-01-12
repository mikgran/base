package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates new aliases and caches the created aliases in a LinkedHashMap of LinkedHashMaps.
 *
 * The aliases are organized in the following manner: Map<familyKey, Map<memberKey, memberValue>.
 * The typical contents of an AliasBuilder for example:
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
     * Builds a new SQL alias for a string and caches the alias
     * internally. Uses the first character of the provided String
     * to create the alias.
     *
     * @param string the String to create the alias for.
     * Must be not null and non empty.
     * @return the new alias
     */
    public String aliasOf(String string) {

        validateNotNullOrEmpty("tableName", string);

        // starting letter
        // find out current highest index
        // increment and combine

        String firstChar = firstCharacterOf(string);
        String newAlias = "";

        Map<String, String> familyMap = aliases.get(firstChar);

        if (familyMap != null) {

            newAlias = buildAlias(firstChar, familyMap);
            familyMap.put(string, newAlias);

        } else {

            Map<String, String> newFamilyMap = new LinkedHashMap<>();
            newAlias = buildAlias(firstChar, newFamilyMap);
            newFamilyMap.put(string, newAlias);
            aliases.put(firstChar, newFamilyMap);
        }

        return newAlias;
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

    public String firstCharacterOf(String string) {
        if (string != null && string.length() > 0) {
            return String.valueOf(string.charAt(0));
        }
        return "";
    }

    /**
     * Returns an alias for the provided key.
     * @param key The key to use in fetching the alias.
     * @return Returns the alias corresponding the key, or null if no matching alias is found.
     */
    public String getAlias(String key) {

        if (key != null && key.length() > 0) {

            Map<String, String> familyMap = aliases.get(firstCharacterOf(key));

            if (familyMap != null) {

                return familyMap.get(key);
            }
        }
        return null;
    }

    //    public Map<String, String> getAliases(String familyKey) {
    //
    //        if (familyKey != null && familyKey.length() > 0) {
    //            return Collections.emptyMap();
    //        }
    //
    //        return Collections.emptyMap();
    //    }

    private String buildAlias(String charAsString, Map<String, String> familyMap) {
        return charAsString + (familyMap.size() + 1);
    }

}
