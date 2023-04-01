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

import cd.go.contrib.elasticagent.PluginSettingsNotConfiguredException;

import static cd.go.contrib.elasticagent.utils.Util.isBlank;

public class StatusReportGenerationError {
    private static final String DEFAULT_ERROR_MESSAGE = "We're sorry, but something went wrong.";
    private String message;
    private String description;

    public StatusReportGenerationError(Throwable throwable) {
        this.message = getOrDefaultMessage(throwable);

        if (throwable instanceof StatusReportGenerationException) {
            this.description = ((StatusReportGenerationException) throwable).getDetailedMessage();
        } else if (throwable instanceof PluginSettingsNotConfiguredException) {
            this.description = "Please configure the plugin settings properly before using the plugin.";
        }

        if (isBlank(this.description)) {
            this.description = "Please check the plugin logs for more information.";
        }
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    private String getOrDefaultMessage(Throwable throwable) {
        if (!isBlank(throwable.getMessage())) {
            return throwable.getMessage();
        }

        return DEFAULT_ERROR_MESSAGE;
    }
}
