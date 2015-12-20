package mg.util.db.persist;

import static mg.util.validation.rule.ValidationRule.CONTAINS_FIELD;
import static mg.util.validation.rule.ValidationRule.FIELD_TYPE_MATCHES;
import static mg.util.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.util.HashMap;

import mg.util.db.persist.constraint.Constraint;
import mg.util.db.persist.constraint.DecimalConstraint;
import mg.util.db.persist.constraint.StringConstraint;
import mg.util.validation.Validator;

public abstract class Persistable {

    protected HashMap<String, Constraint> constraints = new HashMap<>();
    protected int id = 0;
    private String fieldName;

    /**
     * Starts Constraint building by setting the the 'name' named field as the current 
     * constraint.
     * @param name The field to use for the follow-up command.
     * @return 
     */
    public Persistable field(String fieldName) {
        Validator.of("fieldName", fieldName,
                     NOT_NULL_OR_EMPTY_STRING,
                     CONTAINS_FIELD.inType(this))
                 .validate();

        this.fieldName = fieldName;
        return this;
    }

    public HashMap<String, Constraint> getConstraints() {
        return constraints;
    }

    /**
     * The id of this data object. Any above zero ids mean that the object has
     * been loaded from the database.
     * 
     * @return the id corresponding this records primary key.
     */
    public int getId() {
        return id;
    }

    // finisher
    public Persistable is(int constraint) {

        Validator.of("constraint", constraint,
                     NOT_NEGATIVE_OR_ZERO,
                     FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.put(fieldName, new DecimalConstraint(constraint));
        fieldName = "";
        return this;
    }

    // finisher
    public Persistable is(String constraint) {

        Validator.of("constraint", constraint,
                     NOT_NULL_OR_EMPTY_STRING,
                     FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.put(fieldName, new StringConstraint(constraint));
        fieldName = "";
        return this;
    }

    /**
     * Sets the id of this record. Changing a loaded objects id causes another
     * record to be overridden via save().
     * 
     * @param id
     *            the new id for this object.
     */
    public void setId(int id) {
        this.id = id;
    }

}
