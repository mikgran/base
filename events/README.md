Installation instructions (requires mvn to be installed):

events$ mvn clean package

Running:

events$ java -cp target/app-report-1.0.jar mg.tracking.event.Report

Or directly:

java -cp target/app-report-1.0.jar mg.tracking.event.Report -v 01.01.2013 00:00 02.01.2013 00:00 2
java -cp target/app-report-1.0.jar mg.tracking.event.Report -w 01.01.2013 00:00 02.01.2013 00:00 A

Or in eclipse as a run target configuration:

-w 01.01.2013 00:00 02.01.2013 00:00 A

Notes for cygwin users:

Setting the classpath and running - workaround for the semicolon and colon issue:
export CLASSPATH=.:target/app-report-1.0.jar
export CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
java -cp "$CLASSPATH" mg.tracking.event.Report


