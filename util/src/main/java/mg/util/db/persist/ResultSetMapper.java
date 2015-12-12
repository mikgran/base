package mg.util.db.persist;

import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.validation.Validator;

public class ResultSetMapper<T extends Persistable> {

    // private Logger logger = LoggerFactory.getLogger(this.getClass());
    private T t;

    /**
     * Constructs the ResultSetMapper.
     * @param t The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T t) {
        Validator.of("type", t, NOT_NULL).validate();
        this.t = t;
    }

    public static <T extends Persistable> ResultSetMapper<T> of(T t) {
        return new ResultSetMapper<T>(t);
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

        ThrowingConsumer<FieldBuilder> throwingFieldSetter = fieldBuilder -> {

            fieldBuilder.setFieldValue(resultSet.getObject(fieldBuilder.getName()));
        };

        Validator.of("resultSet", resultSet, NOT_NULL)
                 .validate();

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }

        T t = newInstance();

        List<FieldBuilder> fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                                 .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                                                 .filter(fieldBuilder1 -> fieldBuilder1.isDbField())
                                                 .collect(Collectors.toList());

        // TOIMPROVE: remove side effect and force resultSet.next() usage outside of this method.
        if (!resultSet.next()) {
            throw new ResultSetMapperException("ResultSet has to contain at least one row for mapping.");
        }

        try {
            fieldBuilders.forEach(throwingFieldSetter);

        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }

        t.setId(resultSet.getInt("id"));

        return t;
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
