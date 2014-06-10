#### Reservation - a booking program.

##### Technologies and methods used in this project:

eclipse, jdk1.7, cygwin, mariadb, maven, dbcp connection pooling, relational databases, heavy jdoc, sql scripts, jetty, 
bower, npm, javascript, jquery, jquery-ui, smoothness theme, rest-jersey, fullCalendar, unit tests, refactoring, 
regexp, separation of concerns.

##### Installation and running instructions:

1. preparation: run all the scripts/sql.txt commands on a database.
2. starting the jetty server: mvn clean install jetty:run.
3. querying the jetty server and the rest api: 
	cygwin: curl -v "http://localhost:8080/api/reservations?start=1401094800000&end=1401098400000"