<!DOCTYPE html>
<html>
<head>
<title>Test links</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />

<script type='text/javascript' src='/js/jquery.js'></script>
<!-- json = JSON.stringify(json, undefined, 2); -->

<script type='text/javascript'>
	$(document).ready(function() {
		$("#calendarListFetch").click(function() {
			$.ajax({
				type : "GET",
				url : "http://localhost:8080/api/reservations?start=1401094800000&end=1401098400000",
				async : true,
				success : function(result) {

					console.log("##2");
					console.log(result);
					console.log("##2.2");
				},
				error : function(data) {

					console.log("##3");
					console.log(data);
					console.log("##3.2");
				}
			});

			console.log("##1");
		});

	});
</script>
</head>
<body>
	<h2>Urls</h2>
	<ul>
		<li><a href="http://localhost:8080">http://localhost:8080</a></li>
		<li><a href="http://localhost:8080/reservation">http://localhost:8080/reservation</a></li>
		<li><a href="http://localhost:8080/reservation.html">http://localhost:8080/reservation.html</a></li>
		<li><a href="http://localhost:8080/api/reservations">http://localhost:8080/api/reservations</a></li>
		<li><button id="calendarListFetch">link</button></li>
	</ul>
</body>
</html>
