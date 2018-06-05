module mg.util {
    requires transitive java.sql;
    requires transitive junit;

    requires net.bytebuddy;
    requires java.ws.rs;
    requires log4j;
    requires joda.time;
    requires commons.dbcp;
    requires jmock;
    requires jmock.junit4;
    requires org.slf4j;

    // remember to clean build as well after changes.
    exports mg.util;
    exports mg.util.db;
    exports mg.util.db.persist;
    exports mg.util.db.persist.annotation;
    exports mg.util.db.persist.constraint;
    exports mg.util.db.persist.field;
    exports mg.util.rest;
    exports mg.util.validation;
    exports mg.util.validation.rule;
    exports mg.util.functional.classmatching;
    exports mg.util.functional.consumer;
    exports mg.util.functional.function;
    exports mg.util.functional.option;
    exports mg.util.functional.predicate;
    exports mg.util.functional.supplier;
}