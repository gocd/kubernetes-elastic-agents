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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValidatePluginSettingsRequestTest {

    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        String json = "{\n" +
                "  \"plugin-settings\": {\n" +
                "    \"server_url\": {\n" +
                "      \"value\": \"http://localhost\"\n" +
                "    },\n" +
                "    \"username\": {\n" +
                "      \"value\": \"bob\"\n" +
                "    },\n" +
                "    \"password\": {\n" +
                "      \"value\": \"secret\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        ValidatePluginSettingsRequest request = ValidatePluginSettingsRequest.fromJSON(json);

        assertThat(request.get("server_url"), is("http://localhost"));
        assertThat(request.get("username"), is("bob"));
        assertThat(request.get("password"), is("secret"));

    }
}
