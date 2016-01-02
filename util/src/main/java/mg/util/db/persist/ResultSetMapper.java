package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private T type;

    /**
     * Constructs the ResultSetMapper.
     * @param t The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T t) {
        this.type = validateNotNull("t", t);
    }

    public List<T> map(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        validateResultSet(resultSet);

        List<T> results = new ArrayList<T>();

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        while (resultSet.next()) {

            T t = buildNewInstanceFrom(resultSet);

            results.add(t);
        }

        return results;
    }

    /**
     * Maps resultSet first row to a {@code <T extends Persistable>}.
     * Any remaining rows in the ResultSet are ignored. If the ResultSet contains
     * multiple rows, the first row matching type T will be mapped and returned.
     * <br><br>
     * This method expects a ResultSet with all the columns. Use
     * partialMap to partially map the columns in the ResultSet. Any mismatch between
     * selected columns and the type T object will result in ResultSetMapperException.
     * <br><br>
     * The first item using default sort order is used unless another sorting method is
     * used.
     *
     * @param resultSet the ResultSet containing at least one row of data corresponding the type T Persistable.
     * @return a type T object created by retrieving the data from the provided resultSet. If
     * no rows are present in the resultSet, an empty object of type T is returned.
     * @throws SQLException on any database related exception.
     * @throws ResultSetMapperException If mapping fails or the resultSet was closed.
     */
    public T mapOne(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        validateResultSet(resultSet);

        T t = null;

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        if (resultSet.next()) {

            t = buildNewInstanceFrom(resultSet);

        } else {

            t = newInstance();
        }

        return t;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException();
    }

    private T buildNewInstanceFrom(ResultSet resultSet) throws ResultSetMapperException, SQLException {

        T t = newInstance();

        List<FieldBuilder> fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                                 .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                                                 .filter(fieldBuilder -> fieldBuilder.isDbField())
                                                 .collect(Collectors.toList());
        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder, Exception>) fieldBuilder -> {

                fieldBuilder.setFieldValue(resultSet.getObject(fieldBuilder.getName()));
            });

            // t.setId(resultSet.getInt("id"));
            t.setFetched(true);

        } catch (RuntimeException e) {
            throw new ResultSetMapperException(e.getCause());
        }

        return t;
    }

    @SuppressWarnings("unchecked")
    private T newInstance() throws ResultSetMapperException {

        try {
            return (T) type.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResultSetMapperException("Exception in instantiating type T." + e.getMessage());
        }
    }

    private void validateResultSet(ResultSet resultSet) throws SQLException, ResultSetMapperException {
        validateNotNull("resultSet", resultSet);

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }
    }

}
