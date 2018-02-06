<style>
    [data-plugin-style-id="kubernetes-plugin"] .outer-container {
        display: table;
        position: absolute;
        height: calc(100vh - 119px);
        width: 100%;
    }

    [data-plugin-style-id="kubernetes-plugin"] .container {
        display: table-cell;
        vertical-align: middle;
    }

    [data-plugin-style-id="kubernetes-plugin"] .message {
        margin-left: auto;
        margin-right: auto;

        background: #FFF;
        padding: 20px;
        color: #991A00;
        font-size: large;
        text-align: center;
        vertical-align: middle;
    }
</style>

<div data-plugin-style-id="kubernetes-plugin">
    <div class="outer-container">
        <div class="container">
            <div class="message">
            ${ message! }
            </div>
        </div>
    </div>
</div>
