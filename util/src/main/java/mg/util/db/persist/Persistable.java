package mg.util.db.persist;

import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;
import static mg.util.validation.rule.ValidationRule.CONNECTION_NOT_CLOSED;
import static mg.util.validation.rule.ValidationRule.CONTAINS_FIELD;
import static mg.util.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.util.validation.rule.ValidationRule.FIELD_TYPE_MATCHES;
import static mg.util.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mg.util.db.persist.annotation.IntermediateOperation;
import mg.util.db.persist.annotation.TerminalOperation;
import mg.util.db.persist.constraint.AndConstraintBuilder;
import mg.util.db.persist.constraint.BetweenConstraintBuilder;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.constraint.DateBeforeConstraintBuilder;
import mg.util.db.persist.constraint.DateLaterConstraintBuilder;
import mg.util.db.persist.constraint.DecimalEqualsBuilder;
import mg.util.db.persist.constraint.GroupConstraintBuilder;
import mg.util.db.persist.constraint.IsStringConstraintBuilder;
import mg.util.db.persist.constraint.LikeStringConstraintBuilder;
import mg.util.db.persist.constraint.OrConstraintBuilder;
import mg.util.db.persist.constraint.OrderByBuilder;
import mg.util.db.persist.constraint.OrderByBuilder.Direction;
import mg.util.validation.Validator;

/**
 * <b>Note: any class extending this needs to declare parameterless constructor: it is needed by instantiation.</b><br><br>
 * Offers DSL for simple SQL queries construction and delegates for db access.
 * Used with DB and SqlBuilder.<br><br>
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
 * p.field("name").is("john");
 *
 * Contact contact = new Contact()
 * contact.setId(1L);
 * contact = p.findById();
 * </pre>
 *
 * All terminal operations implicitly use the a conjuction operator. Change the operator via
 * and() or or() methods. Using multiple terminal operators consecutively will use the same
 * operator between each terminal operator.
 *
 * <pre>
 * p.field("x").is("y")
 *  .field("q").is("r");
 *
 * is synonymous with
 *
 * p.field("x").is("y")
 *  .and()
 *  .field("q").is("r");
 * </pre>
 */
public abstract class Persistable {

    private static final ConstraintBuilder OR = new OrConstraintBuilder("orBuilder");
    private static final ConstraintBuilder AND = new AndConstraintBuilder("andBuilder");
    private List<ConstraintBuilder> constraints = new ArrayList<>();
    private List<OrderByBuilder> orderings = new ArrayList<>();
    private List<ConstraintBuilder> groupConstraints = new ArrayList<>();
    private ConjuctionOperator conjuctionOperator = ConjuctionOperator.AND;
    private boolean fetched = false;
    private String fieldName = "";
    private Connection connection;
    private DB db;
    private long id;

    public Persistable() {
    }

    public Persistable(Connection connection) {
        this.connection = connection;
        db = new DB(connection);
    }

    @TerminalOperation
    public Persistable after(LocalDateTime localDateTime) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("localDateTime", localDateTime, NOT_NULL, DATE_EARLIER.than(LocalDateTime.now()), FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new DateLaterConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    @IntermediateOperation
    public Persistable and() {
        conjuctionOperator = ConjuctionOperator.AND;
        return this;
    }

    @TerminalOperation
    public Persistable before(LocalDateTime localDateTime) {
        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("localDateTime", localDateTime, NOT_NULL, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new DateBeforeConstraintBuilder(fieldName, localDateTime));
        return this;
    }

    @TerminalOperation
    public Persistable between(LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("lowerConstraint", lowerConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .add("upperConstraint", upperConstraint, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new BetweenConstraintBuilder(fieldName, lowerConstraint, upperConstraint));
        return this;
    }

    @IntermediateOperation
    public Persistable clearConstraints() {
        constraints.clear();
        groupConstraints.clear();
        return this;
    }

    public void createTable() throws SQLException, DBValidityException {
        validateConnection();
        db.createTable(this);
    }

    public void dropTable() throws SQLException, DBValidityException {
        validateConnection();
        db.dropTable(this);
    }

    /**
     * Starts Constraint building by setting the the 'name' named field as the current
     * constraint.
     * @param name The field to use for the follow-up command.
     * @return the Persistable for method call chaining.
     */
    @IntermediateOperation
    public Persistable field(String fieldName) {
        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING, CONTAINS_FIELD.inType(this))
                 .validate();

        this.fieldName = fieldName;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> T find() throws SQLException, DBValidityException, DBMappingException {
        validateConnection();
        return db.findBy((T) this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> List<T> findAll() throws SQLException, DBValidityException, DBMappingException {
        validateConnection();
        return db.findAllBy((T) this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> List<T> findAllBy(String sql) throws SQLException, DBValidityException, DBMappingException {
        validateConnection();
        return db.findAllBy((T) this, sql);
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> T findBy(String sql) throws SQLException, DBValidityException, DBMappingException {
        validateConnection();
        return db.findBy((T) this, sql);
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> T findById() throws SQLException, DBValidityException, DBMappingException {
        validateConnection();
        return db.findById((T) this);
    }

    /**
     * Has a side effect of returning all constraints specified.
     * Returns the group and single constraints. Groups are returned in order of creation and then finally the
     * ungrouped constraints as the last.
     */
    public List<ConstraintBuilder> getConstraints() {

        if (hasContent(groupConstraints)) {

            List<ConstraintBuilder> allConstraints = new ArrayList<>();
            allConstraints.addAll(groupConstraints);
            allConstraints.addAll(constraints);
            return allConstraints;

        } else {
            return constraints;
        }
    }

    /**
     * Returns the name of the field this persistable constraint building points to.
     * Default is an empty string.
     * @return The fieldName this Persistable currently points to.
     */
    public String getFieldName() {
        return fieldName;
    }

    public long getId() {
        return id;
    }

    public List<OrderByBuilder> getOrderings() {
        return orderings;
    }

    @IntermediateOperation
    public Persistable group() {

        List<ConstraintBuilder> groupedConstraints = new ArrayList<>();
        groupedConstraints.addAll(constraints);
        GroupConstraintBuilder groupConstraintBuilder = new GroupConstraintBuilder(groupedConstraints);
        groupConstraints.add(groupConstraintBuilder);
        constraints.clear();
        return this;
    }

    @TerminalOperation
    public Persistable is(int constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NEGATIVE_OR_ZERO, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new DecimalEqualsBuilder(fieldName, constraint));
        return this;
    }

    @TerminalOperation
    public Persistable is(long constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NEGATIVE_OR_ZERO, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new DecimalEqualsBuilder(fieldName, constraint));
        return this;
    }

    @TerminalOperation
    public Persistable is(String constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NULL_OR_EMPTY_STRING, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

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
     */
    @TerminalOperation
    public Persistable like(String constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NULL_OR_EMPTY_STRING, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        conditionallyAddConjunctionConstraint();

        constraints.add(new LikeStringConstraintBuilder(fieldName, constraint));
        return this;
    }

    /**
     * Always changes the conjunction operator to OR. If used with groupConstraints, adds a
     * OrConstraintBuilder to the list of groupConstraints if there were any constraints present.
     */
    @IntermediateOperation
    public Persistable or() {
        conjuctionOperator = ConjuctionOperator.OR;
        return this;
    }

    public void orderByAscending() {

        validateNotNullOrEmpty("fieldName", fieldName);

        orderings.add(new OrderByBuilder(fieldName, Direction.ASC));
    }

    public void orderByDescending() {

        validateNotNullOrEmpty("fieldName", fieldName);

        orderings.add(new OrderByBuilder(fieldName, Direction.DESC));
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> void remove() throws SQLException, DBValidityException {
        validateConnection();
        db.remove((T) this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Persistable> void save() throws SQLException, DBValidityException {
        validateConnection();
        db.save((T) this);
    }

    public Persistable setConnectionAndDB(Connection connection) throws IllegalArgumentException, SQLException {
        validateNotNull("connection", connection);
        this.connection = connection;
        this.db = new DB(connection);
        return this;
    }

    /**
     * Clears constraints, groupConstraints and sets constraints or groupConstraints based what was provided by the
     * function.
     * @throws Exception the provider is allowed to throw an Exception if any applicable.
     */
    public void setConstraints(Consumer<Persistable> constraintProvider) throws Exception {
        validateNotNull("constraintProvider", constraintProvider);
        constraintProvider.accept(this);
    }

    /**
     * Sets the fetched status of this Persistable.
     *
     * @param b true to indicate this object was fetched via DB.
     */
    public void setFetched(boolean b) {
        this.fetched = b;
    }

    public void setId(long id) { // TOIMPROVE: enable builder pattern over all persistables: persistable.setId(2).setSomethingElse("val");
        this.id = id;
    }

    private void conditionallyAddConjunctionConstraint() {

        if (constraints.size() > 0) {

            constraints.add(getConjunctionOperator());

        } else if (constraints.size() == 0 &&
                   groupConstraints.size() > 0) {

            groupConstraints.add(getConjunctionOperator());
        }
    }

    private ConstraintBuilder getConjunctionOperator() {
        return (conjuctionOperator == ConjuctionOperator.OR) ? OR : AND;
    }

    private void validateConnection() {
        Validator.of("connection", connection, NOT_NULL, CONNECTION_NOT_CLOSED).validate();
    }

    private static enum ConjuctionOperator {
        OR, AND
    }
}
