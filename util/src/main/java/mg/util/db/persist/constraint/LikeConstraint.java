package mg.util.db.persist.constraint;

public class LikeConstraint extends Constraint {

    private String constraint;

    public LikeConstraint(String fieldName, String constraint) {       
        super(fieldName);
        this.constraint = constraint;
    }

    @Override
    public String get() {
        return new StringBuilder().append(fieldName)
                                  .append(" LIKE '")
                                  .append(constraint)
                                  .append("'")
                                  .toString();
    }
}

