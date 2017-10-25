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

package cd.go.contrib.elasticagent.executors;

import cd.go.contrib.elasticagent.model.MemoryMetadata;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class MemoryMetadataTest {

    @Test
    public void shouldValidateMemoryBytes() throws Exception {
        assertTrue(new MemoryMetadata("Disk", false).validate("100mb").isEmpty());

        Map<String, String> validate = new MemoryMetadata("Disk", false).validate("xxx");
        assertThat(validate.size(), is(2));
        assertThat(validate, hasEntry("message", "Invalid size: xxx"));
        assertThat(validate, hasEntry("key", "Disk"));
    }

    @Test
    public void shouldValidateMemoryBytesWhenRequireField() throws Exception {
        Map<String, String> validate = new MemoryMetadata("Disk", true).validate(null);
        assertThat(validate.size(), is(2));
        assertThat(validate, hasEntry("message", "Disk must not be blank."));
        assertThat(validate, hasEntry("key", "Disk"));
    }
}