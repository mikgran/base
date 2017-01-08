package mg.util.db.persist;

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

import mg.util.db.persist.OrderByBuilder.Direction;
import mg.util.db.persist.constraint.BetweenConstraintBuilder;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.constraint.DateBeforeConstraintBuilder;
import mg.util.db.persist.constraint.DateLaterConstraintBuilder;
import mg.util.db.persist.constraint.DecimalEqualsBuilder;
import mg.util.db.persist.constraint.IsStringConstraintBuilder;
import mg.util.db.persist.constraint.LikeStringConstraintBuilder;
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
 * p.field("name")
 *  .is("john");
 *
 * Contact contact = new Contact()
 * contact.setId(1L);
 * contact = p.findById();
 * </pre>
 */
public abstract class Persistable {

    // TOCONSIDER: move jsonExcludeFields to RestUtil
    private static String[] jsonExcludeFields = {"jsonExcludeFields", "constraints", "orderings", "fetched", "fieldName", "connection", "db"};
    private List<ConstraintBuilder> constraints = new ArrayList<>();
    private List<OrderByBuilder> orderings = new ArrayList<>();
    private boolean fetched = false;
    private String fieldName = "";
    private Connection connection;
    private DB db;

    public static String[] getJsonExcludeFields() {
        return jsonExcludeFields;
    }

    public Persistable() {
    }

    public Persistable(Connection connection) {
        this.connection = connection;
        db = new DB(connection);
    }

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

    public List<OrderByBuilder> getOrderings() {
        return orderings;
    }

    public Persistable is(int constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NEGATIVE_OR_ZERO, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new DecimalEqualsBuilder(fieldName, constraint));
        return this;
    }

    public Persistable is(long constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NEGATIVE_OR_ZERO, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();

        constraints.add(new DecimalEqualsBuilder(fieldName, constraint));
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
     */
    public Persistable like(String constraint) {

        Validator.of("fieldName", fieldName, NOT_NULL_OR_EMPTY_STRING)
                 .add("constraint", constraint, NOT_NULL_OR_EMPTY_STRING, FIELD_TYPE_MATCHES.inType(this, fieldName))
                 .validate();
        constraints.add(new LikeStringConstraintBuilder(fieldName, constraint));
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

    public void setConnectionAndDB(Connection connection) throws IllegalArgumentException, SQLException {
        validateNotNull("connection", connection);
        this.connection = connection;
        this.db = new DB(connection);
    }

    /**
     * Sets the fetched status of this Persistable.
     *
     * @param b true to indicate this object was fetched via DB.
     */
    public void setFetched(boolean b) {
        this.fetched = b;
    }

    private void validateConnection() {
        Validator.of("connection", connection, NOT_NULL, CONNECTION_NOT_CLOSED).validate();
    }
}
