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

/**
 * Offers DSL for simple SQL queries construction. Used with SqlBuilder.<br><br>
 *
 * Intermediate operations i.e: field("name")<br>
 * Terminal operations i.e: is("john"), like("joh")<br><br>
 *
 * Use preparing operation field("name"); first and finish the call chain
 * with for instance is("john");.<br><br>
 *
 * Example: <br>
 * The following will create constraint into the Persistable p for the field named "name", with an
 * IsStringConstraint(fieldName, "john");
 * <pre>
 * Persistable p = new Contact();
 * p.field("name")
 *  .is("john");
 * </pre>
 */
public abstract class Persistable {

    protected List<ConstraintBuilder> constraints = new ArrayList<>();
    protected String fieldName = "";
    private boolean fetched = false;

    public Persistable after(LocalDateTime localDateTime) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("localDateTime", localDateTime, NOT_NULL, DATE_EARLIER.than(LocalDateTime.now()), FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new DateLaterConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    public Persistable before(LocalDateTime localDateTime) {
        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("localDateTime", localDateTime, NOT_NULL, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new DateBeforeConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    public Persistable between(LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("lowerConstraint", lowerConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("upperConstraint", upperConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
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

    // TOIMPROVE: replace single id handling with multiple ID, and remove id, getId and setId methods from all Persistables
    // public abstract int getId();

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
     * Sets the fetched status of this Persistable.
     *
     * @param b true to indicate this object was fetched via DB.
     */
    public void setFetched(boolean b) {
        this.fetched = b;
    }

    // public abstract void setId(int id);
}
