package mg.util.db.persist;

import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.db.persist.field.IdBuilder;

public class FieldBuilderCache {

    private Map<Class<?>, BuilderInfo> typeBuilders = new HashMap<>();

    public synchronized <T extends Persistable> BuilderInfo buildersFor(T type) throws DBValidityException {

        validateNotNull("type", type);

        BuilderInfo builderInfo = typeBuilders.get(type.getClass());
        if (builderInfo == null) {

            String tableName = getTableNameAndValidate(type);
            List<FieldBuilder> allBuilders = getAllBuilders(type);
            List<FieldBuilder> fieldBuilders = getFieldBuildersAndValidate(allBuilders);
            List<FieldBuilder> idBuilders = getIdBuildersAndValidate(allBuilders);
            FieldBuilder primaryKeyBuilder = getPrimaryKeyBuilder(idBuilders);
            List<FieldBuilder> foreignKeyBuilders = getForeignKeyBuilders(allBuilders);
            List<FieldBuilder> collectionBuilders = getCollectionBuilders(allBuilders, type);

            builderInfo = new BuilderInfo(type.getClass(),
                                          collectionBuilders,
                                          fieldBuilders,
                                          foreignKeyBuilders,
                                          idBuilders,
                                          primaryKeyBuilder,
                                          tableName);

            typeBuilders.put(type.getClass(), builderInfo);
        }

        return builderInfo;
    }

    public Map<Class<?>, BuilderInfo> getBuilders() {
        return typeBuilders;
    }

    private <T extends Persistable> List<FieldBuilder> getAllBuilders(T t) {
        return Arrays.stream(t.getClass().getDeclaredFields())
                     .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                     .collect(Collectors.toList());
    }

    private <T extends Persistable> List<FieldBuilder> getCollectionBuilders(List<FieldBuilder> allBuilders, T t) {
        return allBuilders.stream()
                          .filter(fieldBuilder -> fieldBuilder.isCollectionField())
                          .collect(Collectors.toList());
    }

    private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> fieldBuilders = allBuilders.stream()
                                                      .filter(fieldBuilder -> fieldBuilder.isDbField())
                                                      .collect(Collectors.toList());

        if (!hasContent(fieldBuilders)) {
            throw new DBValidityException("Type T has no field annotations.");
        }

        return fieldBuilders;
    }
    private <T extends Persistable> List<FieldBuilder> getForeignKeyBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(fieldBuilder -> fieldBuilder.isForeignKeyField())
                          .collect(Collectors.toList());
    }

    private List<FieldBuilder> getIdBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> idBuilders = allBuilders.stream()
                                                   .filter(fb -> fb.isIdField())
                                                   .collect(Collectors.toList());

        List<IdBuilder> ids = idBuilders.stream()
                                        .map(ib -> (IdBuilder) ib)
                                        .collect(Collectors.toList());

        long autoIncrementFieldCount = ids.stream().filter(id -> id.isAutoIncrement()).count();
        long primaryKeyFieldCount = ids.stream().filter(id -> id.isPrimaryKeyField()).count();

        if (autoIncrementFieldCount > 1) {
            throw new DBValidityException("Type T can not contain more than one id field with autoIncrement.");
        }

        if (primaryKeyFieldCount < 1) {
            throw new DBValidityException("Type T does not contain an expected primary key field.");
        }

        return idBuilders;
    }

    private FieldBuilder getPrimaryKeyBuilder(List<FieldBuilder> idBuilders) {

        return idBuilders.stream()
                         .filter(idBuilder -> idBuilder.isPrimaryKeyField())
                         .findFirst()
                         .get();
    }

    private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

        Table tableAnnotation = t.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new DBValidityException("Type T has no @Table annotation.");
        }

        return tableAnnotation.name();
    }
}
