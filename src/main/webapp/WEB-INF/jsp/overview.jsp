<!DOCTYPE html>
<html>
<head>
	<meta name="viewport" content="width=device-width">
	<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,700' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" href="${ctx.cssPrefix}/main.css">
</head>
<body>
<div class="box">
	<div>
		<h2>First Floor</h2>
		<canvas id="firstFloorCtx" width="600"></canvas>

		<h2>Second Floor</h2>
		<canvas id="secondFloorCtx" width="600"></canvas>

		<h2>Attic</h2>
		<canvas id="thirdFloorCtx" width="600"></canvas>

	</div>
</div>
<script type="application/javascript" src="${ctx.jsPrefix}/jquery-3.2.1.js"></script>
<script type="application/javascript" src="${ctx.jsPrefix}/functions.js"></script>
<script type="application/javascript">

	var openSVGRequests = 3;

	var firstFloorSVG = new Image();
	firstFloorSVG.src = "${ctx.imgPrefix}/overview/floor_first.svg";
	firstFloorSVG.onload = function()
	{
		drawSVG('firstFloorCtx',firstFloorSVG);
		loadData();
	}

	var secondFloorSVG = new Image();
	secondFloorSVG.src = "${ctx.imgPrefix}/overview/floor_second.svg";
	secondFloorSVG.onload = function()
	{
		drawSVG('secondFloorCtx',secondFloorSVG);
		loadData();
	}

	var thirdFloorSVG = new Image();
	thirdFloorSVG.src = "${ctx.imgPrefix}/overview/floor_attic.svg";
	thirdFloorSVG.onload = function()
	{
		drawSVG('thirdFloorCtx',thirdFloorSVG);
		loadData();
	}

	function loadData()
	{
		openSVGRequests--;

		if( openSVGRequests > 0 ) return;

		$.get( "/overviewTracker/", function( data )
		{
			drawPoints(data,"#c82124");
		});

		function refreshBeacons()
		{
			$.get( "/overviewBeacon/", function( data )
			{
				drawPoints(data,"#006600");

				window.setTimeout( refreshBeacons, 1000 )
			});
		}

		refreshBeacons();
	}
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>