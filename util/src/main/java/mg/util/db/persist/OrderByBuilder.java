package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;

public class OrderByBuilder {

    public enum Direction {
        ASC, DESC;
    }

    private String fieldName;
    private Direction direction;

    public OrderByBuilder(String fieldName) {
        this(fieldName, Direction.ASC);
    }

    public OrderByBuilder(String fieldName, Direction direction) {
        validateNotNull("fieldName", fieldName);
        this.fieldName = fieldName;
        this.direction = direction != null ? direction : Direction.ASC;
    }

    public String build() {
        return new StringBuffer().append(fieldName)
                                 .append(" ")
                                 .append(direction.toString())
                                 .toString();
    }

    public Direction getDirection() {
        return direction;
    }

    public String getFieldName() {
        return fieldName;
    }
}
