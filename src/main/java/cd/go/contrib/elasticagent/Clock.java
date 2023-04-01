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

import java.time.Instant;

public interface Clock {
    Clock DEFAULT = Instant::now;

    Instant now();

    class TestClock implements Clock {

        Instant time = null;

        public TestClock(Instant time) {
            this.time = time;
        }

        public TestClock() {
            this(Instant.now());
        }

        @Override
        public Instant now() {
            return time;
        }

        public TestClock set(Instant time) {
            this.time = time;
            return this;
        }
    }
}
