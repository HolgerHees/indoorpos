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
		<canvas id="firstFloorCtx" width="600" height="442"></canvas>
		<img id="firstFloorSVG" src="${ctx.imgPrefix}/overview/floor_first.svg" style="display:none">

		<h2>Second Floor</h2>
		<canvas id="secondFloorCtx" width="600" height="442"></canvas>
		<img id="secondFloorSVG" src="${ctx.imgPrefix}/overview/floor_second.svg" style="display:none">

		<h2>Attic</h2>
		<canvas id="thirdFloorCtx" width="600" height="442"></canvas>
		<img id="thirdFloorSVG" src="${ctx.imgPrefix}/overview/floor_attic.svg" style="display:none">

	</div>
</div>
<script type="application/javascript" src="${ctx.jsPrefix}/jquery-3.2.1.js"></script>
<script type="application/javascript">
	var originalWidth = 1000;
	var originalHeight = 736;

	var currentWidth = 600;
	var currentHeight = 442;

	drawSVG('firstFloorCtx','firstFloorSVG',currentWidth,currentHeight);   // 1000 x 736
	drawSVG('secondFloorCtx','secondFloorSVG',currentWidth,currentHeight); // 1000 x 736
	drawSVG('thirdFloorCtx','thirdFloorSVG',currentWidth,currentHeight);   // 1000 x 736

	function drawSVG( canvasId, imageId, width, height )
	{
		var can = document.getElementById(canvasId);
		var ctx = can.getContext('2d');

		var svg = document.getElementById(imageId);
		ctx.drawImage(svg, 0, 0,width,height);
	}

	function convertX( x )
	{
		return x * currentWidth / originalWidth;
	}

	function convertY( y )
	{
		return currentHeight - ( y * currentHeight / originalHeight );
	}

	function drawPoints( data, color )
	{
		for( var i = 0; i < data.length; i++ )
		{
			var element = data[i];

			var canvas = null;

			switch( element.floor )
			{
				case 0:
					canvas = document.getElementById('firstFloorCtx');
					break;
				case 1:
					canvas = document.getElementById('secondFloorCtx');
					break;
				case 2:
					canvas = document.getElementById('thirdFloorCtx');
					break;
				default:
					// inactive point
					continue;
			}

			console.log( element.name + " "  + convertX( element.posX ) + " " + convertY( element.posY ) );

			var ctx = canvas.getContext('2d');

			ctx.fillStyle = color
			ctx.beginPath();
			ctx.arc( convertX( element.posX ), convertY( element.posY ), 10, 0, Math.PI*2, true);
			ctx.closePath();
			ctx.fill();
		}
	}

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
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>