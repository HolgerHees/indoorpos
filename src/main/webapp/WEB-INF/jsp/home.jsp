<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width">
<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,700' rel='stylesheet' type='text/css'>
<style>
HTML, BODY, H1, H2, P, A {
    margin: 0;
    padding: 0;
    border: 0;
}
body {
    display: block;
    padding: 0;
    border: 0;
    background-color: #eee;
    font-family: helvetica, arial, sans-serif;
    margin: 10px;
    color: #444;
    font-size: 16px;
}
BODY > * {
    max-width: 650px;
    margin: 0 auto;
}
div {
    display: block;
    background-color: #fff;
    padding: 15px;
}
H1 {
	font-family: Roboto, helvetica, arial, sans-serif;
    font-weight: 300;
    font-size: 2em;
    color: #666;
    display: block;
    -webkit-margin-before: 0.67em;
    -webkit-margin-after: 0.67em;
    -webkit-margin-start: 0px;
    -webkit-margin-end: 0px;
}
H2 {
    font-family: Roboto, helvetica, arial, sans-serif;
    font-weight: 300;
    font-size: 1.4em;
    font-style: italic;
    margin-bottom: 10px;
    display: block;
    -webkit-margin-before: 0.83em;
    -webkit-margin-after: 0.83em;
    -webkit-margin-start: 0px;
    -webkit-margin-end: 0px;
}
p {
    display: block;
    -webkit-margin-before: 1em;
    -webkit-margin-after: 1em;
    -webkit-margin-start: 0px;
    -webkit-margin-end: 0px;
}
A {
    color: #33b5e5;
    text-decoration: none;
}
A {
    color: #33b5e5;
    text-decoration: none;
}
a:-webkit-any-link {
    color: #33b5e5;
    text-decoration: underline;
    cursor: auto;
}
.box {
    border-radius: 3px;
    border-width: 1px 1px 2px;
    border-style: solid;
    border-color: #D8D8D8;
    -moz-border-top-colors: none;
    -moz-border-right-colors: none;
    -moz-border-bottom-colors: none;
    -moz-border-left-colors: none;
    margin-top:10px;
}
.icon {
    height: 5em;
    float: left;
    margin-right: 25px;
}
.hint {
    font-family: Roboto,helvetica,arial,sans-serif;
    font-weight: 400;
    font-size: 1.2em;
    color: #999;
}
footer {
    font-size: 12px;
    color: #999;
    padding: 10px 15px 0px;
    text-align:center;
}
</style>
</head>
<body>
<div class="box">
    <div>
        <img class="icon" src="${ctx.imgPrefix}/home/home.jpg">
        <h1>Indoorpos</h1>
        <span class="hint">Track your indoor position</span>
        <div style="float:clear"></div>
    </div>
    <div>
        <h2>More about Indoorpos is coming soon.</h2>
    </div>
</div>
<footer>&copy; 2017 by Holger Hees</footer>
</body>
</html>