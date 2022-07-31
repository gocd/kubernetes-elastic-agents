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
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginRequestTest {
    @Test
    public void shouldNotThrowAnExceptionIfConsoleLogAppenderCallFails() {
        final JobIdentifier jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);

        GoApplicationAccessor accessor = mock(GoApplicationAccessor.class);
        when(accessor.submit(any())).thenReturn(DefaultGoApiResponse.badRequest("Something went wrong"));

        final PluginRequest pluginRequest = new PluginRequest(accessor);
        pluginRequest.appendToConsoleLog(jobIdentifier, "text1");
    }
}