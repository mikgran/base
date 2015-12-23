package mg.util.db.persist.constraint;

public class IsStringConstraintBuilder extends ConstraintBuilder {

    private String constraint;

    public IsStringConstraintBuilder(String fieldName, String constraint) {       
        super(fieldName);
        this.constraint = constraint;
    }

    @Override
    public String build() {
        return new StringBuilder().append(fieldName)
                                  .append(" = '")
                                  .append(constraint)
                                  .append("'")
                                  .toString();
    }
}
