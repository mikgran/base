module mg.util {
    requires java.sql;
    requires log4j;
    requires joda.time;
    requires org.slf4j;
    requires net.bytebuddy;
    requires junit;
    requires commons.dbcp;
    requires java.ws.rs;

    // remember to clean build as well after changes.
    exports mg.util;
    exports mg.util.db;
    exports mg.util.validation;
    exports mg.util.validation.rule;
}