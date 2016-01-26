package mg.util.db.persist;

import java.util.List;

import mg.util.db.persist.field.FieldBuilder;

public class BuilderInfo {

    private List<FieldBuilder> collectionBuilders;
    private List<FieldBuilder> fieldBuilders;
    private List<FieldBuilder> foreignKeyBuilders;
    private List<FieldBuilder> idBuilders;
    private FieldBuilder primaryKeyBuilder;
    private Class<?> refType;
    private String tableName;

    public BuilderInfo(Class<?> refType,
        List<FieldBuilder> collectionBuilders,
        List<FieldBuilder> fieldBuilders,
        List<FieldBuilder> foreignKeyBuilders,
        List<FieldBuilder> idBuilders,
        FieldBuilder primaryKeyBuilder,
        String tableName) {

        super();
        this.refType = refType;
        this.collectionBuilders = collectionBuilders;
        this.fieldBuilders = fieldBuilders;
        this.foreignKeyBuilders = foreignKeyBuilders;
        this.idBuilders = idBuilders;
        this.primaryKeyBuilder = primaryKeyBuilder;
        this.tableName = tableName;
    }

    public List<FieldBuilder> getCollectionBuilders() {
        return collectionBuilders;
    }

    public List<FieldBuilder> getFieldBuilders() {
        return fieldBuilders;
    }
    public List<FieldBuilder> getForeignKeyBuilders() {
        return foreignKeyBuilders;
    }
    public List<FieldBuilder> getIdBuilders() {
        return idBuilders;
    }
    public FieldBuilder getPrimaryKeyBuilder() {
        return primaryKeyBuilder;
    }
    public Class<?> getRefType() {
        return refType;
    }
    public String getTableName() {
        return tableName;
    }
}