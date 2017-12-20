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

package cd.go.contrib.elasticagent.requests;

import cd.go.contrib.elasticagent.AgentInstances;
import cd.go.contrib.elasticagent.Constants;
import cd.go.contrib.elasticagent.PluginRequest;
import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.executors.CreateAgentRequestExecutor;
import cd.go.contrib.elasticagent.executors.StatusReportExecutor;
import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.fabric8.kubernetes.api.model.EnvVar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static cd.go.contrib.elasticagent.utils.Util.GSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class StatusReportRequest {
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    public StatusReportRequest() {
    }

    public StatusReportRequest(JobIdentifier identifier) {
        this.jobIdentifier = identifier;
    }

    public static StatusReportRequest fromJSON(String json) {
        StatusReportRequest statusReportRequest = GSON.fromJson(json, StatusReportRequest.class);
        return statusReportRequest;
    }


    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public RequestExecutor executor(PluginRequest pluginRequest) throws IOException {
        return new StatusReportExecutor(this, pluginRequest);
    }

    @Override
    public String toString() {
        return "StatusReportRequest{" +
                "jobIdentifier=" + jobIdentifier +
                '}';
    }
}
