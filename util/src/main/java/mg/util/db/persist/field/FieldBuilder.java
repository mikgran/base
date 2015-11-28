package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldBuilder {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected boolean notNull = true;
    protected Object value = "";
    protected String length = "";
    protected String name = "";
    protected String sql = "";

    // no access: force use of constructor with parameters
    @SuppressWarnings("unused")
    private FieldBuilder() {
    }

    // provided just to ensure usage in subclasses
    public FieldBuilder(Object parentObject, Field declaredField, Annotation annotation) {
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
     *         retrieve the value.
     */
    protected Object getFieldValue(Object parentObject, Field declaredField) {
        try {
            declaredField.setAccessible(true);
            return declaredField.get(parentObject);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.error(format("Object Type %s, field named %s, declaredField.get(t) failed with:\n%s", parentObject.getClass(), declaredField.getName(), e.getMessage()));
        }
        return null;
    }

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

    public String getSql() {
        return sql;
    };

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return format("[name: %s, value: %s, field sql: %s]", name, value, sql);
    }
}
