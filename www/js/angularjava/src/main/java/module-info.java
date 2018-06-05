module mg.angularjava {

	requires jackson.annotations;
	requires jackson.core;
	requires jackson.databind;
	requires jersey.common;
	requires jersey.server;
	requires junit;
	requires log4j;

	requires java.sql;
 	requires java.ws.rs;

	requires org.slf4j;

	exports mg.angular.db;
	exports mg.angular.service;
	exports mg.angular.rest;

	requires mg.util;

}