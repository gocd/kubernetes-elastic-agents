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

package cd.go.contrib.elasticagent.reports;

import cd.go.contrib.elasticagent.builders.PluginStatusReportViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import static cd.go.contrib.elasticagent.KubernetesPlugin.LOG;
import static java.text.MessageFormat.format;

public class StatusReportGenerationErrorHandler {

    public static GoPluginApiResponse handle(PluginStatusReportViewBuilder builder, Exception e) {
        try {
            LOG.error(format("Error while generating status report: {0}", e.getMessage()), e);
            final Template template = builder.getTemplate("error.template.ftlh");
            final String errorView = builder.build(template, new StatusReportGenerationError(e));

            final JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", errorView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception ex) {
            LOG.error(format("Failed to generate error report: {0}", e.getMessage()), e);
            return DefaultGoPluginApiResponse.error(format("Failed to generate error report: {0}", e.toString()));
        }
    }

}
