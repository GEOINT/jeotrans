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
import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem.AbsolutePosition;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 *
 * @author Steve Siebert
 */
public final class PolarStereographicCRS extends ProjectedCRS<PolarStereographic> {

    private final static double PI_OVER_2 = (Math.PI / 2.0);
    private final static double TWO_PI = (2.0 * Math.PI);
    private final static double PI_OVER_4 = (Math.PI / 4.0);

    /* Ellipsoid Parameters, default to WGS 84  */
 /* Semi-major axis of ellipsoid in meters  */
    private double polarA = 6378137.0;
    /* Flattening of ellipsoid  */
    private double polarF = 1 / 298.257223563;
    /* Eccentricity of ellipsoid    */
    private double es = 0.08181919084262188000;
    /* es / 2.0 */
    private double esOver2 = .040909595421311;
    /* Flag variable */
    private double southernHemisphere = 0;
    private double tc = 1.0;
    private double e4 = 1.0033565552493;
    /* Polar_a * mc */
    private double polarAMc = 6378137.0;
    /* 2.0 * Polar_a */
    private double twoPolarA = 12756274.0;

    /* Polar Stereographic projection Parameters */
 /* Latitude of origin in radians */
    private double polarOriginLat = ((Math.PI * 90) / 180);
    /* Longitude of origin in radians */
    private double polarOriginLong = 0.0;
    /* False easting in meters */
    private double polarFalseEasting = 0.0;
    /* False northing in meters */
    private double polarFalseNorthing = 0.0;

    /* Maximum variance for easting and northing values for WGS 84. */
    private double polarDeltaEasting = 12713601.0;
    private double polarDeltaNorthing = 12713601.0;

    public PolarStereographicCRS() {
    }

    /**
     * Constructor.
     *
     * Set the ellipsoid parameters and Polar Stereograpic projection parameters
     * as inputs, and sets the corresponding state variables.
     *
     * @param a Semi-major axis of ellipsoid, in meters
     * @param f Flattening of ellipsoid
     * @param latitudeScale Latitude of true scale, in radians
     * @param longitudeFromPole Longitude down from pole, in radians
     * @param falseEasting Easting (X) at center of projection, in meters
     * @param falseNorthing Northing (Y) at center of projection, in meters
     */
    public PolarStereographicCRS(double a,
            double f, double latitudeScale, double longitudeFromPole,
            double falseEasting, double falseNorthing) {

        double es2;
        double slat, clat;
        double essin;
        double one_PLUS_es, one_MINUS_es;
        double pow_es;
        double inv_f = 1 / f;
        double mc;

        if (a <= 0.0) {
            /* Semi-major axis must be greater than zero */
            throw new ConversionException("Semi-major axis less than "
                    + "or equal to zero");
        }
        if ((inv_f < 250) || (inv_f > 350)) {
            /* Inverse flattening must be between 250 and 350 */
            throw new ConversionException("Inverse flattening outside of "
                    + "valid range (250 to 350)");
        }
        if ((latitudeScale < -PI_OVER_2) || (latitudeScale > PI_OVER_2)) {
            /* Origin Latitude out of range */
            throw new ConversionException("Latitude of true scale outside "
                    + "of valid range (-90 to 90 degrees)");
        }
        if ((longitudeFromPole < -Math.PI) || (longitudeFromPole > TWO_PI)) {
            /* Origin Longitude out of range */
            throw new ConversionException("Longitude down from pole "
                    + "outside of valid range (-180 to 360 degrees)");
        }

        polarA = a;
        twoPolarA = 2.0 * polarA;
        polarF = f;

        if (longitudeFromPole > Math.PI) {
            longitudeFromPole -= TWO_PI;
        }
        if (latitudeScale < 0) {
            southernHemisphere = 1;
            polarOriginLat = -latitudeScale;
            polarOriginLong = -longitudeFromPole;
        } else {
            southernHemisphere = 0;
            polarOriginLat = latitudeScale;
            polarOriginLong = longitudeFromPole;
        }
        polarFalseEasting = falseEasting;
        polarFalseNorthing = falseNorthing;

        es2 = 2 * polarF - polarF * polarF;
        es = Math.sqrt(es2);
        esOver2 = es / 2.0;

        if (Math.abs(Math.abs(polarOriginLat) - PI_OVER_2) > 1.0e-10) {
            slat = Math.sin(polarOriginLat);
            essin = es * slat;
            pow_es = polarPow(essin);
            clat = Math.cos(polarOriginLat);
            mc = clat / Math.sqrt(1.0 - essin * essin);
            polarAMc = polarA * mc;
            tc = Math.tan(PI_OVER_4 - polarOriginLat / 2.0) / pow_es;
        } else {
            one_PLUS_es = 1.0 + es;
            one_MINUS_es = 1.0 - es;
            e4 = Math.sqrt(Math.pow(one_PLUS_es, one_PLUS_es) * Math.pow(one_MINUS_es, one_MINUS_es));
        }

        /* Calculate Radius */
        AbsolutePosition radiusAP = new AbsolutePosition();
        radiusAP.latitudeWGS84 = Measure.valueOf(0, SI.RADIAN);
        radiusAP.longitudeWGS84 = Measure.valueOf(longitudeFromPole, SI.RADIAN);
        PolarStereographic radiusPS = this.coordinatesOf(radiusAP);

        polarDeltaNorthing = radiusPS.northingValue(SI.METER);
        if (polarFalseNorthing != 0) {
            polarDeltaNorthing -= polarFalseNorthing;
        }
        if (polarDeltaNorthing < 0) {
            polarDeltaNorthing = -polarDeltaNorthing;
        }
        polarDeltaNorthing *= 1.01;

        polarDeltaEasting = polarDeltaNorthing;

    }

    /**
     * Convert geodetic to Stereographic Polar
     *
     * @param ap
     * @return
     */
    @Override
    protected PolarStereographic coordinatesOf(AbsolutePosition ap) {
        double dlam;
        double slat;
        double essin;
        double t;
        double rho;
        double pow_es;
        double easting, northing; //meters

        double latitude = ap.latitudeWGS84.doubleValue(SI.RADIAN);
        double longitude = ap.longitudeWGS84.doubleValue(SI.RADIAN);

        if ((latitude < -PI_OVER_2) || (latitude > PI_OVER_2)) {
            /* Latitude out of range */
            throw new ConversionException("Latitude outside of valid "
                    + "range (-90 to 90 degrees)");
        }
        if ((latitude < 0) && (southernHemisphere == 0)) {
            /* Latitude and Origin Latitude in different hemispheres */
            throw new ConversionException("Latitude outside of valid "
                    + "range (-90 to 90 degrees)");
        }
        if ((latitude > 0) && (southernHemisphere == 1)) {
            /* Latitude and Origin Latitude in different hemispheres */
            throw new ConversionException("Latitude outside of valid "
                    + "range (-90 to 90 degrees)");
        }
        if ((longitude < -Math.PI) || (longitude > TWO_PI)) {
            /* Longitude out of range */
            throw new ConversionException("Longitude outside of valid "
                    + "range (-180 to 360 degrees)");
        }

        if (Math.abs(Math.abs(latitude) - PI_OVER_2) < 1.0e-10) {
            easting = polarFalseEasting;
            northing = polarFalseNorthing;
        } else {
            if (southernHemisphere != 0) {
                longitude *= -1.0;
                latitude *= -1.0;
            }
            dlam = longitude - polarOriginLong;
            if (dlam > Math.PI) {
                dlam -= TWO_PI;
            }
            if (dlam < -Math.PI) {
                dlam += TWO_PI;
            }
            slat = Math.sin(latitude);
            essin = es * slat;
            pow_es = polarPow(essin);
            t = Math.tan(PI_OVER_4 - latitude / 2.0) / pow_es;

            if (Math.abs(Math.abs(polarOriginLat) - PI_OVER_2) > 1.0e-10) {
                rho = polarAMc * t / tc;
            } else {
                rho = twoPolarA * t / e4;
            }

            if (southernHemisphere != 0) {
                easting = -(rho * Math.sin(dlam) - polarFalseEasting);
                northing = rho * Math.cos(dlam) + polarFalseNorthing;
            } else {
                easting = rho * Math.sin(dlam) + polarFalseEasting;
                northing = -rho * Math.cos(dlam) + polarFalseNorthing;
            }

        }
        PolarStereographic ps
                = PolarStereographic.valueOf(easting, northing, SI.METER, this);
        return ps;
    }

    /**
     * Convert Polar Stereographic to geodetic
     *
     * @param ps
     * @param ap
     * @return
     */
    @Override
    protected AbsolutePosition positionOf(PolarStereographic ps,
            AbsolutePosition ap) {
        double dy;
        double dx;
        double rho;
        double t;
        double PHI, sin_PHI;
        double tempPHI = 0.0;
        double essin;
        double pow_es;
        double delta_radius;
        double min_easting = polarFalseEasting - polarDeltaEasting;
        double max_easting = polarFalseEasting + polarDeltaEasting;
        double min_northing = polarFalseNorthing - polarDeltaNorthing;
        double max_northing = polarFalseNorthing + polarDeltaNorthing;
        double easting = ps.eastingValue(SI.METER);
        double northing = ps.northingValue(SI.METER);
        double latitude, longitude;

        if (easting > max_easting || easting < min_easting) {
            /* Easting out of range */
            throw new ConversionException("Easting outside of valid range, "
                    + "depending on ellipsoid and projection parameters");
        }
        if (northing > max_northing || northing < min_northing) {
            /* Northing out of range */
            throw new ConversionException("Northing outside of valid "
                    + "range, depending on ellipsoid and projection parameters");
        }

        dy = northing - polarFalseNorthing;
        dx = easting - polarFalseEasting;

        /* Radius of point with origin of false easting, false northing */
        rho = Math.sqrt(dx * dx + dy * dy);

        delta_radius = Math.sqrt(polarDeltaEasting * polarDeltaEasting + polarDeltaNorthing * polarDeltaNorthing);

        if (rho > delta_radius) {
            /* Point is outside of projection area */
            throw new ConversionException("Coordinates too far from pole, "
                    + "depending on ellipsoid and projection parameters");
        }

        if ((dy == 0.0) && (dx == 0.0)) {
            latitude = PI_OVER_2;
            longitude = polarOriginLong;

        } else {
            if (southernHemisphere != 0) {
                dy *= -1.0;
                dx *= -1.0;
            }

            if (Math.abs(Math.abs(polarOriginLat) - PI_OVER_2) > 1.0e-10) {
                t = rho * tc / (polarAMc);
            } else {
                t = rho * e4 / (twoPolarA);
            }
            PHI = PI_OVER_2 - 2.0 * Math.atan(t);
            while (Math.abs(PHI - tempPHI) > 1.0e-10) {
                tempPHI = PHI;
                sin_PHI = Math.sin(PHI);
                essin = es * sin_PHI;
                pow_es = polarPow(essin);
                PHI = PI_OVER_2 - 2.0 * Math.atan(t * pow_es);
            }
            latitude = PHI;
            longitude = polarOriginLong + Math.atan2(dx, -dy);

            if (longitude > Math.PI) {
                longitude -= TWO_PI;
            } else if (longitude < -Math.PI) {
                longitude += TWO_PI;
            }

            if (latitude > PI_OVER_2) /* force distorted values to 90, -90 degrees */ {
                latitude = PI_OVER_2;
            } else if (latitude < -PI_OVER_2) {
                latitude = -PI_OVER_2;
            }

            if (longitude > Math.PI) /* force distorted values to 180, -180 degrees */ {
                longitude = Math.PI;
            } else if (longitude < -Math.PI) {
                longitude = -Math.PI;
            }

        }
        if (southernHemisphere != 0) {
            latitude *= -1.0;
            longitude *= -1.0;
        }
        ap.latitudeWGS84 = Measure.valueOf(latitude, SI.RADIAN);
        ap.longitudeWGS84 = Measure.valueOf(longitude, SI.RADIAN);
        return ap;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private double polarPow(double EsSin) {
        return Math.pow((1.0 - EsSin) / (1.0 + EsSin), esOver2);
    }
}
