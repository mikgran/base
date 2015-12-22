package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.rule.ValidationRule.CONTAINS_FIELD;
import static mg.util.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.util.validation.rule.ValidationRule.FIELD_TYPE_MATCHES;
import static mg.util.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.constraint.Constraint;
import mg.util.db.persist.constraint.DateBeforeConstraint;
import mg.util.db.persist.constraint.DateLaterConstraint;
import mg.util.db.persist.constraint.DecimalConstraint;
import mg.util.db.persist.constraint.IsConstraint;
import mg.util.db.persist.constraint.LikeConstraint;
import mg.util.validation.Validator;

/*
 * id, fieldName reference, constraints.
 * Offers DSL for simple SQL queries construction.
 *
 * Intermediate operation: field("name")
 * Terminal operations i.e: is("john"), like("joh")
 *
 * The field(String fieldName) operation prepares the Persistable to create a Constraint.
 *
 */
public abstract class Persistable {

    protected List<Constraint> constraints = new ArrayList<>();
    protected String fieldName = "";
    protected int id = 0;

    public Persistable after(LocalDateTime localDateTime) {

        Validator.of("localDateTime", localDateTime,
                     NOT_NULL,
                     DATE_EARLIER.than(LocalDateTime.now()))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new DateLaterConstraint(fieldName, localDateTime));
        return this;
    }

    public Persistable before(LocalDateTime localDateTime) {
        validateNotNull("localDateTime", localDateTime);

        constraints.add(new DateBeforeConstraint(fieldName, localDateTime));
        return this;
    }

    public void clearConstraints() {
        constraints.clear();
    }

    /**
     * Starts Constraint building by setting the the 'name' named field as the current
     * constraint.
     * @param name The field to use for the follow-up command.
     * @return the Persistable for method call chaining.
     */
    public Persistable field(String fieldName) {
        Validator.of("fieldName", fieldName,
                     NOT_NULL_OR_EMPTY_STRING,
                     CONTAINS_FIELD.inType(this))
                 .validate();

        this.fieldName = fieldName;
        return this;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Returns the name of the field this persistable constraint building points to.
     * Default is an empty string.
     * @return The fieldName this Persistable currently points to.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * The id of this data object.
     *
     * @return the id corresponding this records primary key.
     */
    public int getId() {
        return id;
    }

    public Persistable is(int constraint) {

        Validator.of("constraint", constraint,
                     NOT_NEGATIVE_OR_ZERO,
                     FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new DecimalConstraint(fieldName, constraint));
        return this;
    }

    public Persistable is(String constraint) {

        Validator.of("constraint", constraint,
                     NOT_NULL_OR_EMPTY_STRING,
                     FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new IsConstraint(fieldName, constraint));
        return this;
    }

    public Persistable like(String constraint) {

        Validator.of("constraint", constraint,
                     NOT_NULL_OR_EMPTY_STRING,
                     FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();
        constraints.add(new LikeConstraint(fieldName, constraint));
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
