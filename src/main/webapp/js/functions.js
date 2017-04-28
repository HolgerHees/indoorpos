function drawSVG( canvasId, image )
{
	var can = document.getElementById(canvasId);
	var ctx = can.getContext('2d');

	var imageWidth = image.width;
	var imageHeight = image.height;

	var canvasWidth = can.width;

	var canvasHeight = imageHeight * canvasWidth / imageWidth;

	can.height = canvasHeight;

	can.setAttribute("_originalWidth",imageWidth);
	can.setAttribute("_originalHeight",imageHeight);

	ctx.drawImage(image, 0, 0, canvasWidth, canvasHeight);
}

function convertX( originalWidth, currentWidth, x )
{
	return x * currentWidth / originalWidth;
}

function convertY( originalHeight, currentHeight, y )
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

		var ctx = canvas.getContext('2d');

		var originalWidth = canvas.getAttribute("_originalWidth");
		var originalHeight = canvas.getAttribute("_originalHeight");

		var currentWidth = canvas.width;
		var currentHeight = canvas.height;

		var posX = convertX( originalWidth, currentWidth, element.posX );
		var posY = convertY( originalHeight, currentHeight, element.posY );

		console.log( element.name + " "  + posX + " " + posY );

		ctx.fillStyle = color
		ctx.beginPath();
		ctx.arc( posX, posY, 10, 0, Math.PI*2, true);
		ctx.closePath();
		ctx.fill();
	}
}