package mg.util.db.persist.constraint;

public class LikeStringConstraintBuilder extends ConstraintBuilder {

    private String constraint;

    public LikeStringConstraintBuilder(String fieldName, String constraint) {
        super(fieldName);
        this.constraint = constraint;
    }

    @Override
    public String build() {
        return new StringBuilder().append(fieldName)
                                  .append(" LIKE '")
                                  .append(constraint)
                                  .append("'")
                                  .toString();
    }
}

