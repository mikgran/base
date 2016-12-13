package mg.util.db.persist.constraint;

public class DecimalEqualsBuilder extends ConstraintBuilder {

    private long constraint = 0;

    public DecimalEqualsBuilder(String fieldName, int i) {
        super(fieldName);
        this.constraint = i;
    }

    public DecimalEqualsBuilder(String fieldName, long i) {
        super(fieldName);
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
