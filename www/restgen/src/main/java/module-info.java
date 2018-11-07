module mg.restgen {

    requires transitive java.sql;
    requires transitive junit;
    requires transitive mg.util;

    requires org.apache.logging.log4j;
	requires jackson.annotations;
	requires jackson.core;
	requires jackson.databind;

	requires java.ws.rs;
	requires jersey.common;
	requires jersey.server;
	requires org.apache.commons.lang3;

    exports mg.restgen.rest;
    exports mg.restgen.service;
    exports mg.restgen.db;
}