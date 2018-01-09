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

package cd.go.contrib.elasticagent.builders;

import cd.go.contrib.elasticagent.model.KubernetesCluster;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class PluginStatusReportViewBuilder {
    private static PluginStatusReportViewBuilder builder;
    private final Configuration configuration;

    private PluginStatusReportViewBuilder() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setDateTimeFormat("iso");
    }

    public Template getTemplate(String template) throws IOException {
        return configuration.getTemplate(template);
    }

    public String build(Template template, Object cluster) throws IOException, TemplateException {
        Writer writer = new StringWriter();
        template.process(cluster, writer);
        return writer.toString();
    }

    public static PluginStatusReportViewBuilder instance() {
        if (builder == null) {
            builder = new PluginStatusReportViewBuilder();
        }
        return builder;
    }
}
