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
                <label for="pod-details">Tab 1</label>
                <dl class="content">
                    This is the content of tab 1
                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="checkbox2" class="checkboxtab">
                <label for="checkbox2">Tab 2</label>
                <dl class="content">
                    This is the content of tab 2
                </dl>
            </div>

            <div class="tab">
                <input name="agent-information" type="radio" id="checkbox3" class="checkboxtab">
                <label for="checkbox3">Tab 3</label>
                <dl class="content">
                    This is the content of tab 3
                </dl>
            </div>
        </div>
    </div>
</div>


