<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Mule Migration Tool Report</title>
    <link rel="stylesheet" type="text/css" href="assets/styles/mulesoft-styles.css">
    <link rel="stylesheet" type="text/css" href="assets/styles/tsorter.css">
</head>
<body>
<div class="mulesoft-topbar">
    <div class="mulesoft-appbar">
        <div class="muleicon muleicon-logo"></div>
        <div class="anypoint-brand">Mule Migration Tool Report</div>
    </div>
</div>
<div class="col-md-8">
    <h4>Issues found during migration:</h2>
</div>
<div class="col-md-8 col-md-offset-1">
    <h2 class="text-bold">Errors:</h2>
    <table id="resources_table" class="table table-featured table-hover sortable">
            <thead>
            <tr>
                <th colspan="2" data-tsorter="link">Resource</th>
                <th data-tsorter="numeric"># Issues</th>
            </tr>
            </thead>
            <tbody id="table-body">
            	<#list applicationErrors?keys as key>
                <tr>
                    <td colspan="2"><a href=${"resources/error-" + key?remove_ending(".xml") + "-report.html"}>${key}</a></td>
                    <td>${applicationErrors[key]?size}</td>
	            	</tr>
            	</#list>
            </tbody>
        </table>
</div>
<div class="col-md-8 col-md-offset-1">
    <h2 class="text-bold">Warnings:</h2>
    <table id="resources_table" class="table table-featured table-hover sortable">
            <thead>
            <tr>
                <th colspan="2" data-tsorter="link">Resource</th>
                <th data-tsorter="numeric"># Issues</th>
            </tr>
            </thead>
            <tbody id="table-body">
                <#list applicationWarnings?keys as key>
                <tr>
                    <td colspan="2" ><a href=${"resources/warn-" + key?remove_ending(".xml") + "-report.html"}>${key}</a></td>
                    <td>${applicationWarnings[key]?size}</td>
                </tr>
                </#list>
            </tbody>
        </table>
</div>
</body>
</html>
