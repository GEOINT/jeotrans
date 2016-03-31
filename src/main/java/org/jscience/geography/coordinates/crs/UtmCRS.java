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
package org.jscience.geography.coordinates.crs;

import org.geoint.jeotrans.util.ConversionUtil;
import javax.measure.Measure;
import javax.measure.converter.ConversionException;
import javax.measure.unit.SI;
import org.geoint.jeotrans.coordinate.TransverseMercator;
import org.geoint.jeotrans.coordinate.UTM;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem.AbsolutePosition;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 *
 * @author Steve Siebert
 */
public final class UtmCRS extends ProjectedCRS<UTM> {

    private static final double MIN_LAT = ((-80.5 * Math.PI) / 180.0);
    private static final double MAX_LAT = ((84.5 * Math.PI) / 180.0);
    private static final int MIN_EASTING = 100000;
    private static final int MAX_EASTING = 900000;
    private static final int MIN_NORTHING = 0;
    private static final int MAX_NORTHING = 10000000;
    /**
     * Semi-major axis of ellipsoid in meters
     */
    private double utmA = 6378137.0;
    /**
     * Flattening of ellipsoid
     */
    private double utmF = 1 / 298.257223563;
    /**
     * Zone override flag
     */
    private int utmOverride = 0;

    public UtmCRS() {
        super();
    }

    public UtmCRS(double semiMajor, double flattening) {
        this(semiMajor, flattening, 0);

    }

    public UtmCRS(double semiMajor, double flattening, int override) {
        super();

        double invF = 1 / flattening;

        if (semiMajor <= 0.0) {
            /* Semi-major axis must be greater than zero */
            throw new ConversionException("Semi-major axis less than or "
                    + "equal to zero");
        }
        if ((invF < 250) || (invF > 350)) {
            /* Inverse flattening must be between 250 and 350 */
            throw new ConversionException("Inverse flattening outside of "
                    + "valid range (250 to 350)");
        }
        if ((override < 0) || (override > 60)) {
            throw new ConversionException("Zone outside of valid range "
                    + "(1 to 60) and within 1 of 'natural' zone");
        }

        utmA = semiMajor;
        utmF = flattening;
        utmOverride = override;
    }

    /**
     * Convert the AbsolutePosition to a UTM coordainte
     *
     * @param ap
     * @return
     */
    @Override
    protected UTM coordinatesOf(AbsolutePosition ap) throws ConversionException {
        double latitudeRadian = ap.latitudeWGS84.doubleValue(SI.RADIAN);
        double longitudeRadian = ap.longitudeWGS84.doubleValue(SI.RADIAN);

        int latDegrees;
        int longDegrees;
        int tempZone;
        double originLatitude = 0;
        double centralMeridian;
        double falseEasting = 500000;
        double falseNorthing = 0;
        double scale = 0.9996;

        if ((latitudeRadian < MIN_LAT) || (latitudeRadian > MAX_LAT)) {
            /* Latitude out of range */
            throw new ConversionException("Latitude outside of valid "
                    + "range (-80.5 to 84.5 degrees)");
        }
        if ((longitudeRadian < -Math.PI) || (longitudeRadian > (2 * Math.PI))) {
            /* Longitude out of range */
            throw new ConversionException("Longitude outside of valid "
                    + "range (-180 to 360 degrees)");
        }

        if ((latitudeRadian > -1.0e-9) && (latitudeRadian < 0)) {
            latitudeRadian = 0.0;
        }
        if (longitudeRadian < 0) {
            longitudeRadian += (2 * Math.PI) + 1.0e-10;
        }

        latDegrees = (int) (latitudeRadian * 180.0 / Math.PI);
        longDegrees = (int) (longitudeRadian * 180.0 / Math.PI);

        if (longitudeRadian < Math.PI) {
            tempZone = (int) (31 + ((longitudeRadian * 180.0 / Math.PI) / 6.0));
        } else {
            tempZone = (int) (((longitudeRadian * 180.0 / Math.PI) / 6.0) - 29);
        }

        if (tempZone > 60) {
            tempZone = 1;
        }
        /* UTM special cases */
        if ((latDegrees > 55) && (latDegrees < 64) && (longDegrees > -1)
                && (longDegrees < 3)) {
            tempZone = 31;
        }
        if ((latDegrees > 55) && (latDegrees < 64) && (longDegrees > 2)
                && (longDegrees < 12)) {
            tempZone = 32;
        }
        if ((latDegrees > 71) && (longDegrees > -1) && (longDegrees < 9)) {
            tempZone = 31;
        }
        if ((latDegrees > 71) && (longDegrees > 8) && (longDegrees < 21)) {
            tempZone = 33;
        }
        if ((latDegrees > 71) && (longDegrees > 20) && (longDegrees < 33)) {
            tempZone = 35;
        }
        if ((latDegrees > 71) && (longDegrees > 32) && (longDegrees < 42)) {
            tempZone = 37;
        }

        if (utmOverride != 0) {
            if ((tempZone == 1) && (utmOverride == 60)) {
                tempZone = utmOverride;
            } else if ((tempZone == 60) && (utmOverride == 1)) {
                tempZone = utmOverride;
            } else if ((latDegrees > 71) && (longDegrees > -1)
                    && (longDegrees < 42)) {
                if (((tempZone - 2) <= utmOverride)
                        && (utmOverride <= (tempZone + 2))) {
                    tempZone = utmOverride;
                } else {
                    throw new ConversionException("Zone outside of valid "
                            + "range (1 to 60) and within 1 of 'natural' zone");
                }
            } else if (((tempZone - 1) <= utmOverride)
                    && (utmOverride <= (tempZone + 1))) {
                tempZone = utmOverride;
            } else {
                throw new ConversionException("Zone outside of valid "
                        + "range (1 to 60) and within 1 of 'natural' zone");
            }
        }

        if (tempZone >= 31) {
            centralMeridian = (6 * tempZone - 183) * Math.PI / 180.0;
        } else {
            centralMeridian = (6 * tempZone + 177) * Math.PI / 180.0;
        }

        if (latitudeRadian < 0) {
            falseNorthing = 10000000;
        }

        LatLong latLong = LatLong.valueOf(latitudeRadian,
                longitudeRadian, SI.RADIAN);
        TransverseMercator tm
                = TransverseMercator.latLongToTransverseMercator(latLong, utmA,
                        utmF, originLatitude, centralMeridian, falseEasting,
                        falseNorthing, scale);
        UTM utm = UTM.valueOf(tempZone, ConversionUtil.getLatitudeZone(latLong),
                tm.eastingValue(SI.METER), tm.northingValue(SI.METER), SI.METER);

        double easting = utm.eastingValue(SI.METER);
        double northing = utm.northingValue(SI.METER);
        if (easting < MIN_EASTING || easting > MAX_EASTING) {
            throw new ConversionException("Easting outside of valid range "
                    + "(100,000 to 900,000 meters)");
        }
        if (northing < MIN_NORTHING || northing > MAX_NORTHING) {
            throw new ConversionException("Northing outside of valid range "
                    + "(0 to 10,000,000 meters)");
        }
        return utm;
    }

    @Override
    protected AbsolutePosition positionOf(UTM utm, AbsolutePosition ap) {
        double Origin_Latitude = 0;
        double Central_Meridian;
        double False_Easting = 500000;
        double False_Northing = 0;
        double Scale = 0.9996;

        int longitudeZone = utm.getUtmZone();
        double easting = utm.eastingValue(SI.METER);
        double northing = utm.northingValue(SI.METER);

        if ((longitudeZone < 1) || (longitudeZone > 60)) {
            throw new ConversionException("Zone outside of valid "
                    + "range (1 to 60)");
        }
        if ((easting < MIN_EASTING) || (easting > MAX_EASTING)) {
            throw new ConversionException("Easting outside of valid range "
                    + "(100,000 to 900,000 meters)");
        }
        if ((northing < MIN_NORTHING) || (northing > MAX_NORTHING)) {
            throw new ConversionException("Northing outside of valid range "
                    + "(0 to 10,000,000 meters)");
        }

        if (longitudeZone >= 31) {
            Central_Meridian = ((6 * longitudeZone - 183) * Math.PI / 180.0);
        } else {
            Central_Meridian = ((6 * longitudeZone + 177) * Math.PI / 180.0);
        }
        if (utm.isSouthernHemisphere()) {
            False_Northing = 10000000;
        }
        TransverseMercator tm = TransverseMercator.valueOf(easting, northing,
                SI.METER);
        LatLong latLong
                = TransverseMercator.transverseMercatorToLatLong(tm, utmA,
                        utmF, Origin_Latitude, Central_Meridian,
                        False_Easting, False_Northing, Scale);

        if ((latLong.latitudeValue(SI.RADIAN) < MIN_LAT)
                || (latLong.latitudeValue(SI.RADIAN) > MAX_LAT)) {
            /* Latitude out of range */
            throw new ConversionException("Northing outside of valid range "
                    + "(0 to 10,000,000 meters)");
        }

        ap.latitudeWGS84
                = Measure.valueOf(latLong.latitudeValue(SI.RADIAN), SI.RADIAN);
        ap.longitudeWGS84
                = Measure.valueOf(latLong.longitudeValue(SI.RADIAN), SI.RADIAN);
        return ap;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return ProjectedCRS.EASTING_NORTHING_CS;
    }

}
