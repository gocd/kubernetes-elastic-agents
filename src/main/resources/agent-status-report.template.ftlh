<style>
    [data-plugin-style-id="kubernetes-plugin"] .kubernetes-elastic-agent {
        padding: 20px;
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
        background-color: white;
        left: 0px;
        width: 100%;
        border: 1px #000 solid;
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab {
        display: none;
    }

    [data-plugin-style-id="kubernetes-plugin"] .tab label {
        margin-right: 10px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab:checked ~ label {
        color: #ab70ff;
        border: 1px #000 solid;
    }

    [data-plugin-style-id="kubernetes-plugin"] .checkboxtab:checked ~ .content {
        z-index: 1;
    }
</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="kubernetes-elastic-agent">
        <div class="agent-name">
            This is the stasdedssddtus report page for${elasticAgentId!}
        </div>

        <div class="tabs">
            <div class="tab">
                <input name="agent-information" type="radio" id="pod-details" class="checkboxtab pod-details" checked>
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
                    <dd>${ agentDetails.command !}</dd>

                    <dt>Environment Variables:</dt>
                    <dd>${ agentDetails.env !}</dd>

                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-events" class="checkboxtab pod-events">
                <label for="pod-events">Events</label>
                <dl class="content">
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
                            <#if events?size != 0>
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
                            <#else>
                            <tr>
                                <td colspan="5" class="message">No events available for the current pod.</td>
                            </tr>
                            </#if>
                        </tbody>
                    </table>
                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-logs" class="checkboxtab">
                <label for="pod-logs">Logs</label>
                <dl class="content"> ${ logs !} </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="pod-configuration" class="checkboxtab">
                <label for="pod-configuration">Configuration</label>
                <dl class="content"> ${ configuration !} </dl>
            </div>
        </div>
    </div>
</div>


