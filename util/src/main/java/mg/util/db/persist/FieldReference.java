package mg.util.db.persist;

import mg.util.db.persist.field.FieldBuilder;

public class FieldReference {

    public final FieldBuilder referredField;
    public final String referredTable;
    public final FieldBuilder referringField;
    public final String referringTable;

    public FieldReference(String referredTable, FieldBuilder referredField, String referringTable, FieldBuilder referringField) {

        super();

        this.referredTable = referredTable;
        this.referredField = referredField;
        this.referringTable = referringTable;
        this.referringField = referringField;
    }
}
