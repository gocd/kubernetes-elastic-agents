<style>
	[data-plugin-style-id="kubernetes-plugin"] .outer-container {
		display:  table;
		position: absolute;
		height:   calc(100vh - 119px);
		width:    100%;
	}

	[data-plugin-style-id="kubernetes-plugin"] .container {
		display: table-cell;
	}

	[data-plugin-style-id="kubernetes-plugin"] .error-container {
		width:         100%;
		border:        1px solid #eee;
		display:       table;
		margin:        15px 0px;
		table-layout:  fixed;
		border-radius: 3px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .title {
		padding:        1rem;
		overflow:       hidden;
		font-size:      15px;
		background:     #d1c4e9;
		font-weight:    600;
		vertical-align: middle;
		margin:         -1px -1px 0px -1px;
		border-radius:  3px 3px 0px 0px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .stacktrace {
		height: calc(100vh - 210px);
	}

	[data-plugin-style-id="kubernetes-plugin"] .no-stacktrace {
		color:       #777;
		padding:     10px;
		font-size:   15px;
		font-weight: 400;
		background:  #FAFAFA;
	}

	[data-plugin-style-id="kubernetes-plugin"] textarea[readonly="readonly"], textarea[readonly] {
		cursor:        auto;
		margin-bottom: 0px;
	}

	[data-plugin-style-id="kubernetes-plugin"] .logs {
		color:            white;
		display:          block;
		height:           calc(100vh - 210px);
		font-size:        13px;
		max-height:       100%;
		font-weight:      400;
		font-family:      monaco;
		padding-left:     10px;
		background-color: #383838;
	}
</style>

<div data-plugin-style-id="kubernetes-plugin">
	<div class="outer-container">
		<div class="container">
			<div class="error-container">
				<div class="title">
					Error while generating status report: ${ message! }
				</div>
				<#if stacktrace??>
					<div class="stacktrace">
						<textarea class="logs" readonly>${stacktrace!}</textarea>
					</div>
                <#else>
					<div class="no-stacktrace">
						Stacktrace not available.
					</div>
                </#if>
			</div>
		</div>
	</div>
</div>
