<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Mule Migratiol Tool Report</title>
    <link rel="stylesheet" type="text/css" href="../assets/styles/mulesoft-styles.css">
    <link rel="stylesheet" type="text/css" href="../assets/styles/tsorter.css">
</head>
<body>
    <div class="mulesoft-topbar">
        <div class="mulesoft-appbar">
            <div class="muleicon muleicon-logo"></div>
            <div class="anypoint-brand">Mule Migration Tool ${version}</div>
        </div>
    </div>
    <div class="col-md-2 col-md-offset-1 sidemenu">
        <ul class="sidemenu-back">
            <li><a href="../summary.html">Summary</a></li>
        </ul>
    </div>
    <div class="col-md-8">
        <h2 class="text-bold">Issues found on: <span class="text-italic text-normal">${resource}</span></h2>
        <h4 class="text-bold">Description: <span class="text-italic text-normal">${description}</span></h4>
        <h4 class="text-bold">Documentation: </h4>
        <#list docLinks as doc>
            <h4><a href="${doc}">${doc}</a></h4>
        </#list>
        <hr>
        <br>
        <#list entries as entry>
            <div class="col-md-12">
                <h4 class="text-bold">
                    Line: <span class="text-italic text-normal">${entry.lineNumber}</span>&nbsp;
                    Column: <span class="text-italic text-normal">${entry.columnNumber}</span>
                </h4>
                <br>
                <pre>${entry.elementContent}</pre>
                <hr>
            </div>
        </#list>
    </div>
</body>
</html>