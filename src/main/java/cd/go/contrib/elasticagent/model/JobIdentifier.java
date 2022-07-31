/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagent.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static java.text.MessageFormat.format;

public class JobIdentifier {
    @Expose
    @SerializedName("pipeline_name")
    private String pipelineName;

    @Expose
    @SerializedName("pipeline_counter")
    private final Long pipelineCounter;

    @Expose
    @SerializedName("pipeline_label")
    private final String pipelineLabel;

    @Expose
    @SerializedName("stage_name")
    private final String stageName;

    @Expose
    @SerializedName("stage_counter")
    private final String stageCounter;

    @Expose
    @SerializedName("job_name")
    private final String jobName;

    @Expose
    @SerializedName("job_id")
    private final Long jobId;

    public JobIdentifier() {
        this(null, null, null, null, null, null, null);
    }

    public JobIdentifier(Long jobId) {
        this(null, null, null, null, null, null, jobId);
    }

    public JobIdentifier(String pipelineName, Long pipelineCounter, String pipelineLabel, String stageName, String stageCounter, String jobName, Long jobId) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.pipelineLabel = pipelineLabel;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public Long getPipelineCounter() {
        return pipelineCounter;
    }

    public String getPipelineLabel() {
        return pipelineLabel;
    }

    public String getStageName() {
        return stageName;
    }

    public String getStageCounter() {
        return stageCounter;
    }

    public String getJobName() {
        return jobName;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getRepresentation() {
        return String.format("%s/%s/%s/%s/%s", pipelineName, pipelineCounter, stageName, stageCounter, jobName);
    }

    public String getPipelineHistoryPageLink() {
        return String.format("/go/tab/pipeline/history/%s", pipelineName);
    }

    public String getVsmPageLink() {
        return String.format("/go/pipelines/value_stream_map/%s/%s", pipelineName, pipelineCounter);
    }

    public String getStageDetailsPageLink() {
        return String.format("/go/pipelines/%s/%s/%s/%s", pipelineName, pipelineCounter, stageName, stageCounter);
    }

    public String getJobDetailsPageLink() {
        return String.format("/go/tab/build/detail/%s", getRepresentation());
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return format("JobIdentifier'{'pipelineName=''{0}'', pipelineCounter={1}, pipelineLabel=''{2}'', stageName=''{3}'', stageCounter=''{4}'', jobName=''{5}'', jobId={6}'}'", pipelineName, pipelineCounter, pipelineLabel, stageName, stageCounter, jobName, jobId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobIdentifier)) return false;

        JobIdentifier that = (JobIdentifier) o;

        if (pipelineName != null ? !pipelineName.equals(that.pipelineName) : that.pipelineName != null) return false;
        if (pipelineCounter != null ? !pipelineCounter.equals(that.pipelineCounter) : that.pipelineCounter != null)
            return false;
        if (pipelineLabel != null ? !pipelineLabel.equals(that.pipelineLabel) : that.pipelineLabel != null)
            return false;
        if (stageName != null ? !stageName.equals(that.stageName) : that.stageName != null) return false;
        if (stageCounter != null ? !stageCounter.equals(that.stageCounter) : that.stageCounter != null) return false;
        if (jobName != null ? !jobName.equals(that.jobName) : that.jobName != null) return false;
        return jobId != null ? jobId.equals(that.jobId) : that.jobId == null;
    }

    @Override
    public int hashCode() {
        int result = pipelineName != null ? pipelineName.hashCode() : 0;
        result = 31 * result + (pipelineCounter != null ? pipelineCounter.hashCode() : 0);
        result = 31 * result + (pipelineLabel != null ? pipelineLabel.hashCode() : 0);
        result = 31 * result + (stageName != null ? stageName.hashCode() : 0);
        result = 31 * result + (stageCounter != null ? stageCounter.hashCode() : 0);
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        return result;
    }

    public static JobIdentifier fromJson(String json) {
        return GSON.fromJson(json, JobIdentifier.class);
    }

}
