package mg.util.db.persist.field;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.Persistable;

public abstract class FieldBuilder {

    protected Field declaredField;
    protected String length = "";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected String name = "";
    protected boolean notNull = true;
    protected Persistable parentObject;
    protected Object value = "";

    public FieldBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        this.parentObject = validateNotNull("parentObject", parentObject);
        this.declaredField = validateNotNull("declaredField", declaredField);

        name = declaredField.getName();
        value = getFieldValue(parentObject, declaredField);

        logger.debug("field value type: " + (value != null ? value.getClass().getSimpleName() : "<no type>"));
    }

    // no access: force use of constructor with parameters
    @SuppressWarnings("unused")
    private FieldBuilder() {
    }

    /**
     * Builds this objects sql part. A string type field would produce 'name VARCHAR (20) NOT NULL' for instance.
     *
     * @return the String representing a sql portion describing this field.
     */
    public abstract String build();

    /**
     * Builds this objects foreign key SQL part. By default an empty String is returned.
     *
     * @return the String representing a SQL portion for foreign key for this field.
     */
    public String buildForeignKey() {
        return "";
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    /**
     * States if the field builder contains a Collection type element.
     *
     * @return Returns true if the field is wrapping a Collection.
     */
    public abstract boolean isCollectionField();

    /**
     * States if the field builder is able to build a valid SQL field.
     *
     * @return Returns true if the implementing field is a db field.
     */
    public abstract boolean isDbField();

    /*
     * States if the builder contains a reference type element.
     *
     * @returns Returns true if the implementing field is a foreign key field and
     * buildForeignKey() will return a non empty String.
     */
    public abstract boolean isForeignKeyField();

    /**
     * States if the builder contains a id type element.
     * @return Returns true if the implementing field is an primary key field.
     */
    public abstract boolean isIdField();

    /**
     * Attempts to set a value for declared field by setting accessibility to
     * true.
     * @param value The new value for the field.
     * @throws Exception
     */
    public void setFieldValue(Object value) {
        try {
            if (value != null && declaredField.getDeclaringClass().equals(value.getClass())) {
                declaredField.setAccessible(true);
                declaredField.set(parentObject, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.set(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(),
                                e.getMessage()));
        }
    }

    @Override
    public String toString() {
        return format("[name: %s, value: %s, field sql: %s]", name, value, build());
    }

    /**
     * Attempts to get a value of a declared field by setting accessibility to
     * true.
     *
     * @param parentObject
     *            the class the field resides in.
     * @param declaredField
     *            the field to manipulate and the value to obtain.
     * @return the value of the field type Object else null if not able to
     *         retrieve the value.Exception
     */
    protected Object getFieldValue(Object parentObject, Field declaredField) {
        try {
            declaredField.setAccessible(true);
            return declaredField.get(parentObject);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.get(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(),
                                e.getMessage()));
        }
        return null;
    }

    protected String validateContent(String value, String noContentMessage) {
        if (!hasContent(value)) {
            throw new IllegalArgumentException(noContentMessage != null ? noContentMessage : "");
        }

        return value;
    }

}
