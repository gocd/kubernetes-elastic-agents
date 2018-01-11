<style>
    [data-plugin-style-id="kubernetes-plugin"] .kubernetes-elastic-agent {
        padding: 60px 20px 0 20px;
    }

    [data-plugin-style-id="kubernetes-plugin"] .header {
        padding-left: 25px;
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
        background: #fff;
        margin-top: 0;
        margin: 20px;
        height: calc(100vh - 280px);
        overflow-x: hidden;
    }

</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="row">
        <div class="header-panel">
            <header class="page-header header">
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
                    <li ng-class="{'current_tab' : currenttab == 'pod-logs' }" ng-click="currenttab='pod-logs'">
                        <a href="#">Logs</a>
                    </li>
                </ul>
            </div>

            <div class="tab-content" ng-show="currenttab == 'pod-details'"></div>
            <div class="tab-content pod-logs"
                 ng-show="currenttab == 'pod-logs'">${ logs?html?replace("\n", "<br>") !} </div>
        </div>
    </div>

</div>


