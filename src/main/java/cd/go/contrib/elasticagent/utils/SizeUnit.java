/*
 * Copyright 2010-2013 Coda Hale and Yammer, Inc., 2014-2016 Dropwizard Team
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

public enum SizeUnit {
    /**
     * Bytes.
     */
    BYTES(8),

    /**
     * Kilobytes.
     */
    KILOBYTES(8L * 1024),

    /**
     * Megabytes.
     */
    MEGABYTES(8L * 1024 * 1024),

    /**
     * Gigabytes.
     */
    GIGABYTES(8L * 1024 * 1024 * 1024),

    /**
     * Terabytes.
     */
    TERABYTES(8L * 1024 * 1024 * 1024 * 1024);

    private final long bits;

    SizeUnit(long bits) {
        this.bits = bits;
    }

    /**
     * Converts a size of the given unit into the current unit.
     *
     * @param size the magnitude of the size
     * @param unit the unit of the size
     * @return the given size in the current unit.
     */
    public double convert(double size, SizeUnit unit) {
        return (size * unit.bits) / bits;
    }

    /**
     * Converts the given number of the current units into bytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in bytes
     */
    public double toBytes(double l) {
        return BYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into kilobytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in kilobytes
     */
    public double toKilobytes(double l) {
        return KILOBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into megabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in megabytes
     */
    public double toMegabytes(double l) {
        return MEGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into gigabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in bytes
     */
    public double toGigabytes(double l) {
        return GIGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into terabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in terabytes
     */
    public double toTerabytes(double l) {
        return TERABYTES.convert(l, this);
    }
}
