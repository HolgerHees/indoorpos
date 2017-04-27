<html>
<head>
	<script src="${ctx.jsPrefix}/jquery-2.1.3.js"></script>
	<script>
		function send(button) {
			$.ajax({
				type: "POST",
				url: "/tracker/",
				data: $(button.form.call).val(),
				success: function(data) {
					$('#result').html('<pre>' + data + '</pre>');
				},
				dataType: "text"
			});
		}
	</script>
</head>
<body>
<form>
		<textarea name="call" style="width:100%;height:300px;">
{
	"version":1,
	"call":[
		{
			"methodKey":"",
			"methodName":"getUserData",
			"parameter": {
				"email": "test@gmail.com"
			}
		}
	]
}
		</textarea>
	<button type="button" onclick="send(this);return false;">Submit</button>
</form>
<div id="result" style="height:300px;"></div>
</body>
</html>