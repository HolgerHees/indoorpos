<!DOCTYPE html>
<html>
<head>
	<%@ include file="blocks/metadata.jsp" %>
    <link rel="stylesheet" href="${ctx.cssPrefix}/samples.css">
</head>
<body>
<div class="box">
    <div>
        <div id="samples" class="table"></div>
    </div>
</div>
<script type="application/javascript">

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
<%@ include file="blocks/footer.jsp" %>
</body>
</html>