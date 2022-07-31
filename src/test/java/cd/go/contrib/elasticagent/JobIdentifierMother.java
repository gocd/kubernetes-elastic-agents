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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.model.JobIdentifier;
import com.google.gson.JsonObject;

public class JobIdentifierMother {
    public static JsonObject getJson() {
        JsonObject jobIdentifierJson = new JsonObject();
        jobIdentifierJson.addProperty("pipeline_name", "up42");
        jobIdentifierJson.addProperty("pipeline_counter", 1);
        jobIdentifierJson.addProperty("pipeline_label", "label");
        jobIdentifierJson.addProperty("stage_name", "stage");
        jobIdentifierJson.addProperty("stage_counter", "1");
        jobIdentifierJson.addProperty("job_name", "job1");
        jobIdentifierJson.addProperty("job_id", 1);
        return jobIdentifierJson;
    }

    public static JobIdentifier get() {
        return new JobIdentifier("up42", 1L, "label", "stage", "1", "job1", 1L);
    }
}
