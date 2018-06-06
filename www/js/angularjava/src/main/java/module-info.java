module mg.angularjava {

	requires mg.util;

	requires jackson.annotations;
	requires jackson.core;
	requires jackson.databind;
	requires jersey.common;
	requires jersey.server;

 	requires java.ws.rs;

 	requires org.apache.logging.log4j;

	exports mg.angular.db;
	exports mg.angular.service;
	exports mg.angular.rest;
}