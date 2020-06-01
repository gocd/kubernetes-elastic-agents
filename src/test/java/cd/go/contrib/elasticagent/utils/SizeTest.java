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

package cd.go.contrib.elasticagent.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SizeTest {

    @Test
    public void shouldParseTheGivenSize() {
        Size parse = Size.parse("24000ki");
        assertEquals(parse.getUnit(), SizeUnit.KILOBYTES);
        assertEquals(parse.getQuantity(), 24000, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTheGivenSizeHasDifferentSuffix() {
        Size parse = Size.parse("24000kig");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTheGivenSizeHasNoUint() {
        Size parse = Size.parse("24000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTheGivenSizeIsNull() {
        Size.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTheGivenSizeIsBlank() {
        Size.parse("");
    }

    @Test
    public void toBytes() {
        Size oneKB = Size.parse("1Ki");
        assertEquals(oneKB.toBytes(), 1024, 0);
    }

    @Test
    public void toKilobytes() {
        Size size = Size.parse("12000B");
        assertEquals(size.toKilobytes(), 11.72, 0.01);
    }

    @Test
    public void toMegabytes() {
        Size size = Size.parse("12000KiB");
        assertEquals(size.toMegabytes(), 11.72, 0.01);
    }

    @Test
    public void toGigabytes() {
        Size size = Size.parse("1200000KiB");
        assertEquals(size.toGigabytes(), 1.14, 0.01);
    }

    @Test
    public void toTerabytes() {
        Size size = Size.parse("1456823212Mi");
        assertEquals(size.toTerabytes(), 1389.33, 0.01);
    }

    @Test
    public void readableSize() {
        Size size = Size.parse("1024Mi");
        Size sizeInKB = Size.parse("10256KiB");
        assertEquals(size.readableSize(), "1 GB");
        assertEquals(sizeInKB.readableSize(), "10.02 MB");
    }
}
