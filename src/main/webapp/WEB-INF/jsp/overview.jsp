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
<script type="application/javascript">
	drawSVG('firstFloorCtx','firstFloorSVG',600,442);   // 1000 x 736
	drawSVG('secondFloorCtx','secondFloorSVG',600,442); // 1000 x 736
	drawSVG('thirdFloorCtx','thirdFloorSVG',600,442);   // 1000 x 736

	function drawSVG( canvasId, imageId, width, height )
	{
		var can = document.getElementById(canvasId);
		var ctx = can.getContext('2d');

		var svg = document.getElementById(imageId);
		ctx.drawImage(svg, 0, 0,width,height);
	}
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>