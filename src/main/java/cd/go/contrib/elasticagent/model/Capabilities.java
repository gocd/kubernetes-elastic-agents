/*
 * Copyright 2018 ThoughtWorks, Inc.
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

public class Capabilities {
    @Expose
    @SerializedName("supports_status_report")
    private boolean supportsStatusReport;

    @Expose
    @SerializedName("supports_agent_status_report")
    private boolean supportsAgentStatusReport;

    public Capabilities(boolean supportsStatusReport, boolean supportsAgentStatusReport) {
        this.supportsStatusReport = supportsStatusReport;
        this.supportsAgentStatusReport = supportsAgentStatusReport;
    }

    public static Capabilities fromJSON(String json) {
        return GSON.fromJson(json, Capabilities.class);
    }

    public String toJSON() {
        return GSON.toJson(this);
    }
}
