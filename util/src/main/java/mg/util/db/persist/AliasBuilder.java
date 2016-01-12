package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.LinkedHashMap;
import java.util.Map;

public class AliasBuilder {

    Map<String, String> aliases = new LinkedHashMap<>();

    /*
     * Map<family, members>
     */


    public String aliasTable(String tableName) {

        validateNotNullOrEmpty("tableName", tableName);

        // starting letter
        // find out current highest index
        // increment and combine

        String firstChar = String.valueOf(tableName.charAt(0));



        return "";
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void clear() {
        aliases.clear();
    }

    public int aliasCount() {
        return aliases.size();
    }

    public String getAlias(String key) {
        return aliases.get(key);
    }

}
