function calculateSVGSizes(svgId, image) {
    var svg = document.getElementById(svgId);

    var imageWidth = image.width;
    var imageHeight = image.height;

    var canvasWidth = svg.width;

    var canvasHeight = imageHeight * canvasWidth / imageWidth;

    svg.height = canvasHeight;

    svg.setAttribute("_originalWidth", imageWidth);
    svg.setAttribute("_originalHeight", imageHeight);
}

function convertX(originalWidth, currentWidth, x) {
    return x;// * currentWidth / originalWidth;
}

function convertY(originalHeight, currentHeight, y) {
    return originalHeight - y;//( y * currentHeight / originalHeight );
}

function drawAreas(currentRects, data, color) {
    var foundRects = {};

    for (var i = 0; i < data.length; i++) {
        var element = data[i];

        var object = null;

        switch (element.floor) {
            case 0:
                object = document.getElementById('firstFloorCtx');
                break;
            case 1:
                object = document.getElementById('secondFloorCtx');
                break;
            case 2:
                object = document.getElementById('thirdFloorCtx');
                break;
            default:
                // inactive point
                continue;
        }

        var originalWidth = object.getAttribute("_originalWidth");
        var originalHeight = object.getAttribute("_originalHeight");

        var currentWidth = object.width;
        var currentHeight = object.height;

        var topLeftX = convertX(originalWidth, currentWidth, element.topLeftX);
        var topLeftY = convertY(originalHeight, currentHeight, element.topLeftY);
        var bottomRightX = convertX(originalWidth, currentWidth, element.bottomRightX);
        var bottomRightY = convertY(originalHeight, currentHeight, element.bottomRightY);

        if (currentRects[element.key]) {
            //console.log("update " + element.key );
            var rect = currentRects[element.key];
            rect.setAttribute("x", topLeftX);
            rect.setAttribute("y", topLeftY);
            rect.setAttribute("width", bottomRightX - topLeftX);
            rect.setAttribute("height", bottomRightY - topLeftY);
        }
        else {
            //console.log("create " + element.key);
            var rect = document.createElementNS("http://www.w3.org/2000/svg", 'rect');
            rect.setAttribute("x", topLeftX);
            rect.setAttribute("y", topLeftY);
            rect.setAttribute("width", bottomRightX - topLeftX);
            rect.setAttribute("height", bottomRightY - topLeftY);
            rect.setAttribute("style", "fill: " + color + ";");

            var svg = object.contentDocument.getElementsByTagName("svg")[0];
            //console.log(rect);
            svg.insertBefore(rect, svg.childNodes[0]);

            currentRects[element.key] = rect;
        }

        foundRects[element.key] = element.key;
    }

    cleanElements(currentRects, foundRects);
}

function drawPoints(currentCircles, data, color) {
    var foundCircles = {};

    for (var i = 0; i < data.length; i++) {
        var element = data[i];

        var object = null;

        switch (element.floor) {
            case 0:
                object = document.getElementById('firstFloorCtx');
                break;
            case 1:
                object = document.getElementById('secondFloorCtx');
                break;
            case 2:
                object = document.getElementById('thirdFloorCtx');
                break;
            default:
                // inactive point
                continue;
        }

        var originalWidth = object.getAttribute("_originalWidth");
        var originalHeight = object.getAttribute("_originalHeight");

        var currentWidth = object.width;
        var currentHeight = object.height;

        var posX = convertX(originalWidth, currentWidth, element.posX);
        var posY = convertY(originalHeight, currentHeight, element.posY);

        if (currentCircles[element.key]) {
            //console.log("update " + element.key );
            var circle = currentCircles[element.key];
            circle.setAttribute("cx", posX);
            circle.setAttribute("cy", posY);
        }
        else {
            //console.log("create " + element.key );
            var circle = document.createElementNS("http://www.w3.org/2000/svg", 'circle');
            circle.setAttribute("cx", posX);
            circle.setAttribute("cy", posY);
            circle.setAttribute("r", 10);
            circle.setAttribute("style", "fill: " + color + ";");

            var svg = object.contentDocument.getElementsByTagName("svg")[0];
            //console.log(circle);
            svg.appendChild(circle);

            currentCircles[element.key] = circle;
        }

        foundCircles[element.key] = element.key;
    }

    cleanElements(currentCircles, foundCircles);
}

function cleanElements(currentElements, usedElements) {
    for (var key in currentElements) {
        // skip loop if the property is from prototype
        if (!currentElements.hasOwnProperty(key)) continue;

        if (usedElements[key]) continue;

        //console.log("remove " + currentCircles[key] );

        currentElements[key].parentNode.removeChild(currentElements[key]);

        delete currentElements[key];
    }
}

function updateSamples(data)
{
	var content = "";
	content += "<div class=\"row\">";
	content += "<span class=\"column head\">Status</span>";
	content += "<span class=\"column head\">Tracker</span>";
	//content += "<span class=\"column head\">Beacon</span>";
	content += "<span class=\"column head\">RSSI</span>";
	content += "<span class=\"column head\">Var.</span>";
	content += "<span class=\"column head\">Cou.</span>";
	content += "</div>";

	for (var i = 0; i < data.length; i++) {
		var sample = data[i];

		//console.log(sample.states);

		content += "<div class=\"row";
		if ( sample.states.indexOf("SKIPPED") != -1 ) content += " skipped";
		else if ( sample.states.indexOf("ACTIVE") != -1 ) content += " active";
		else if ( sample.states.indexOf("FALLBACK") != -1 ) content += " fallback";

		var info = "";
		if ( sample.states.indexOf("PRIORITY") != -1 )
		{
			if ( sample.states.indexOf("PRIORITY_SIGNAL") != -1 )
			{
				info += "PRIO+";
			}
			else
			{
				info += "PRIO";
			}
		}
		else if ( sample.states.indexOf("STRONG_SIGNAL") != -1 )
		{
			if( sample.states.indexOf("TOO_FAR_AWAY") != -1 )
			{
				info += "SIG+";
			}
			else
			{
				info += "SIG";
			}
		}
		else if( sample.states.indexOf("TOO_FAR_AWAY") != -1 )
		{
			info += "FAR";
		}

        else if( sample.states.indexOf("MIN_RSSI") != -1 )
        {
            info += "LOW";
        }

		content += "\">";
		content += "<span class=\"column\">" + info + "</span>";
		content += "<span class=\"column\">" + sample.trackerName + "</span>";
		//content += "<span class=\"column\">" + sample.beaconName + "</span>";
		content += "<span class=\"column\">" + sample.adjustedRssi + " (" + sample.rssi + ")</span>";
		content += "<span class=\"column\">" + parseFloat( sample.variance ).toFixed(2) + "</span>";
		content += "<span class=\"column\">" + sample.samples + "</span>";
		content += "</div>";
	}

	var box = document.getElementById("samples");
	box.innerHTML = content;
}
