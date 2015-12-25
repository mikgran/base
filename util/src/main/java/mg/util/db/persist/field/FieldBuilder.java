package mg.util.db.persist.field;

import static java.lang.String.format;
import static mg.util.validation.Validator.validateNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldBuilder {

    protected Field declaredField;
    protected String length = "";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected String name = "";
    protected boolean notNull = true;
    protected Object parentObject;
    protected String sql = "";
    protected Object value = "";

    // provided just to ensure usage in subclasses
    public FieldBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        this.parentObject = validateNotNull("parentObject", parentObject);
        this.declaredField = validateNotNull("declaredField", declaredField);
    }

    // no access: force use of constructor with parameters
    @SuppressWarnings("unused")
    private FieldBuilder() {
    }

    /**
     * Builds this objects sql part. A string type field would produce 'name VARCHAR (20) NOT NULL' for instance.
     *
     * @return the string representing a sql portion describing this field.
     */
    public abstract String build();

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    /**
     * States if the field builder contains a Collection type element. Fields
     * that are collections should return true and false for SQL fields.
     *
     * @return Returns true if the field is wrapping a Collection otherwise
     *         false;
     */
    public abstract boolean isCollectionField();

    /**
     * States if the field builder is able to build a valid SQL field.
     * Collection fields and fields not buildable into field SQL parts should
     * return false.
     *
     * @return Returns true if the implementing field is a db field for
     *         generating SQL else returns false to indicate a non db field or a
     *         collection field
     */
    public abstract boolean isDbField();

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

}
