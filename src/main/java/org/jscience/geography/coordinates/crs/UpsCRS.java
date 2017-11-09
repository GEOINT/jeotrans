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

import javax.measure.Measure;
import javax.measure.converter.ConversionException;
import javax.measure.unit.SI;
import org.geoint.jeotrans.coordinate.PolarStereographic;
import org.geoint.jeotrans.coordinate.UPS;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem.AbsolutePosition;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 *
 * @author Steve Siebert
 */
public final class UpsCRS extends ProjectedCRS<UPS> {

    private static final double MAX_LAT = ((Math.PI * 90) / 180.0); //radians
    private static final double MAX_ORIGIN_LAT = ((81.114528 * Math.PI) / 180.0); //radians
    private static final double MIN_NORTH_LAT = (83.5 * Math.PI / 180.0); //radians
    private static final double MIN_SOUTH_LAT = (-79.5 * Math.PI / 180.0); //radians
    private static final double UPS_FALSE_EASTING = 2000000;
    private static final double UPS_FALSE_NORTHING = 2000000;
    private static final int MIN_EAST_NORTH = 0;
    private static final double MAX_EAST_NORTH = 4000000;

    /* Ellipsoid Parameters, default to WGS 84  */
 /* Semi-major axis of ellipsoid in meters
     */
    private double upsA = 6378137.0;
    /* Flattening of ellipsoid
     */
    private double upsF = 1 / 298.257223563;
    /* set default = North Hemisphere
     */
    private double upsOriginLatitude = MAX_ORIGIN_LAT;
    private double upsOriginLongitude = 0.0;

    public UpsCRS() {
        super();
    }

    public UpsCRS(double semiMajor, double flattening) {
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
        upsA = semiMajor;
        upsF = flattening;
    }

    /**
     * Convert geodetic to UPS
     *
     * @param ap
     * @return
     */
    @Override
    protected UPS coordinatesOf(AbsolutePosition ap) {
        double latitude = ap.latitudeWGS84.doubleValue(SI.RADIAN);
        double longitude = ap.longitudeWGS84.doubleValue(SI.RADIAN);
        char hemisphere;

        validateUpsCoord(latitude, longitude);

        if (latitude < 0) {
            upsOriginLatitude = -MAX_ORIGIN_LAT;
            hemisphere = 'S';
        } else {
            upsOriginLatitude = MAX_ORIGIN_LAT;
            hemisphere = 'N';
        }

        LatLong latLong = LatLong.valueOf(latitude, longitude, SI.RADIAN);

        PolarStereographic ps
                = PolarStereographic.latLongToPolarStereographic(latLong,
                        upsA, upsF, upsOriginLatitude, upsOriginLongitude,
                        UPS_FALSE_EASTING, UPS_FALSE_NORTHING);

        UPS ups = UPS.valueOf(ps.eastingValue(SI.METER),
                ps.northingValue(SI.METER), hemisphere, SI.METER, this);
        return ups;
    }

    @Override
    protected AbsolutePosition positionOf(UPS ups, AbsolutePosition ap) {

        double easting = ups.eastingValue(SI.METER);
        double northing = ups.northingValue(SI.METER);

        if (!ups.isNorthernHemisphere() && !ups.isSouthernHemisphere()) {
            throw new ConversionException("Invalid hemisphere ('N' or 'S')");
        }
        if ((easting < MIN_EAST_NORTH) || (easting > MAX_EAST_NORTH)) {
            throw new ConversionException("Easting outside of valid range, "
                    + "(0 to 4,000,000m)");
        }
        if ((northing < MIN_EAST_NORTH) || (northing > MAX_EAST_NORTH)) {
            throw new ConversionException("Northing outside of valid range, "
                    + "(0 to 4,000,000m)");
        }

        if (ups.isNorthernHemisphere()) {
            upsOriginLatitude = MAX_ORIGIN_LAT;
        }
        if (ups.isSouthernHemisphere()) {
            upsOriginLatitude = -MAX_ORIGIN_LAT;
        }

        /**
         * PolarStereographic ps =
         * PolarStereographic.latLongToPolarStereographic (latLong, upsA, upsF,
         * upsOriginLatitude, upsOriginLongitude, UPS_FALSE_EASTING,
         * UPS_FALSE_NORTHING);
         *
         */
        /*
         * public static LatLong polarStereographicToLatLong (PolarStereographic ps,
            double a, double f, double latitudeScale, double longitudeFromPole,
            double falseEasting, double falseNorthing)
         */
        PolarStereographic ps = PolarStereographic.valueOf(ups.eastingValue(SI.METER), ups.northingValue(SI.METER), SI.METER);

        LatLong latLong = PolarStereographic.polarStereographicToLatLong(ps, upsA, upsF, upsOriginLatitude, upsOriginLongitude,
                UPS_FALSE_EASTING, UPS_FALSE_NORTHING);

        double lat = latLong.latitudeValue(SI.RADIAN);
        double lon = latLong.longitudeValue(SI.RADIAN);

        validateUpsCoord(lat, lon);

        ap.latitudeWGS84 = Measure.valueOf(lat, SI.RADIAN);
        ap.longitudeWGS84 = Measure.valueOf(lon, SI.RADIAN);

        return ap;
    }

    private void validateUpsCoord(double latitudeRadian,
            double longitudeRadian) throws ConversionException {
        if (latitudeRadian < 0 && (latitudeRadian > MIN_SOUTH_LAT || latitudeRadian < -MAX_LAT)) {
            throw new ConversionException("Latitude outside of valid range "
                    + "(North Pole: 83.5 to 90, South Pole: -79.5 to -90)");
        }
        if (latitudeRadian >= 0 && (latitudeRadian < MIN_NORTH_LAT || latitudeRadian > MAX_LAT)) {
            throw new ConversionException("Latitude outside of valid range "
                    + "(North Pole: 83.5 to 90, South Pole: -79.5 to -90)");
        }
        if ((longitudeRadian < -Math.PI) || (longitudeRadian > (2 * Math.PI))) {
            /* slam out of range */
            throw new ConversionException("Longitude outside of valid "
                    + "range (-180 to 360 degrees)");
        }
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return ProjectedCRS.EASTING_NORTHING_CS;
    }
}
