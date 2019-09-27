package cd.go.contrib.elasticagent.model.reports.agent;

public class GoCDContainerLog {
    private String name;
    private String content;

    public GoCDContainerLog(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
