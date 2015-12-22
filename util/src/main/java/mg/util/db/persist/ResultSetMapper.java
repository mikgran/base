package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.NotYetImplementedException;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.functional.consumer.ThrowingConsumer;

public class ResultSetMapper<T extends Persistable> {

    public static <T extends Persistable> ResultSetMapper<T> of(T t) {
        return new ResultSetMapper<T>(t);
    }

    // private Logger logger = LoggerFactory.getLogger(this.getClass());
    private T t;

    /**
     * Constructs the ResultSetMapper.
     * @param t The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T t) {
        validateNotNull("t", t);
        this.t = t;
    }

    public List<T> map(ResultSet resultSet) {

        return null;
    }

    /**
     * Maps resultSet first row to a {@code <T extends Persistable>}.
     * Any remaining rows in the ResultSet are ignored. If the ResultSet contains
     * multiple rows, the first row matching type T will be mapped and returned.
     * <br><br>
     * This method expects a ResultSet with all the columns. Use
     * partialMap to partially map the columns in the ResultSet. Any mismatch between
     * selected columns and the type T object will result in ResultSetMapperException.
     *
     * @param resultSet the ResultSet containing at least one row of data corresponding the type T Persistable.
     * @return a type T object created by retrieving the data from the provided resultSet.
     * @throws SQLException on any database related exception.
     * @throws ResultSetMapperException If mapping fails or the resultSet was closed.
     */
    public T mapOne(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        validateNotNull("resultSet", resultSet);

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }

        T t = newInstance();

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        if (!resultSet.next()) {
            return t;
        }

        List<FieldBuilder> fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                                 .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                                                 .filter(fieldBuilder -> fieldBuilder.isDbField())
                                                 .collect(Collectors.toList());

        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder>) fieldBuilder -> {

                fieldBuilder.setFieldValue(resultSet.getObject(fieldBuilder.getName()));

            });

        } catch (RuntimeException e) {
            throw new ResultSetMapperException(e.getCause());
        }

        t.setId(resultSet.getInt("id"));

        return t;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException();
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
