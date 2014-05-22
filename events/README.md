#### Events - a workhours and vehicle events tracking program.

##### Technologies and methods used in this project:
eclipse, jdk1.7, cygwin, csv parsing, maven dependencies, separation of concerns: services, util classes, business classes, 
iterative state machine: events, generic extensible and reusable java code, joda time, try to avoid magic numbers: prefer 
constants, dry, atomic classes, inheritance and abstractions: reusability, prefer exceptions to return codes, refactoring, 
prefer self documenting code: meaningful method (and class) naming over heavy javadoc, minimal side effects per 
class or method, avoiding misleading and intention hiding classes and methods.


##### Installation instructions:

events$ mvn clean package

Running: events$ java -cp target/app-report-1.0.jar mg.tracking.event.Report

Or directly:
java -cp target/app-report-1.0.jar mg.tracking.event.Report -v 01.01.2013 00:00 02.01.2013 00:00 2
java -cp target/app-report-1.0.jar mg.tracking.event.Report -w 01.01.2013 00:00 02.01.2013 00:00 A

Or in eclipse as a run target configuration:
-w 01.01.2013 00:00 02.01.2013 00:00 A

Notes for cygwin users:
Setting the classpath and running - workaround for the semicolon and colon issue:
export CLASSPATH=.:target/app-report-1.0.jar
export CLASSPATH=\` cygpath --path --windows "$CLASSPATH" \`
java -cp "$CLASSPATH" mg.tracking.event.Report


