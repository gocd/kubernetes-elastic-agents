package cd.go.contrib.elasticagent.model.reports.agent;

public class KubernetesPodEvent {
    private final String firstTimestamp;
    private final String lastTimestamp;
    private final Integer count;
    private final String fieldPath;
    private final String type;
    private final String reason;
    private final String message;

    public KubernetesPodEvent(String firstTimestamp, String lastTimestamp, Integer count, String fieldPath, String type, String reason, String message) {
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        this.count = count;
        this.fieldPath = fieldPath;
        this.type = type;
        this.reason = reason;
        this.message = message;
    }

    public String getFirstTimestamp() {
        return firstTimestamp;
    }

    public String getLastTimestamp() {
        return lastTimestamp;
    }

    public Integer getCount() {
        return count;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}
