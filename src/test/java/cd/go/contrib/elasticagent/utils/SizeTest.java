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


import io.fabric8.kubernetes.api.model.Quantity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SizeTest {

    @Test
    public void shouldParseTheGivenSize() {
        Size parse = Size.parse("24000ki");
        assertEquals(parse.getUnit(), SizeUnit.KILOBYTES);
        assertEquals(parse.getQuantity(), 24000, 0);
    }

    @Test
    public void shouldThrowExceptionIfTheGivenSizeHasDifferentSuffix() {
        assertThrows(IllegalArgumentException.class, () -> Size.parse("24000kig"));
    }

    @Test
    public void shouldThrowExceptionIfTheGivenSizeHasNoUint() {
        assertThrows(IllegalArgumentException.class, () -> Size.parse("24000"));
    }

    @Test
    public void shouldThrowExceptionIfTheGivenSizeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Size.parse(null));
    }

    @Test
    public void shouldThrowExceptionIfTheGivenSizeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> Size.parse(""));
    }

    @Test
    public void fromQuantity() {
        assertEquals("58 GB", Size.fromQuantity(Quantity.parse("58Gi")).readableSize());
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
