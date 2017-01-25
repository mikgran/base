package mg.util.db.persist.constraint;

public class AndConstraintBuilder extends ConstraintBuilder {

    public AndConstraintBuilder(String fieldName) {
        super(fieldName);
    }

    @Override
    public String build() {
        return "AND";
    }

}
