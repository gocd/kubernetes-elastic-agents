/*
 * Copyright 2023 ThoughtWorks, Inc.
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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesInstanceTest {
    @Test
    void podAnnotationsDefaultToEmptyMap() {
        KubernetesInstance instance1 = KubernetesInstance.builder()
                .podName("test")
                .environment("test")
                .jobId(1L)
                .build();
        assertEquals(Collections.emptyMap(), instance1.getPodAnnotations());
    }

    @Test
    void podAnnotationsSafelyHandleNull() {
        KubernetesInstance instance1 = KubernetesInstance.builder()
                .podName("test")
                .environment("test")
                .podAnnotations(null)
                .jobId(1L)
                .build();
        assertEquals(Collections.emptyMap(), instance1.getPodAnnotations());
    }

    @Test
    void podAnnotationsAreCopied() {
        Map<String, String> annotations = new HashMap<>();
        annotations.put("key1", "value1");
        KubernetesInstance instance1 = KubernetesInstance.builder()
                .podName("test")
                .environment("test")
                .jobId(1L)
                .podAnnotations(annotations)
                .build();
        annotations.put("key2", "value2");
        assertEquals(Map.of("key1", "value1"), instance1.getPodAnnotations());
    }
}
