/*
 * Copyright 2017 ThoughtWorks, Inc.
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
    private final String staqeName;

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
        pipelineCounter = null;
        pipelineLabel = null;
        staqeName = null;
        stageCounter = null;
        jobName = null;
        jobId = null;
    }

    public JobIdentifier(Long jobId) {
        this(null, null, null, null, null, null, jobId);
    }

    public JobIdentifier(String pipelineName, Long pipelineCounter, String pipelineLabel, String staqeName, String stageCounter, String jobName, Long jobId) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.pipelineLabel = pipelineLabel;
        this.staqeName = staqeName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobIdentifier)) return false;

        JobIdentifier that = (JobIdentifier) o;

        if (pipelineCounter != that.pipelineCounter) return false;
        if (jobId != that.jobId) return false;
        if (pipelineName != null ? !pipelineName.equals(that.pipelineName) : that.pipelineName != null) return false;
        if (pipelineLabel != null ? !pipelineLabel.equals(that.pipelineLabel) : that.pipelineLabel != null)
            return false;
        if (staqeName != null ? !staqeName.equals(that.staqeName) : that.staqeName != null) return false;
        if (stageCounter != null ? !stageCounter.equals(that.stageCounter) : that.stageCounter != null) return false;
        return jobName != null ? jobName.equals(that.jobName) : that.jobName == null;
    }

    @Override
    public int hashCode() {
        int result = pipelineName != null ? pipelineName.hashCode() : 0;
        result = 31 * result + (int) (pipelineCounter ^ (pipelineCounter >>> 32));
        result = 31 * result + (pipelineLabel != null ? pipelineLabel.hashCode() : 0);
        result = 31 * result + (staqeName != null ? staqeName.hashCode() : 0);
        result = 31 * result + (stageCounter != null ? stageCounter.hashCode() : 0);
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (int) (jobId ^ (jobId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JobIdentifier{" +
                "pipelineName='" + pipelineName + '\'' +
                ", pipelineCounter=" + pipelineCounter +
                ", pipelineLabel='" + pipelineLabel + '\'' +
                ", staqeName='" + staqeName + '\'' +
                ", stageCounter='" + stageCounter + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobId=" + jobId +
                '}';
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String representation() {
        return pipelineName + "/" + pipelineCounter + "/" + staqeName + "/" + stageCounter + "/" + jobName;
    }
}
