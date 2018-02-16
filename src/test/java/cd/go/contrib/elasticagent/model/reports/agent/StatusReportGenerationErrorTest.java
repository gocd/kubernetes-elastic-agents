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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class StatusReportGenerationErrorTest {

    @Test
    public void shouldConvertThrowableToStatusReportGenerationErrorObject() {
        final StatusReportGenerationError statusReportGenerationError = new StatusReportGenerationError(new RuntimeException("Some error."));

        assertThat(statusReportGenerationError.getMessage(), is("Some error."));
        assertThat(statusReportGenerationError.getStacktrace(), startsWith("java.lang.RuntimeException: Some error.\n" +
                "\tat cd.go.contrib.elasticagent.model.reports.agent.StatusReportGenerationErrorTest.shouldConvertThrowableToStatusReportGenerationErrorObject"));
    }
}