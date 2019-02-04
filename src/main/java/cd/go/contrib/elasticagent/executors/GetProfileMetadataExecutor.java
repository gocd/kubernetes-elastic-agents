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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagent.model.MemoryMetadata;
import cd.go.contrib.elasticagent.model.Metadata;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

import static cd.go.contrib.elasticagent.utils.Util.GSON;

public class GetProfileMetadataExecutor implements RequestExecutor {
    public static final Metadata IMAGE = new Metadata("Image", false, false);
    public static final Metadata MAX_MEMORY = new MemoryMetadata("MaxMemory", false);
    public static final Metadata MAX_CPU = new Metadata("MaxCPU", false, false);
    public static final Metadata ENVIRONMENT = new Metadata("Environment", false, false);
    public static final Metadata POD_CONFIGURATION = new Metadata("PodConfiguration", false, false);
    public static final Metadata SPECIFIED_USING_POD_CONFIGURATION = new Metadata("SpecifiedUsingPodConfiguration", false, false);
    public static final Metadata POD_SPEC_TYPE = new Metadata("PodSpecType", true, false);
    public static final Metadata REMOTE_FILE = new Metadata("RemoteFile", false, false);
    public static final Metadata REMOTE_FILE_TYPE = new Metadata("RemoteFileType", false, false);
    public static final Metadata PRIVILEGED = new Metadata("Privileged", false, false);
    public static final List<Metadata> FIELDS = new ArrayList<>();

    static {
        FIELDS.add(IMAGE);
        FIELDS.add(MAX_MEMORY);
        FIELDS.add(MAX_CPU);
        FIELDS.add(ENVIRONMENT);
        FIELDS.add(POD_CONFIGURATION);
        FIELDS.add(SPECIFIED_USING_POD_CONFIGURATION);
        FIELDS.add(POD_SPEC_TYPE);
        FIELDS.add(REMOTE_FILE);
        FIELDS.add(REMOTE_FILE_TYPE);
        FIELDS.add(PRIVILEGED);
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        return DefaultGoPluginApiResponse.success(GSON.toJson(FIELDS));
    }
}
