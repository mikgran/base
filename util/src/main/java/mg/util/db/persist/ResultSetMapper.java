package mg.util.db.persist;

import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.VarChar;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.validation.Validator;

public class ResultSetMapper<T extends Persistable> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private T t;

    /**
     * Constructs the ResultSetMapper.
     * @param t The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T t) {
        Validator.of("type", t, NOT_NULL).validate();
        this.t = t;
    }

    /**
     * Maps resultSet first row to a {@code <T extends Persistable>}. 
     * Any remaining rows in the ResultSet are ignored. If the ResultSet contains
     * multiple rows, the first row matching type T will be mapped and returned.
     * <br><br>
     * This method expects a resultset with all the columns. Use
     * partialMap to partially map the columns in the ResultSet. Any mismatch between 
     * selected columns and the type T object will result in ResultSetMapperException. 
     * 
     * @param resultSet the ResultSet containing at least one row of data corresponding the type T Persistable.
     * @return a type T object created by retrieving the data from the provided resultSet.
     * @throws SQLException on any database related exception.
     * @throws ResultSetMapperException If mapping fails or the resultSet was closed. 
     */
    public T mapOne(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        Validator.of("resultSet", resultSet, NOT_NULL)
                 .validate();

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }

        // reflect the fields and map them to the named fields. alternatively: get the meta data and find out the indexes for the named fields and then use ints.
        T t = newInstance();

        List<FieldBuilder> fieldBuilders = getFieldBuilders();

        if (!resultSet.next()) {
            throw new ResultSetMapperException("ResultSet has to contain at least one row for mapping.");
        }

        // since the type Persistable only has setId(): reflect for the fields and set them

        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder>)fieldBuilder -> setField(t, fieldBuilder, resultSet));
        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }

        return t;
    }

    private void setField(T t, FieldBuilder fieldBuilder, ResultSet resultSet) throws SQLException, NoSuchFieldException, SecurityException {
        logger.debug(fieldBuilder.toString());

        // TOIMPROVE: expand type coverage: int, date, etc
        if (fieldBuilder instanceof VarChar) {
            String columnValue = resultSet.getString(fieldBuilder.getName());
            
            Field declaredField = t.getClass().getDeclaredField(fieldBuilder.getName());
            
            
            
            
            // TODO set the field accessible and it's value
        }

    }

    private List<FieldBuilder> getFieldBuilders() {

        return Arrays.stream(t.getClass().getDeclaredFields())
                     .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                     .filter(fieldBuilder -> fieldBuilder.isDbField())
                     .collect(Collectors.toList());
    }

    public T partialMap(ResultSet resultSet) {

        return null;
    }

    public List<T> map(ResultSet resultSet) {

        return null;
    }

    @SuppressWarnings("unchecked")
    private T newInstance() throws ResultSetMapperException {

        try {
            return (T) t.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResultSetMapperException("Exception in instantiating type T." + e.getMessage());
        }
    }

}
