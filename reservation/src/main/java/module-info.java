module mg.reservation {

    requires mg.util;

    requires java.sql;
    requires log4j;
    requires joda.time;
    requires org.slf4j;
    requires junit;
    requires commons.dbcp;
    requires java.xml.bind;

    requires jetty.server;
    requires jetty.servlet;

    requires javax.servlet.api;
    requires jersey.common;
    requires jersey.server;

}