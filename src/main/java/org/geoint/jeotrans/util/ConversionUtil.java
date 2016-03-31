/*
 *  The MIT License
 *
 *  Copyright 2010 Steve Siebert.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.geoint.jeotrans.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.jscience.geography.coordinates.LatLong;

/**
 *
 * @author Steve Siebert
 */
public class ConversionUtil {

    private static final double NORTH_POLAR_MIN = 84.0;
    private static final double SOUTH_POLAR_MIN = -80.0;

    /**
     * Convert latitude value to latitude zone character. This methods expects
     * the latitude to be non-polar
     *
     * @param latitude
     * @param unit
     * @return
     */
    public static char getLatitudeZone(double latitude, Unit<Angle> unit) {
        if (unit != NonSI.DEGREE_ANGLE) {
            UnitConverter toDegree = unit.getConverterTo(NonSI.DEGREE_ANGLE);
            latitude = toDegree.convert(latitude);
        }
        int degreesLatitude = (int) latitude;
        if (degreesLatitude > NORTH_POLAR_MIN || degreesLatitude < SOUTH_POLAR_MIN) {
            throw new ConversionException("Unable to determine latitude"
                    + " zone, polar regions require longitude value to "
                    + "determine code.");
        }
        char zone = (char) ((degreesLatitude + 80) / 8 + 'C');
        if (zone > 'H') {
            zone++;
        }
        if (zone > 'N') {
            zone++;
        }
        if (zone > 'X') {
            zone = 'X';
        }
        return zone;
    }

    /**
     * Returns the UTM/UPS latitude zone identifier for the specified
     * coordinates.
     *
     * @param latLong The coordinates.
     * @return the latitude zone character.
     */
    public static char getLatitudeZone(final LatLong latLong) {
        if (isNorthPolar(latLong)) {
            if (latLong.longitudeValue(SI.RADIAN) < 0) {
                return 'Y';
            } else {
                return 'Z';
            }
        }
        if (isSouthPolar(latLong)) {
            if (latLong.longitudeValue(SI.RADIAN) < 0) {
                return 'A';
            } else {
                return 'B';
            }
        }
        return getLatitudeZone(latLong.latitudeValue(NonSI.DEGREE_ANGLE), NonSI.DEGREE_ANGLE);
    }

    /**
     * Returns true if the position indicated by the coordinates is north of the
     * northern limit of the UTM grid (84 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is greater than 84 degrees.
     */
    public static boolean isNorthPolar(final LatLong latLong) {
        return latLong.latitudeValue(NonSI.DEGREE_ANGLE) > NORTH_POLAR_MIN;
    }

    /**
     * Returns true if the position indicated by the coordinates is south of the
     * southern limit of the UTM grid (-80 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is less than -80 degrees.
     */
    public static boolean isSouthPolar(final LatLong latLong) {
        return latLong.latitudeValue(NonSI.DEGREE_ANGLE) < SOUTH_POLAR_MIN;
    }

    /**
     * Decimal scale rounding - always round half up (0-4 rounds down, 5-9
     * rounds up)
     *
     * @param decdeg
     * @param scale
     * @return
     */
    public static double roundHalfUp(double decdeg, int scale) {
        return BigDecimal.valueOf(decdeg).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Round a decimal to an int - always round half up (0-4 rounds down, 5-9
     * rounds up)
     *
     * NOTE: I didn't use Math.round() because it rounds down on .50
     *
     * @param decdeg
     * @return
     */
    public static int roundHalfUp(double decdeg) {
        return (int) BigDecimal.valueOf(decdeg).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * simply truncate the decimal component of the double and return the
     * integer (no rounding)
     *
     * @param d
     * @return
     */
    public static int truncateDouble(double d) {
        return (int) d;
    }

}
