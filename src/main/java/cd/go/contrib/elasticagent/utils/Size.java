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
import io.fabric8.kubernetes.api.model.Quantity;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
            .put("Ki", SizeUnit.KILOBYTES)
            .put("KiB", SizeUnit.KILOBYTES)
            .put("kilobyte", SizeUnit.KILOBYTES)
            .put("kilobytes", SizeUnit.KILOBYTES)
            .put("M", SizeUnit.MEGABYTES)
            .put("Mi", SizeUnit.MEGABYTES)
            .put("MB", SizeUnit.MEGABYTES)
            .put("MiB", SizeUnit.MEGABYTES)
            .put("megabyte", SizeUnit.MEGABYTES)
            .put("megabytes", SizeUnit.MEGABYTES)
            .put("G", SizeUnit.GIGABYTES)
            .put("Gi", SizeUnit.GIGABYTES)
            .put("GB", SizeUnit.GIGABYTES)
            .put("GiB", SizeUnit.GIGABYTES)
            .put("gigabyte", SizeUnit.GIGABYTES)
            .put("gigabytes", SizeUnit.GIGABYTES)
            .put("T", SizeUnit.TERABYTES)
            .put("TB", SizeUnit.TERABYTES)
            .put("Ti", SizeUnit.TERABYTES)
            .put("TiB", SizeUnit.TERABYTES)
            .put("terabyte", SizeUnit.TERABYTES)
            .put("terabytes", SizeUnit.TERABYTES)
            .build();
    private final double count;
    private final SizeUnit unit;

    private Size(double count, SizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    public static Size bytes(double count) {
        return new Size(count, SizeUnit.BYTES);
    }

    public static Size kilobytes(double count) {
        return new Size(count, SizeUnit.KILOBYTES);
    }

    public static Size megabytes(double count) {
        return new Size(count, SizeUnit.MEGABYTES);
    }

    public static Size gigabytes(double count) {
        return new Size(count, SizeUnit.GIGABYTES);
    }

    public static Size terabytes(double count) {
        return new Size(count, SizeUnit.TERABYTES);
    }

    public static Size parse(String size) {
        if (StringUtils.isBlank(size)) {
            throw new IllegalArgumentException();
        }
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        checkArgument(matcher.matches(), "Invalid size: " + size);

        final double count = Double.parseDouble(matcher.group(1));
        final SizeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new Size(count, unit);
    }

    public static Size fromQuantity(Quantity quantity) {
        return bytes(Quantity.getAmountInBytes(quantity).doubleValue());
    }

    public double getQuantity() {
        return count;
    }

    public SizeUnit getUnit() {
        return unit;
    }

    public double toBytes() {
        return SizeUnit.BYTES.convert(count, unit);
    }

    public double toKilobytes() {
        return SizeUnit.KILOBYTES.convert(count, unit);
    }

    public double toMegabytes() {
        return SizeUnit.MEGABYTES.convert(count, unit);
    }

    public double toGigabytes() {
        return SizeUnit.GIGABYTES.convert(count, unit);
    }

    public double toTerabytes() {
        return SizeUnit.TERABYTES.convert(count, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Size size = (Size) obj;
        return Double.compare(size.count, count) == 0 &&
                unit == size.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, unit);
    }

    @Override
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Double.toString(count) + ' ' + units;
    }

    @Override
    public int compareTo(Size other) {
        if (unit == other.unit) {
            return Double.compare(count, other.count);
        }

        return Double.compare(toBytes(), other.toBytes());
    }

    public String readableSize() {
        double size = this.unit.toBytes(this.count);

        if (size <= 0) return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
