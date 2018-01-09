<style>
    [data-plugin-style-id="kubernetes-plugin"] .cluster {
        padding: 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node {
        border: 1px solid #d1c4e9;
        width: 100%;
        background: #fff;
        border-radius: 2px;
        margin-bottom: 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header {
        background: #d1c4e9;
        padding: 10px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header .left {
        float: left;
        padding: 0px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header .right {
        float: right;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header div:first-child {
        padding-left: 0px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header span {
        display: inline-block;
        width: 15%;
        font-size: 17px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-content {
        padding: 10px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node table {
        width: calc(100% - 40px);
        min-width: 95%;
        border: 1px solid #D8D8D8;
        font-size: 13px;
        margin: 10px 20px 20px 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node table thead {
        background: #D8D8D8;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node table thead th {
        padding: 10px 20px;
        font-weight: bold;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node table tbody tr {
        border-top: 1px solid #D8D8D8;
        border-bottom: 1px solid #D8D8D8;
        border-collapse: collapse;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node table tbody td {
        padding: 10px 20px;
        max-width: 190px;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties {
        overflow: hidden;
        margin: 0;
        font-size: 13px;
        display: inline-block;
        vertical-align: top;
    }

    [data-plugin-style-id="kubernetes-plugin"] dt {
        float: left;
        clear: both;
        min-width: 230px;
        text-align: right;
        font-weight: normal;
        padding: 5px 0px;
        margin: 0;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties.inline dt {
        clear: none;
        min-width: auto;
        vertical-align: bottom;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties.inline dd, [data-plugin-style-id="kubernetes-plugin"] dl.properties.inline dt {
        padding: 0px;
        height: 15px;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties.inline dd {
        margin-right: 15px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node-header .hostname, [data-plugin-style-id="kubernetes-plugin"] .node-header .node-id {
        min-width: 150px;
        max-width: 150px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    [data-plugin-style-id="kubernetes-plugin"] .name {
        min-width: 300px;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    [data-plugin-style-id="kubernetes-plugin"] dl.properties.inline dt:first-child {
        min-width: auto;
        margin-left: 10px;
    }

    [data-plugin-style-id="kubernetes-plugin"] dt:after {
        content: ":";
        padding: 0px 5px 0px 2px;
    }

    [data-plugin-style-id="kubernetes-plugin"] dd {
        float: left;
        padding: 5px 0px;
        font-weight: 600;
        margin: 0;
    }

    [data-plugin-style-id="kubernetes-plugin"] .node .header {
        margin: 20px 0px 0px 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .warning {
        font-size: 13px;
        font-weight: 600;
        margin-bottom: 10px;
        border-left: 2px solid #f7bc08;
        padding: 10px;
        background: #fff;
        border-radius: 2px;
    }
</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="kubernetes-elastic-agent">
        this is kubernetes elastic agent plugin
    </div>
</div>


