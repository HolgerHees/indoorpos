<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width">
    <link href='http://fonts.googleapis.com/css?family=Roboto:400,300,700' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="${ctx.cssPrefix}/main.css">
	<link rel="stylesheet" href="${ctx.cssPrefix}/samples.css">
</head>
<body>
<div class="box">
    <div>
	    <div id="samples" class="table">

	    </div>
    </div>
</div>
<script type="application/javascript" src="${ctx.jsPrefix}/jquery-3.2.1.js"></script>
<script type="application/javascript" src="${ctx.jsPrefix}/functions.js"></script>
<script type="application/javascript">

    function refreshSamples()
    {
        $.get( "/samplesUpdate/", function( data )
        {
        	var content = "";
			for( var i = 0; i < data.length; i++ )
	        {
		        var sample = data[i];

		        content += "<div class=\"row\">";
		        content += "<span class=\"column\">" + sample.trackerName + "</span>";
				content += "<span class=\"column\">" + sample.trackerName + "</span>";
		        content += "<span class=\"column\">" + sample.beaconName + "</span>";
		        content += "<span class=\"column\">" + sample.rssi + "</span>";
		        content += "<span class=\"column\">" + sample.samples + "</span>";
		        content += "</div>";
	        }

	        var box = document.getElementById("samples");
			box.innerHTML = content;

            window.setTimeout(refreshSamples, 500);
        });
    }

    refreshBeacons();
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>