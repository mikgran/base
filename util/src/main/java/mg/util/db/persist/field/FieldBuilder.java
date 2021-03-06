package mg.util.db.persist.field;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.Common.isInterchangeable;
import static mg.util.validation.Validator.validateNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mg.util.db.persist.Persistable;

// TOCONSIDER: change all the abstract is*** methods to use int masking FLAG?
public abstract class FieldBuilder {

    protected Field declaredField;
    protected String length = "";
    protected Logger logger = LogManager.getLogger(this.getClass());
    protected String name = "";
    protected boolean notNull = true;
    protected Persistable parentObject;

    public FieldBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        this.parentObject = validateNotNull("parentObject", parentObject);
        this.declaredField = validateNotNull("declaredField", declaredField);

        name = declaredField.getName();
    }

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
     * Builds this Objects foreign key SQL part. By default an empty String is returned.
     *
     * @return the String representing a SQL portion for foreign key for this field.
     */
    public String buildForeignKey() {
        return "";
    }

    public Field getDeclaredField() {
        return declaredField;
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
    // TOIMPROVE: sync the call signature of both getFieldValue and setFieldValue
    public Object getFieldValue(Object parentObject) {
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

    /**
     * Returns the name of the field reflected by this builder.
     * @return the name of the field.
     */
    public String getName() {
        return name;
    }

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
     * buildForeignKey() will return a non null and non empty String.
     */
    public abstract boolean isForeignKeyField();

    /**
     * States if the builder contains a id type element which is not a PRIMARY KEY
     * but part of composite key element. I.e. PRIMARY KEY (pk, id).
     *
     * @return Returns true if the implementing field is an id field for the
     *  reflected type.
     */
    public abstract boolean isIdField();

    /**
     * States if the field builder contains a one to many type element.
     *
     * @return Returns true if the field is wrapping a one to many field.
     */
    public abstract boolean isOneToManyField();

    /**
     * States if the field builder contains a one to one type element.
     *
     * @return Returns true if the field is wrapping a one to one field.
     */
    public abstract boolean isOneToOneField();

    /**
     * States if the builder contains the primary key (id) type element. Id fields
     * that are not primary key should return false;
     *
     * @return Returns true if the implementing field is the primary key for the
     * reflected type.
     */
    public abstract boolean isPrimaryKeyField();

    /**
     * Attempts to set a value for declared field by setting accessibility to
     * true.
     * @param parentObject the reflection target object the field belongs to.
     * @param value The new value for the field.
     */
    public void setFieldValue(Object parentObject, Object value) {
        try {
            Class<?> fieldType = declaredField.getType();

            // TOCONSIDER: remove all guards and let the mismatches that detonate into the catch block.
            if (value != null && parentObject != null &&
                (compareTypeToParentType(parentObject) ||

                 (fieldType.isAssignableFrom(value.getClass()) ||
                  fieldType.isPrimitive() && isInterchangeable(value, fieldType)))) {

                declaredField.setAccessible(true);
                declaredField.set(parentObject, value);
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("declaredField.set(%s:%s, field(named %s, type %s) value(%s, type %s) failed with:\n%s",
                                parentObject.getClass(),
                                parentObject,
                                declaredField.getName(),
                                declaredField.getType(),
                                value,
                                value.getClass(),
                                e.getMessage()));
        }
    }

    @Override
    public String toString() {
        return format("%s(name: %s, field sql: %s)", getClass().getSimpleName(), name, build());
    }

    protected boolean compareTypeToParentType(Object refParentObject) {
        return this.parentObject.getClass().equals(refParentObject.getClass());
    }

    // TOCONSIDER: perhaps allow mixing types int <-> long?
    protected void disallowIntFieldType(Field declaredField) {
        if (declaredField != null &&
            declaredField.getType() == Integer.TYPE ||
            declaredField.getType() == Integer.class) {
            throw new IllegalArgumentException("The field may not be of type int. Use long instead" +
                                               ", Class name: " + declaredField.getDeclaringClass().getName() +
                                               ", Field type: " + declaredField.getType() +
                                               ", Field name: " + declaredField.getName());
        }
    }

    protected String validateContent(String value, String noContentMessage) {
        if (!hasContent(value)) {
            throw new IllegalArgumentException(noContentMessage != null ? noContentMessage : "");
        }

        return value;
    }
}
