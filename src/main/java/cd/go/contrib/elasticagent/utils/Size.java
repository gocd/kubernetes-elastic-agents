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

import com.google.common.collect.ImmutableSortedMap;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Size implements Comparable<Size> {
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

    private static final Map<String, SizeUnit> SUFFIXES = ImmutableSortedMap.<String, SizeUnit>orderedBy(String.CASE_INSENSITIVE_ORDER)
            .put("B", SizeUnit.BYTES)
            .put("byte", SizeUnit.BYTES)
            .put("bytes", SizeUnit.BYTES)
            .put("K", SizeUnit.KILOBYTES)
            .put("KB", SizeUnit.KILOBYTES)
            .put("KiB", SizeUnit.KILOBYTES)
            .put("kilobyte", SizeUnit.KILOBYTES)
            .put("kilobytes", SizeUnit.KILOBYTES)
            .put("M", SizeUnit.MEGABYTES)
            .put("MB", SizeUnit.MEGABYTES)
            .put("MiB", SizeUnit.MEGABYTES)
            .put("megabyte", SizeUnit.MEGABYTES)
            .put("megabytes", SizeUnit.MEGABYTES)
            .put("G", SizeUnit.GIGABYTES)
            .put("GB", SizeUnit.GIGABYTES)
            .put("GiB", SizeUnit.GIGABYTES)
            .put("gigabyte", SizeUnit.GIGABYTES)
            .put("gigabytes", SizeUnit.GIGABYTES)
            .put("T", SizeUnit.TERABYTES)
            .put("TB", SizeUnit.TERABYTES)
            .put("TiB", SizeUnit.TERABYTES)
            .put("terabyte", SizeUnit.TERABYTES)
            .put("terabytes", SizeUnit.TERABYTES)
            .build();
    private final long count;
    private final SizeUnit unit;

    private Size(long count, SizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    public static Size bytes(long count) {
        return new Size(count, SizeUnit.BYTES);
    }

    public static Size kilobytes(long count) {
        return new Size(count, SizeUnit.KILOBYTES);
    }

    public static Size megabytes(long count) {
        return new Size(count, SizeUnit.MEGABYTES);
    }

    public static Size gigabytes(long count) {
        return new Size(count, SizeUnit.GIGABYTES);
    }

    public static Size terabytes(long count) {
        return new Size(count, SizeUnit.TERABYTES);
    }

    public static Size parse(String size) {
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        checkArgument(matcher.matches(), "Invalid size: " + size);

        final long count = Long.parseLong(matcher.group(1));
        final SizeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new Size(count, unit);
    }

    public long getQuantity() {
        return count;
    }

    public SizeUnit getUnit() {
        return unit;
    }

    public long toBytes() {
        return SizeUnit.BYTES.convert(count, unit);
    }

    public long toKilobytes() {
        return SizeUnit.KILOBYTES.convert(count, unit);
    }

    public long toMegabytes() {
        return SizeUnit.MEGABYTES.convert(count, unit);
    }

    public long toGigabytes() {
        return SizeUnit.GIGABYTES.convert(count, unit);
    }

    public long toTerabytes() {
        return SizeUnit.TERABYTES.convert(count, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final Size size = (Size) obj;
        return (count == size.count) && (unit == size.unit);
    }

    @Override
    public int hashCode() {
        return (31 * (int) (count ^ (count >>> 32))) + unit.hashCode();
    }

    @Override
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    @Override
    public int compareTo(Size other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toBytes(), other.toBytes());
    }
}
