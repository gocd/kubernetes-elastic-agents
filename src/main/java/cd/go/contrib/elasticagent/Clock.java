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

package cd.go.contrib.elasticagent;

import org.joda.time.DateTime;
import org.joda.time.Period;

public interface Clock {
    Clock DEFAULT = new Clock() {
        @Override
        public DateTime now() {
            return new DateTime();
        }
    };

    DateTime now();

    class TestClock implements Clock {

        DateTime time = null;

        public TestClock(DateTime time) {
            this.time = time;
        }

        public TestClock() {
            this(new DateTime());
        }

        @Override
        public DateTime now() {
            return time;
        }

        public TestClock reset() {
            time = new DateTime();
            return this;
        }

        public TestClock set(DateTime time) {
            this.time = time;
            return this;
        }

        public TestClock rewind(Period period) {
            this.time = this.time.minus(period);
            return this;
        }

        public TestClock forward(Period period) {
            this.time = this.time.plus(period);
            return this;
        }
    }
}
