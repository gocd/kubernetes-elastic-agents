<style>
	[data-plugin-style-id="kubernetes-plugin"] .kubernetes-elastic-agent {
		padding: 60px 20px 0 20px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .sub-nav {
		padding-left: 25px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .header {
		margin:          25px 0 10px 3px;
		font-weight:     600;
		text-decoration: underline;
	}

	[data-plugin-style-id="kubernetes-plugin"] .tabs {
		position: relative;
		clear:    both;
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
		padding:          7px 15px;
		line-height:      19px;
		font-size:        13px;
		border-radius:    3px 3px 0 0;
		font-weight:      600;
		color:            #333;
	}

	[data-plugin-style-id="kubernetes-plugin"] .checkboxtab:checked ~ label {
		background-color: #dfdef0;
		border:           1px solid;
		border-bottom:    none;
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
		font-size:        13px;
		font-family:      monaco;
		color:            white;
		display:          block;
		font-weight:      400;
		padding-left:     10px;
		max-height:       100%;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-configuration {
		background-color: white;
		font-family:      courier;
		font-size:        15px;
		white-space:      pre;
		font-weight:      600;
		display:          block;
		line-height:      10px;
		color:            #484848;
		border:           1px solid #ddd;
		border-radius:    3px;
		padding:          5px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .tab-content {
		margin-top: 0;
		height:     calc(100vh - 280px);
		overflow-x: hidden;
	}

	[data-plugin-style-id="kubernetes-plugin"] .tab-content-outer {
		background: white;
		padding:    20px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-events table {
		width:     calc(100% - 40px);
		min-width: 95%;
		border:    1px solid #D8D8D8;
		font-size: 13px;
		margin:    10px 20px 20px 20px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-events table thead {
		background: #D8D8D8;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-events table thead th {
		padding:     10px 20px;
		font-weight: bold;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-events table tbody tr {
		border-top:      1px solid #D8D8D8;
		border-bottom:   1px solid #D8D8D8;
		border-collapse: collapse;
	}

	[data-plugin-style-id="kubernetes-plugin"] .pod-events table tbody td {
		padding:       10px 20px;
		max-width:     190px;
		text-overflow: ellipsis;
		overflow:      hidden;
	}

	[data-plugin-style-id="kubernetes-plugin"] dl.properties {
		overflow:       hidden;
		margin:         0;
		font-size:      14px;
		vertical-align: top;
		font-family:    "Open Sans", "Helvetica Neue", Helvetica, Roboto, Arial, sans-serif;
		color:          #484848;
	}

	[data-plugin-style-id="kubernetes-plugin"] .properties dt {
		float:       left;
		clear:       both;
		text-align:  left;
		min-width:   200px;
		font-weight: 600;
		margin:      0;
		padding:     3px;
		min-height:  20px;
		line-height: 1.5;
	}

	[data-plugin-style-id="kubernetes-plugin"] .properties dd {
		margin-left: 200px;
		min-height:  20px;
		padding:     3px;
		line-height: 1.5;
	}

	[data-plugin-style-id="kubernetes-plugin"] .warning-message {
		background-color: #fdf5e2;
		color:            #b88c34;
		font-weight:      600;
		font-size:        15px;
		padding:          10px 20px;
		border-radius:    3px;
		margin:           0 0 1rem 0;
		border:           1px solid rgba(10, 10, 10, 0.25);
		position:         relative;
	}

	[data-plugin-style-id="kubernetes-plugin"] .entity_title span.label {
		position:   absolute;
		margin-top: -10px;
		font-size:  0.625em;
	}

	[data-plugin-style-id="kubernetes-plugin"] .status-report-page-header {
		width:           100%;
		height:          50px;
		z-index:         10;
		position:        fixed;
		background:      #fff;
		box-shadow:      0 0 0 #000;
		border-bottom:   1px solid #ddd;
		background-size: cover;
	}

	[data-plugin-style-id="kubernetes-plugin"] .status-report-page-header .entity_title {
		top:      12px;
		left:     20px;
		position: absolute;
	}

	[data-plugin-style-id="kubernetes-plugin"] .status-report-page-header .entity_title .last h1 {
		margin-top: 2px !important;
	}

	[data-plugin-style-id="kubernetes-plugin"] .status-report-page-header .entity_title li a {
		font-size:   0.9375rem;
		font-weight: 600;
		line-height: normal;
	}

	[data-plugin-style-id="kubernetes-plugin"] textarea[readonly="readonly"], textarea[readonly] {
		cursor: auto;
	}

	[data-plugin-style-id="kubernetes-plugin"] .no-margin-top {
		margin-top: 0px !important;
	}

	[data-plugin-style-id="kubernetes-plugin"] .name-value {
		float:           none;
		list-style-type: none;
	}

	[data-plugin-style-id="kubernetes-plugin"] .name-value_pair {
		float:   none;
		display: flex;
	}

	[data-plugin-style-id="kubernetes-plugin"] .name-value_pair label {
		width:       180px;
		padding:     3px 10px 3px 0;
		min-width:   150px;
		font-weight: 600;
	}

	[data-plugin-style-id="kubernetes-plugin"] .name-value_pair span {
		padding:     3px 10px 3px 0;
		font-size:   13px;
		line-height: 25px;
	}
</style>

<div data-plugin-style-id="kubernetes-plugin">
	<div class="status-report-page-header">
		<ul class="entity_title">
			<li class="name">
				<span class="label">Pipeline</span>
				<a href="/go/tab/pipeline/history/${jobIdentifier.pipelineName!''}"
				   title="View this pipeline's activity">${jobIdentifier.pipelineName!''}</a>
			</li>
			<li class="pipeline_label">
				<span class="label">Instance</span>
				<span class="run_no">${jobIdentifier.pipelineCounter!''}</span>
				<a href="/go/pipelines/value_stream_map/${jobIdentifier.pipelineName!''}/${jobIdentifier.pipelineCounter!''}"
				   title="View this stage's jobs summary">VSM</a>
			</li>
			<li class="stage_name">
				<span class="label">Stage</span>
				<a href="/go/pipelines/${jobIdentifier.pipelineName!''}/${jobIdentifier.pipelineCounter!''}/${jobIdentifier.stageName!''}/${jobIdentifier.stageCounter!''}"
				   title="View this stage's details">${jobIdentifier.stageName!''}
					/ ${jobIdentifier.stageCounter!''}</a>
			</li>
			<li class="job_label">
				<span class="label">Job</span>
				<a href="/go/tab/build/detail/${jobIdentifier.pipelineName!''}/${jobIdentifier.pipelineCounter!''}/${jobIdentifier.stageName!''}/${jobIdentifier.stageCounter!''}/${jobIdentifier.jobName!''}"
				   title="View this job's details">${jobIdentifier.jobName!''}</a>
			</li>

			<li class="last">
				<span class="label">Elastic Agent Id</span>
				<h1>${ elasticAgentId! }</h1>
			</li>
		</ul>
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
					<h4 class="header no-margin-top">Pod Details</h4>
					<ul class="name-value">
						<li class="name-value_pair">
							<label>Pod name</label>
							<span>${ podDetails.name !}</span>
						</li>
						<li class="name-value_pair">
							<label>Cluster Name</label>
							<span>${ podDetails.clusterName !}</span>
						</li>
						<li class="name-value_pair">
							<label>Node Name</label>
							<span>${ podDetails.nodeName !}</span>
						</li>
						<li class="name-value_pair">
							<label>Namespace</label>
							<span>${ podDetails.namespace !}</span>
						</li>
						<li class="name-value_pair">
							<label>Creation Timestamp</label>
							<span>${ podDetails.createdAt !}</span>
						</li>
						<li class="name-value_pair">
							<label>Start Timestamp</label>
							<span>${ podDetails.startedAt !}</span>
						</li>
						<li class="name-value_pair">
							<label>Conditions</label>
							<span>${ podDetails.conditions !}</span>
						</li>
						<li class="name-value_pair">
							<label>Status</label>
							<span>${ podDetails.phase !}</span>
						</li>
						<li class="name-value_pair">
							<label>Pod IP</label>
							<span>${ podDetails.podIP !}</span>
						</li>
						<li class="name-value_pair">
							<label>Host IP</label>
							<span>${ podDetails.hostIP !}</span>
						</li>
					</ul>
					<h4 class="header">GoCD Agent Details</h4>
					<ul class="name-value">
						<li class="name-value_pair">
							<label>Name</label>
							<span>${ agentDetails.name !}</span>
						</li>
						<li class="name-value_pair">
							<label>Image</label>
							<span>${ agentDetails.image !}</span>
						</li>
						<li class="name-value_pair">
							<label>ImagePullPolicy</label>
							<span>${ agentDetails.imagePullPolicy !}</span>
						</li>
						<li class="name-value_pair">
							<label>Ready</label>
							<span>${ agentDetails.ready !}</span>
						</li>
						<li class="name-value_pair">
							<label>Restart Count</label>
							<span>${ agentDetails.restartCount !}</span>
						</li>
						<li class="name-value_pair">
							<label>Command</label>
							<span>${ agentDetails.command?html?replace("\n", "<br>") !}</span>
						</li>
						<li class="name-value_pair">
							<label>Environment Variables</label>
							<span>${ agentDetails.env?html?replace("\n", "<br>") !}</span>
						</li>
					</ul>
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
                        <div class="warning-message">No events available for the current pod.</div>
                    </#if>
				</div>
				<textarea class="tab-content pod-logs" ng-show="currenttab == 'pod-logs'"
						  readonly>${ logs?html?trim !}</textarea>
				<div class="tab-content pod-configuration"
					 ng-show="currenttab == 'pod-configuration'">${ configuration?html?trim?replace("\n", "<br>") !}
				</div>
			</div>
		</div>
	</div>

</div>


