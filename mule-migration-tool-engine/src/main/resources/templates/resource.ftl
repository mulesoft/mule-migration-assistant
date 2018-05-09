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
            <div class="anypoint-brand">Mule Migration Tool Report - ${resource}</div>
        </div>
    </div>
    <div class="col-md-2 col-md-offset-1 sidemenu">
        <ul class="sidemenu-back">
            <li><a href="../summary.html">Summary</a></li>
        </ul>
    </div>
    <div class="col-md-8">
        <h2 class="text-bold">Issues found on: <span class="text-italic text-normal">${resource}</span></h2>
        <hr>
        <br>
        <#list entries as entry>
        <div class="col-md-12">
            <h4 class="text-bold">Description: </h4>
            <h4>${entry.message}</h4>
            <h4 class="text-bold">Documentation: </h4>
            <#list entry.documentationLinks as doc>
                <h4><a href="${doc}">${doc}</a></h4>
            </#list>
            <h4 class="text-bold">Line: <span class="text-italic text-normal">${entry.lineNumber}</span></h4>
            <h4 class="text-bold">Column: <span class="text-italic text-normal">${entry.columnNumber}</span></h4>
            <br>
            <code>${entry.elementContent}</code>
            <hr>
        </div>
        </#list>
    </div>
</body>
</html>