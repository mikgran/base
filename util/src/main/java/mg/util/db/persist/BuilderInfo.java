package mg.util.db.persist;

import java.util.List;

import mg.util.db.persist.field.FieldBuilder;

public class BuilderInfo {

    public final List<FieldBuilder> fieldBuilders;
    public final List<FieldBuilder> foreignKeyBuilders;
    public final List<FieldBuilder> idBuilders;
    public final List<FieldBuilder> oneToManyBuilders;
    public final List<FieldBuilder> oneToOneBuilders;
    public final FieldBuilder primaryKeyBuilder;
    public final Class<?> refType;
    public final String tableName;

    public BuilderInfo(Class<?> refType,
        List<FieldBuilder> collectionBuilders,
        List<FieldBuilder> singleBuilders,
        List<FieldBuilder> fieldBuilders,
        List<FieldBuilder> foreignKeyBuilders,
        List<FieldBuilder> idBuilders,
        FieldBuilder primaryKeyBuilder,
        String tableName) {

        super();
        this.refType = refType;
        this.oneToManyBuilders = collectionBuilders;
        this.oneToOneBuilders = singleBuilders;
        this.fieldBuilders = fieldBuilders;
        this.foreignKeyBuilders = foreignKeyBuilders;
        this.idBuilders = idBuilders;
        this.primaryKeyBuilder = primaryKeyBuilder;
        this.tableName = tableName;
    }
}