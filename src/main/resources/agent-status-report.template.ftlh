<style>
    [data-plugin-style-id="kubernetes-plugin"] .kubernetes-elastic-agent {
        padding: 50px 20px 0 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs {
        position: relative;
        clear: both;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs .tab {
        float: left;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tabs .tab .content {
        position: absolute;
        display: none;
        left: 0px;
        width: 100%;
        height: calc(100vh - 225px);
        border: 1px #000 solid;
        border-radius: 5px;
        overflow-x: hidden;
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

    [data-plugin-style-id="kubernetes-plugin"] .pod-logs {
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

</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="row">
        <div class="header-panel">
            <header class="page-header">
                <f.row>
                    <f.column size={6}>
                        <h1>${ elasticAgentId! }</h1>
                    </f.column>
                </f.row>
            </header>
        </div>
    </div>
    <div class="kubernetes-elastic-agent row">
        <div class="tabs">
            <div class="tab">
                <input name="agent-information" type="radio" id="pod-details" class="checkboxtab pod-details">
                <label for="pod-details">Pod Details</label>
                <dl class="content">
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

                    <dt>Conditions:</dt>
                    <dd>
                        <table>
                            <thead>
                            <tr>
                                <th>Last Transition Time</th>
                                <th>Last Probe Time</th>
                                <th>Type</th>
                                <th>Status</th>
                            </thead>
                            </tr>
                            <tbody>
                                <#list podDetails.conditions as condition>
                                <tr>
                                    <td>${condition.lastTransitionTime!}</td>
                                    <td>${condition.lastProbeTime!}</td>
                                    <td>${condition.type!}</td>
                                    <td>${condition.status!}</td>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </dd>
                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="agent-details" class="checkboxtab agent-details">
                <label for="agent-details">GoCD Agent Details</label>
                <dl class="content">
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

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-events" class="checkboxtab pod-events">
                <label for="pod-events">Events</label>
                <dl class="content">
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
                                <th>Message</th>
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
                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-logs" class="checkboxtab" checked>
                <label for="pod-logs">Logs</label>
                <dl class="content pod-logs"> ${ logs?html?replace("\n", "<br>") !} </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-configuration" class="checkboxtab">
                <label for="pod-configuration">Configuration</label>
                <dl class="content pod-configuration"> ${ configuration?html?replace("\n", "<br>") !} </dl>
            </div>
        </div>
    </div>
</div>


