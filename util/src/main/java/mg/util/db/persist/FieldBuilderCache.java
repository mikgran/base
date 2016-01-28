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

        Class<? extends Persistable> typeClass = type.getClass();
        BuilderInfo builderInfo = typeBuilders.get(typeClass);
        if (builderInfo == null) {

            String tableName = getTableNameAndValidate(type);
            List<FieldBuilder> allBuilders = getAllBuilders(type);
            List<FieldBuilder> fieldBuilders = getFieldBuildersAndValidate(allBuilders);
            List<FieldBuilder> idBuilders = getIdBuildersAndValidate(allBuilders);
            FieldBuilder primaryKeyBuilder = getPrimaryKeyBuilder(idBuilders);
            List<FieldBuilder> foreignKeyBuilders = getForeignKeyBuilders(allBuilders);
            List<FieldBuilder> oneToManyBuilders = getOneToManyBuilders(allBuilders);
            List<FieldBuilder> oneToOneBuilders = getOneToOneBuilders(allBuilders);

            builderInfo = new BuilderInfo(typeClass,
                                          oneToManyBuilders,
                                          oneToOneBuilders,
                                          fieldBuilders,
                                          foreignKeyBuilders,
                                          idBuilders,
                                          primaryKeyBuilder,
                                          tableName);

            typeBuilders.put(typeClass, builderInfo);
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

    private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> fieldBuilders = allBuilders.stream()
                                                      .filter(FieldBuilder::isDbField)
                                                      .collect(Collectors.toList());

        if (!hasContent(fieldBuilders)) {
            throw new DBValidityException("Type T has no field annotations.");
        }

        return fieldBuilders;
    }

    private <T extends Persistable> List<FieldBuilder> getForeignKeyBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(FieldBuilder::isForeignKeyField)
                          .collect(Collectors.toList());
    }

    private List<FieldBuilder> getIdBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> idBuilders = allBuilders.stream()
                                                   .filter(FieldBuilder::isIdField)
                                                   .collect(Collectors.toList());

        List<IdBuilder> ids = idBuilders.stream()
                                        .map(ib -> (IdBuilder) ib)
                                        .collect(Collectors.toList());

        long autoIncrementFieldCount = ids.stream().filter(id -> id.isAutoIncrement()).count();
        long primaryKeyFieldCount = ids.stream().filter(FieldBuilder::isPrimaryKeyField).count();

        if (autoIncrementFieldCount > 1) {
            throw new DBValidityException("Type T can not contain more than one id field with autoIncrement.");
        }

        if (primaryKeyFieldCount < 1) {
            throw new DBValidityException("Type T does not contain an expected primary key field.");
        }

        return idBuilders;
    }
    private <T extends Persistable> List<FieldBuilder> getOneToManyBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(FieldBuilder::isOneToManyField)
                          .collect(Collectors.toList());
    }

    private List<FieldBuilder> getOneToOneBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(FieldBuilder::isOneToOneField)
                          .collect(Collectors.toList());
    }

    private FieldBuilder getPrimaryKeyBuilder(List<FieldBuilder> idBuilders) {

        return idBuilders.stream()
                         .filter(FieldBuilder::isPrimaryKeyField)
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
