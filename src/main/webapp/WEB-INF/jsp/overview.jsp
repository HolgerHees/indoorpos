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
        <object id="firstFloorCtx" data="${ctx.imgPrefix}/overview/floor_first.svg" width="600" type="image/svg+xml"></object>

        <h2>Second Floor</h2>
        <object id="secondFloorCtx" data="${ctx.imgPrefix}/overview/floor_second.svg" width="600" type="image/svg+xml"></object>

        <h2>Attic</h2>
        <object id="thirdFloorCtx" data="${ctx.imgPrefix}/overview/floor_attic.svg" width="600" type="image/svg+xml"></object>

    </div>
</div>
<script type="application/javascript" src="${ctx.jsPrefix}/jquery-3.2.1.js"></script>
<script type="application/javascript" src="${ctx.jsPrefix}/functions.js"></script>
<script type="application/javascript">

    var openSVGRequests = 3;

    var firstFloorSVG = new Image();
    firstFloorSVG.src = "${ctx.imgPrefix}/overview/floor_first.svg";
    firstFloorSVG.onload = function () {
        calculateSVGSizes('firstFloorCtx', firstFloorSVG);
        loadData();
    }

    var secondFloorSVG = new Image();
    secondFloorSVG.src = "${ctx.imgPrefix}/overview/floor_second.svg";
    secondFloorSVG.onload = function () {
        calculateSVGSizes('secondFloorCtx', secondFloorSVG);
        loadData();
    }

    var thirdFloorSVG = new Image();
    thirdFloorSVG.src = "${ctx.imgPrefix}/overview/floor_attic.svg";
    thirdFloorSVG.onload = function () {
        calculateSVGSizes('thirdFloorCtx', thirdFloorSVG);
        loadData();
    }

    areaRects = {};
    trackerCircles = {};
    beaconCircles = {};

    function loadData()
    {
        openSVGRequests--;

        if (openSVGRequests > 0) return;

        $.get("/overviewTracker/", function (data) {
            drawPoints( trackerCircles, data, "#c82124" );
        });

        function refreshBeacons()
        {
            $.get( "/overviewArea/", function( data )
            {
                drawAreas( areaRects, data, "#000066");

                window.setTimeout(refreshBeacons, 1000 );
            });
        }

        refreshBeacons();
    }
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>