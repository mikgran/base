package mg.util.db.persist.constraint;

public class OrConstraintBuilder extends ConstraintBuilder {

    public OrConstraintBuilder(String fieldName) {
        super(fieldName);
    }

    @Override
    public String build() {
        return "OR";
    }
}
