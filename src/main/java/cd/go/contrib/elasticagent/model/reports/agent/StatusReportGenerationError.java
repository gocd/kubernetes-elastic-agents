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

package cd.go.contrib.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagent.model.reports.StatusReportGenerationException;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReportGenerationError {
    private String stacktrace;
    private String message;

    public StatusReportGenerationError(Throwable throwable) {
        if (throwable instanceof StatusReportGenerationException) {
            this.message = throwable.getMessage();
            this.stacktrace = ((StatusReportGenerationException) throwable).getDetailedMessage();
        } else {
            this.stacktrace = initStacktrace(throwable);
            this.message = initMessage(throwable);
        }
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public String getMessage() {
        return message;
    }

    private String initMessage(Throwable throwable) {
        if (StringUtils.isNotBlank(throwable.getMessage())) {
            return throwable.getMessage();
        }

        if (StringUtils.isNotBlank(this.stacktrace)) {
            return stacktrace.split("\n")[0];
        }

        return null;
    }

    private String initStacktrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            String stacktrace = sw.toString();
            return stacktrace;
        } finally {
            closeQuietly(sw);
            closeQuietly(pw);
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            //Ignore
        }
    }
}
