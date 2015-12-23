package mg.util.db.persist.constraint;

public abstract class Constraint {

    protected String fieldName;

    public Constraint(String fieldName) {
        this.fieldName = fieldName;
    }

    public abstract String get();

    public String getFieldName() {
        return fieldName;
    }
}
