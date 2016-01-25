package mg.util.db.persist;

import mg.util.db.persist.field.FieldBuilder;

public class FieldReference {

    public final Persistable referredType;
    public final FieldBuilder referredField;
    public final String referredTable;
    public final Persistable referringType;
    public final FieldBuilder referringField;
    public final String referringTable;

    public FieldReference(Persistable referredType,
        String referredTable,
        FieldBuilder referredField,
        Persistable referringType,
        String referringTable,
        FieldBuilder referringField) {

        super();
        this.referredType = referredType;
        this.referredTable = referredTable;
        this.referredField = referredField;
        this.referringType = referringType;
        this.referringTable = referringTable;
        this.referringField = referringField;
    }

    public boolean fieldValuesMatch() {
        return (referredField.getFieldValue(referredType) != null &&
                referringField.getFieldValue(referringType) != null &&
                referredField.getFieldValue(referredType).equals(referringField.getFieldValue(referringType)));

    }

    @Override
    public String toString() {
        return String.format("FieldReference('%s.%s': '%s', '%s.%s': '%s')",
                             referredTable,
                             referredField.getName(),
                             referredField.getFieldValue(referredType),
                             referringTable,
                             referringField.getName(),
                             referringField.getFieldValue(referringType));
    }
}
