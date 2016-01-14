package mg.util.db.persist;

public class FieldReference {

    public final String referredField;
    public final String referredTable;
    public final String referringField;
    public final String referringTable;

    public FieldReference(String referredTable, String referredField, String referringTable, String referringField) {

        super();

        this.referredTable = referredTable;
        this.referredField = referredField;
        this.referringTable = referringTable;
        this.referringField = referringField;
    }
}
