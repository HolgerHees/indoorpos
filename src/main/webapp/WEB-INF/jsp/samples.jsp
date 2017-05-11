<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link href='http://fonts.googleapis.com/css?family=Roboto:400,300,700' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="${ctx.cssPrefix}/main.css">
    <link rel="stylesheet" href="${ctx.cssPrefix}/samples.css">
</head>
<body>
<div class="box">
    <div>
        <div id="samples" class="table"></div>
    </div>
</div>
<script type="application/javascript" src="${ctx.jsPrefix}/jquery-3.2.1.js"></script>
<script type="application/javascript" src="${ctx.jsPrefix}/functions.js"></script>
<script type="application/javascript">

    function updateSamples(data)
    {
        var content = "";
        content += "<div class=\"row\">";
        content += "<span class=\"column head\"></span>";
        content += "<span class=\"column head\">Tracker</span>";
        content += "<span class=\"column head\">Beacon</span>";
        content += "<span class=\"column head\">RSSI</span>";
        content += "<span class=\"column head\">Count</span>";
        content += "</div>";

        for (var i = 0; i < data.length; i++) {
            var sample = data[i];

            content += "<div class=\"row";
	        if ( sample.states.indexOf("skipped") != -1 ) content += " skipped";
	        else if ( sample.states.indexOf("active") != -1 ) content += " active";
	        else if ( sample.states.indexOf("fallback") != -1 ) content += " fallback";

            var info = "";
	        if ( sample.states.indexOf("priority") != -1 )
	        {
		        if ( sample.states.indexOf("priority_signal") != -1 )
		        {
			        info += "PRI+";
		        }
		        else
		        {
			        info += "PRI";
		        }
	        }
	        else if ( sample.states.indexOf("strong_signal") != -1 )
	        {
	        	if( sample.states.indexOf("too_far_away") != -1 )
		        {
			        info += "SIG+";
		        }
		        else
		        {
			        info += "SIG";
		        }
	        }
	        else if( sample.states.indexOf("too_far_away") != -1 )
	        {
		        info += "TFA";
	        }

            content += "\">";
            content += "<span class=\"column\">" + info + "</span>";
            content += "<span class=\"column\">" + sample.trackerName + "</span>";
            content += "<span class=\"column\">" + sample.beaconName + "</span>";
            content += "<span class=\"column\">" + sample.rssi + "</span>";
            content += "<span class=\"column\">" + sample.samples + "</span>";
            content += "</div>";
        }

        var box = document.getElementById("samples");
        box.innerHTML = content;
    }

    function wsOpen(message)
    {
        console.log("wsOpen");
    }

    function wsGetMessage(message)
    {
        //console.log(message );
        updateSamples(JSON.parse(message.data));
    }

    function wsClose(message)
    {
        console.log("wsClose " + message.data);
        window.setTimeout(function(){initWebsocket();},10000);
    }

    function wsError(message)
    {
        console.log("wsError " + message.data);
    }

    function initWebsocket()
    {
        var webSocket = new WebSocket("ws://${ctx.server}/samplesUpdate");
        webSocket.onopen = wsOpen;
        webSocket.onmessage = wsGetMessage;
        webSocket.onclose = wsClose;
        webSocket.onerror = wsError;
    }

    initWebsocket();
</script>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>