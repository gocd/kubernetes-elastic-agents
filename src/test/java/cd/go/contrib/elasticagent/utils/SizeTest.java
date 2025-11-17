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

package cd.go.contrib.elasticagent.utils;


import io.fabric8.kubernetes.api.model.Quantity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SizeTest {

    @Test
    public void shouldParseTheGivenSize() {
        Size parse = Size.parse("24000ki");
        assertEquals(SizeUnit.KILOBYTES, parse.getUnit());
        assertEquals(24000, parse.getQuantity(), 0);
    }

    @Test
    public void shouldThrowExceptionIfTheGivenSizeHasDifferentSuffix() {
        assertThrows(IllegalArgumentException.class, () -> Size.parse("24000kig"));
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
    public void defaultSizeUnitIsBytes() {
        Size oneKB = Size.parse("1024");
        assertEquals(1024, oneKB.toBytes(), 0);
    }

    @Test
    public void toBytes() {
        Size oneKB = Size.parse("1Ki");
        assertEquals(1024, oneKB.toBytes(), 0);
    }

    @Test
    public void toKilobytes() {
        Size size = Size.parse("12000B");
        assertEquals(11.72, size.toKilobytes(), 0.01);
    }

    @Test
    public void toMegabytes() {
        Size size = Size.parse("12000KiB");
        assertEquals(11.72, size.toMegabytes(), 0.01);
    }

    @Test
    public void toGigabytes() {
        Size size = Size.parse("1200000KiB");
        assertEquals(1.14, size.toGigabytes(), 0.01);
    }

    @Test
    public void toTerabytes() {
        Size size = Size.parse("1456823212Mi");
        assertEquals(1389.33, size.toTerabytes(), 0.01);
    }

    @Test
    public void readableSize() {
        Size size = Size.parse("1024Mi");
        Size sizeInKB = Size.parse("10256KiB");
        assertEquals("1 GB", size.readableSize());
        assertEquals("10.02 MB", sizeInKB.readableSize());
    }
}
