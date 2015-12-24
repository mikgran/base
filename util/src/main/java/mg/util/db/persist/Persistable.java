package mg.util.db.persist;

import static mg.util.validation.rule.ValidationRule.CONTAINS_FIELD;
import static mg.util.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.util.validation.rule.ValidationRule.FIELD_TYPE_MATCHES;
import static mg.util.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.constraint.BetweenConstraintBuilder;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.constraint.DateBeforeConstraintBuilder;
import mg.util.db.persist.constraint.DateLaterConstraintBuilder;
import mg.util.db.persist.constraint.DecimalConstraintBuilder;
import mg.util.db.persist.constraint.IsStringConstraintBuilder;
import mg.util.db.persist.constraint.LikeStringConstraintBuilder;
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

    protected List<ConstraintBuilder> constraints = new ArrayList<>();
    protected String fieldName = "";
    protected int id = 0;
    private boolean fetched = false;

    public Persistable after(LocalDateTime localDateTime) {

        Validator.of("localDateTime", localDateTime, NOT_NULL, DATE_EARLIER.than(LocalDateTime.now()), FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new DateLaterConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    public Persistable before(LocalDateTime localDateTime) {
        Validator.of("localDateTime", localDateTime, NOT_NULL, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new DateBeforeConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    public Persistable between(LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {

        Validator.of("lowerConstraint", lowerConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("upperConstraint", upperConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .validate();

        constraints.add(new BetweenConstraintBuilder(fieldName, lowerConstraint, upperConstraint));
        return this;
    }

    public Persistable clearConstraints() {
        constraints.clear();
        return this;
    }

    /**
     * Starts Constraint building by setting the the 'name' named field as the current
     * constraint.
     * @param name The field to use for the follow-up command.
     * @return the Persistable for method call chaining.
     */
    public Persistable field(String fieldName) {
        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING, CONTAINS_FIELD.inType(this))
                 .validate();

        this.fieldName = fieldName;
        return this;
    }

    public List<ConstraintBuilder> getConstraints() {
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

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NEGATIVE_OR_ZERO, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new DecimalConstraintBuilder(fieldName, constraint));
        return this;
    }

    public Persistable is(String constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NULL_OR_EMPTY_STRING, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new IsStringConstraintBuilder(fieldName, constraint));
        return this;
    }

    public boolean isFetched() {
        return fetched;
    }

    /**
     * Usage:<br>
     * Persistable p = new Contact();<br>
     * p.field("firstName")<br>
     *  .like("partOfNam%");<br><br>
     *
     * For wildcarding, use the percent mark(%). i.e. p.like("%rtOfName");
     * matches to any number of characters before rtOfName, in this case it
     * matches to 'partOfName'.
     *
     * @param constraint finisher constraint
     * @return
     */
    public Persistable like(String constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NULL_OR_EMPTY_STRING, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();
        constraints.add(new LikeStringConstraintBuilder(fieldName, constraint));
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
