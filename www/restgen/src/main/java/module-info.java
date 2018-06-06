module mg.restgen {
	exports mg.restgen.rest;
	exports mg.restgen.service;
	exports mg.restgen.db;

	requires mg.util;

	requires org.apache.logging.log4j;

	requires jackson.annotations;
	requires jackson.core;
	requires jackson.databind;
	requires java.sql;
	requires java.ws.rs;
	requires jersey.common;
	requires jersey.server;
	requires junit;

	requires org.apache.commons.lang3;


}