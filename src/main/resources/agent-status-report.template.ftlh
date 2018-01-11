<style>
    [data-plugin-style-id="kubernetes-plugin"] .kubernetes-elastic-agent {
        padding: 60px 20px 0 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .sub-nav {
        padding-left: 25px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .header {
        margin: 25px 0 10px 3px;
        font-weight: 600;
        text-decoration: underline;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs {
        position: relative;
        clear: both;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs .tab {
        float: left;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs .tab .content {
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab {
        display: none;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tab label {
        background-color: #d1c4e9;
        padding: 7px 15px;
        line-height: 19px;
        font-size: 13px;
        border-radius: 3px 3px 0 0;
        font-weight: 600;
        color: #333;
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab:checked ~ label {
        background-color: #dfdef0;
        border: 1px solid;
        border-bottom: none;
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab:checked ~ .content {
        z-index: 1;
        display: block !important;
    }

    [data-plugin-style-id="kubernetes-plugin"] .agent-name {
        padding: 10px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tab-content.pod-logs {
        background-color: #383838;
        font-size: 13px;
        font-family: monaco;
        color: white;
        display: block;
        font-weight: 400;
        line-height: 20px;
        padding-left: 10px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-configuration {
        background-color: white;
        font-family: courier;
        font-size: 15px;
        white-space: pre;
        font-weight: 600;
        display: block;
        line-height: 10px;
        color: #484848;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tab-content {
        margin-top: 0;
        height: calc(100vh - 280px);
        overflow-x: hidden;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tab-content-outer {
        background: white;
        padding: 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-events table {
        width: calc(100% - 40px);
        min-width: 95%;
        border: 1px solid #D8D8D8;
        font-size: 13px;
        margin: 10px 20px 20px 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-events table thead {
        background: #D8D8D8;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-events table thead th {
        padding: 10px 20px;
        font-weight: bold;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-events table tbody tr {
        border-top: 1px solid #D8D8D8;
        border-bottom: 1px solid #D8D8D8;
        border-collapse: collapse;
    }

    [data-plugin-style-id="kubernetes-plugin"] .pod-events table tbody td {
        padding: 10px 20px;
        max-width: 190px;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties {
        overflow: hidden;
        margin: 0;
        font-size: 14px;
        vertical-align: top;
        font-family: "Open Sans", "Helvetica Neue", Helvetica, Roboto, Arial, sans-serif;
        color: #484848;
    }

    [data-plugin-style-id="kubernetes-plugin"] .properties dt {
        float: left;
        clear: both;
        text-align: left;
        min-width: 200px;
        font-weight: 600;
        margin: 0;
        padding: 3px;
        min-height: 20px;
        line-height: 1.5;
    }

    [data-plugin-style-id="kubernetes-plugin"] .properties dd {
        margin-left: 200px;
        min-height: 20px;
        padding: 3px;
        line-height: 1.5;
    }

</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="row">
        <div class="header-panel">
            <header class="page-header sub-nav">
                <f.row>
                    <f.column size={6}>
                        <h1>${ elasticAgentId! }</h1>
                    </f.column>
                </f.row>
            </header>
        </div>
    </div>

    <div class="content_wrapper_inner kubernetes-elastic-agent">
        <div class="row">
            <div class="sub_tabs_container">
                <ul class="tabs" ng-init="currenttab=currentab || 'pod-details'">
                    <li ng-class="{'current_tab' : currenttab == 'pod-details' }" ng-click="currenttab='pod-details'">
                        <a href="#">Details</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'pod-events' }" ng-click="currenttab='pod-events'">
                        <a href="#">Events</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'pod-logs' }" ng-click="currenttab='pod-logs'">
                        <a href="#">Logs</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'pod-configuration' }"
                        ng-click="currenttab='pod-configuration'">
                        <a href="#">Configuration</a>
                    </li>
                </ul>
            </div>

            <div class="tab-content-outer">
                <div class="tab-content" ng-show="currenttab == 'pod-details'">
                    <h4 class="header">Pod Details</h4>
                    <dl class="properties">
                        <dt>Pod Name:</dt>
                        <dd>${ podDetails.name !}</dd>
                        <dt>Cluster Name:</dt>
                        <dd>${ podDetails.clusterName !}</dd>
                        <dt>Node Name:</dt>
                        <dd>${ podDetails.nodeName !}</dd>
                        <dt>Namespace:</dt>
                        <dd>${ podDetails.namespace !}</dd>

                        <dt>Creation Timestamp:</dt>
                        <dd>${ podDetails.createdAt !}</dd>
                        <dt>Start Timestamp:</dt>
                        <dd>${ podDetails.startedAt !}</dd>

                        <dt>Status:</dt>
                        <dd>${ podDetails.phase !}</dd>

                        <dt>Pod IP:</dt>
                        <dd>${ podDetails.podIP !}</dd>
                        <dt>Host IP:</dt>
                        <dd>${ podDetails.hostIP !}</dd>
                    </dl>
                    <h4 class="header">GoCD Agent Details</h4>
                    <dl class="properties">
                        <dt>Name:</dt>
                        <dd>${ agentDetails.name !}</dd>
                        <dt>Image:</dt>
                        <dd>${ agentDetails.image !}</dd>
                        <dt>ImagePullPolicy:</dt>
                        <dd>${ agentDetails.imagePullPolicy !}</dd>

                        <dt>Ready:</dt>
                        <dd>${ agentDetails.ready !}</dd>

                        <dt>Restart Count:</dt>
                        <dd>${ agentDetails.restartCount !}</dd>

                        <dt>Command:</dt>
                        <dd>${ agentDetails.command?html?replace("\n", "<br>") !}</dd>

                        <dt>Environment Variables:</dt>
                        <dd>${ agentDetails.env?html?replace("\n", "<br>") !}</dd>
                    </dl>
                </div>
                <div class="tab-content pod-events" ng-show="currenttab == 'pod-events'">
                <#if events?size != 0>
                    <table>
                        <thead>
                        <tr>
                            <th>First Seen</th>
                            <th>Last Seen</th>
                            <th>Count</th>
                            <th>Path</th>
                            <th>Type</th>
                            <th>Reason</th>
                            <th width="700">Message</th>
                        </thead>
                        </tr>
                        <tbody>
                                <#list events as event>
                                <tr>
                                    <td>${event.firstTimestamp!}</td>
                                    <td>${event.lastTimestamp!}</td>
                                    <td>${event.count!}</td>
                                    <td>${event.fieldPath!}</td>
                                    <td>${event.type!}</td>
                                    <td>${event.reason!}</td>
                                    <td>${event.message!}</td>
                                </tr>
                                </#list>

                        </tbody>
                    </table>
                <#else>
                            <tr>
                                <td colspan="5" class="message">No events available for the current pod.</td>
                            </tr>
                </#if>
                </div>
                <div class="tab-content pod-logs" ng-show="currenttab == 'pod-logs'">
                ${ logs?html?replace("\n", "<br>") !}
                </div>
                <div class="tab-content pod-configuration" ng-show="currenttab == 'pod-configuration'">
                ${ configuration?html?replace("\n", "<br>") !}
                </div>
            </div>
        </div>
    </div>

</div>


