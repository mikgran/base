package mg.util.db.persist.constraint;

public abstract class ConstraintBuilder {

    protected String fieldName;

    public ConstraintBuilder(String fieldName) {
        this.fieldName = fieldName;
    }

    public abstract String build();

    public String getFieldName() {
        return fieldName;
    }
}
