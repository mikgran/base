package mg.util.db.persist.constraint;

public class IsConstraint extends Constraint {

    private String constraint;

    public IsConstraint(String fieldName, String constraint) {       
        super(fieldName);
        this.constraint = constraint;
    }

    @Override
    public String get() {
        return new StringBuilder().append(fieldName)
                                  .append(" = '")
                                  .append(constraint)
                                  .append("'")
                                  .toString();
    }
}
