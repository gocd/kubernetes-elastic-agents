<style>
	[data-plugin-style-id="ea-plugin"] .outer-container {
		display:  table;
		position: absolute;
		height:   calc(100vh - 119px);
		width:    100%;
	}

	[data-plugin-style-id="ea-plugin"] .container {
		display: table-cell;
	}

	[data-plugin-style-id="ea-plugin"] .error-container {
		width:         100%;
		display:       table;
		margin:        15px 0px;
		table-layout:  fixed;
		border-radius: 3px;
	}

	[data-plugin-style-id="ea-plugin"] blockquote {
		color:       #555;
		border:      1px dashed #ddd;
		padding:     10px;
		font-size:   13px;
		border-left: 4px solid red;
		margin-top:  10px;
		font-weight: normal;
	}

	[data-plugin-style-id="ea-plugin"] blockquote header {
		color:       #a94442;
		font-size:   15px;
		font-weight: 600;
	}

	[data-plugin-style-id="ea-plugin"] blockquote p {
		margin-top: 10px;
	}
</style>

<div data-plugin-style-id="ea-plugin">
	<div class="outer-container">
		<div class="container">
			<div class="error-container">
				<blockquote>
					<header>${ message!'' }</header>
					<p>${description!''}</p>
				</blockquote>
			</div>
		</div>
	</div>
</div>