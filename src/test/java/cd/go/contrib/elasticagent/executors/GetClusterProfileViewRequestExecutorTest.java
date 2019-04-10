/*
 * Copyright 2019 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagent.model.Metadata;
import cd.go.contrib.elasticagent.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GetClusterProfileViewRequestExecutorTest {
    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertThat(hashSet, Matchers.hasEntry("template", Util.readResource("/plugin-settings.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() {
        String template = Util.readResource("/plugin-settings.template.html");
        final Document document = Jsoup.parse(template);

        for (Metadata field : GetClusterProfileMetadataExecutor.FIELDS) {
            final Elements inputFieldForKey = document.getElementsByAttributeValue("ng-model", field.getKey());
            assertThat(inputFieldForKey, hasSize(1));

            final Elements spanToShowError = document.getElementsByAttributeValue("ng-show", "GOINPUTNAME[" + field.getKey() + "].$error.server");
            assertThat(spanToShowError, hasSize(1));
            assertThat(spanToShowError.attr("ng-show"), is("GOINPUTNAME[" + field.getKey() + "].$error.server"));
            assertThat(spanToShowError.text(), is("{{GOINPUTNAME[" + field.getKey() + "].$error.server}}"));
        }

        final Elements inputs = document.select("textarea,input[type=text],select,input[type=checkbox]");
        assertThat(inputs, hasSize(GetProfileMetadataExecutor.FIELDS.size() - 3)); // do not include SPECIFIED_USING_POD_CONFIGURATION, POD_SPEC_TYPE, REMOTE_FILE_TYPE key
    }
}
