<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Mule Migration Assistant Report</title>
    <link rel="stylesheet" type="text/css" href="assets/styles/mulesoft-styles.css">
    <link rel="stylesheet" type="text/css" href="assets/styles/tsorter.css">
</head>
<body>
<div class="mulesoft-topbar">
    <div class="mulesoft-appbar">
        <div class="muleicon muleicon-logo"></div>
        <div class="anypoint-brand">Mule Migration Assistant ${version}</div>
    </div>
</div>
<div class="col-md-8">
    <h2 class="text-bold">Migration Summary</h2>
    <div class="col-md-8 col-md-offset-1">
        <h3><span class="text-bold">Migrated Components:</span> <span>${componentsMigrated}</span></h3>
        <h3><span class="text-bold">Migrated MEL Expressions:</span> <span>${melExpressionsMigrated}</span></h3>
        <h3><span class="text-bold">Migrated MEL Expression Lines:</span> <span>${melExpressionLinesMigrated}</span></h3>
        <h3><span class="text-bold">Migrated DW Transformations:</span> <span>${dwTransformationsMigrated}</span></h3>
        <h3><span class="text-bold">Migrated DW Transformation Lines:</span> <span>${dwTransformationLinesMigrated}</span></h3>
    </div>
</div>
<div class="col-md-12"><hr /></div>
<div class="col-md-8">
    <h2 class="text-bold">Issues found during migration</h2>
</div>
    <div class="col-md-8 col-md-offset-1">
        <h2 class="text-bold">Errors:</h2>
        <table id="error_resources_table" class="table table-featured table-hover sortable">
                <thead>
                <tr>
                    <th colspan="2" data-tsorter="link">Resource</th>
                    <th data-tsorter="numeric"># Issues</th>
                </tr>
                </thead>
                <tbody id="error_table_body">
                    <#list applicationSummaryErrors as path, issuesCount>
                        <tbody class="labels">
                            <tr>
                                <td colspan="2">
                                    <a><label for="error-${path}">${path}</label></a>
                                    <input type="checkbox" name="error-${path}" id="error-${path}" data-toggle="toggle">
                                </td>
                                <td>${issuesCount}</td>
                            </tr>
                        </tbody>
                        <tbody class="hide" style="display: none">
                            <#assign count = 0>
                            <#list applicationErrors[path] as message, entries>
                                    <tr>
                                        <td colspan="2">
                                            <a href=${"resources/error-" + path?remove_ending(".xml") + "-" + count + ".html"}>${ "- " + message}</a>
                                        </td>
                                        <td>${entries?size}</td>
                                    </tr>
                                <#assign count = count + 1>
                            </#list>
                        </tbody>
                    <#else>
                        <tr>
                            <td colspan="2">No errors found during migration.</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
    </div>
    <div class="col-md-8 col-md-offset-1">
        <h2 class="text-bold">Warnings:</h2>
        <table id="warn_resources_table" class="table table-featured table-hover sortable">
                <thead>
                <tr>
                    <th colspan="2" data-tsorter="link">Resource</th>
                    <th data-tsorter="numeric"># Issues</th>
                </tr>
                </thead>
                <tbody id="warn_table_body">
                    <#list applicationSummaryWarnings as path, issuesCount>
                        <tbody class="labels">
                            <tr>
                                <td colspan="2">
                                    <a><label for="warn-${path}">${path}</label></a>
                                    <input type="checkbox" name="warn-${path}" id="warn-${path}" data-toggle="toggle">
                                </td>
                                <td>${issuesCount}</td>
                            </tr>
                        </tbody>
                        <tbody class="hide" style="display: none">
                            <#assign count = 0>
                            <#list applicationWarnings[path] as message, entries>
                                    <tr>
                                        <td colspan="2">
                                            <a href=${"resources/warn-" + path?remove_ending(".xml") + "-" + count + ".html"}>${ "- " + message}</a>
                                        </td>
                                        <td>${entries?size}</td>
                                    </tr>
                                <#assign count = count + 1>
                            </#list>
                        </tbody>
                    <#else>
                        <tr>
                            <td colspan="2">No warnings found during migration.</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
    </div>
    <div class="col-md-8 col-md-offset-1">
            <h2 class="text-bold">Info:</h2>
            <table id="info_resources_table" class="table table-featured table-hover sortable">
                    <thead>
                    <tr>
                        <th colspan="2" data-tsorter="link">Resource</th>
                        <th data-tsorter="numeric"># Issues</th>
                    </tr>
                    </thead>
                    <tbody id="info_table_body">
                        <#list applicationSummaryInfo as path, issuesCount>
                            <tbody class="labels">
                                <tr>
                                    <td colspan="2">
                                        <a><label for="info-${path}">${path}</label></a>
                                        <input type="checkbox" name="warn-${path}" id="info-${path}" data-toggle="toggle">
                                    </td>
                                    <td>${issuesCount}</td>
                                </tr>
                            </tbody>
                            <tbody class="hide" style="display: none">
                                <#assign count = 0>
                                <#list applicationInfo[path] as message, entries>
                                        <tr>
                                            <td colspan="2">
                                                <a href=${"resources/info-" + path?remove_ending(".xml") + "-" + count + ".html"}>${ "- " + message}</a>
                                            </td>
                                            <td>${entries?size}</td>
                                        </tr>
                                    <#assign count = count + 1>
                                </#list>
                            </tbody>
                        <#else>
                            <tr>
                                <td colspan="2">No info entries found during migration.</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
        </div>

    <script type="text/javascript" src="assets/js/jquery-3.3.1.js"></script>
    <script type="text/javascript">
         $('[data-toggle="toggle"]').change(function(){
            $(this).parents().next('.hide').toggle();
        });
    </script>
</body>
</html>
