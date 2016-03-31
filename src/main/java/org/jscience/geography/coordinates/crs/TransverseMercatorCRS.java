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
import org.geoint.jeotrans.coordinate.TransverseMercator;

import org.opengis.referencing.cs.CoordinateSystem;

/**
 *
 * @author Steve Siebert
 */
public final class TransverseMercatorCRS extends ProjectedCRS<TransverseMercator>
{

    /* Math constants */
    private static final double PI_OVER_2 = Math.PI / 2.0e0;
    private static final double MIN_SCALE_FACTOR = 0.3;
    private static final double MAX_SCALE_FACTOR = 3.0;
    private static final double MAX_LAT = ((Math.PI * 89.99) / 180.0);
    private static final double MAX_DELTA_LONG = ((Math.PI * 90) / 180.0);
    /*********** Ellipsoid Parameters, default to WGS 84  *****************/
    /**
     * Semi-major axis of ellipsoid in meters
     */
    private double tranMercA = 6378137.0;
    /**
     * Flattening of ellipsoid
     */
    private double tranMercF = 1 / 298.257223563;
    /**
     * Eccentricity (0.08181919084262188000) squared
     */
    private double tranMercES = 0.0066943799901413800;
    /**
     * Second Eccentricity squared
     */
    private double tranMercEBS = 0.0067394967565869;
    /******* Isometeric to geodetic latitude parameters, default to WGS 84 ****/
    private double tranMercAP = 6367449.1458008;
    private double tranMercBP = 16038.508696861;
    private double tranMercCP = 16.832613334334;
    private double tranMercDP = 0.021984404273757;
    private double tranMercEP = 3.1148371319283e-005;

    /* Maximum variance for easting and northing values for WGS 84. */
    private double tranMercDeltaEasting = 40000000.0;
    private double tranMercDeltaNorthing = 40000000.0;

    /* Transverse_Mercator projection Parameters */
    /**
     * Latitude of origin in radians
     */
    private double tranMercOriginLat = 0.0;
    /**
     * Longitude of origin in radians
     */
    private double tranMercOriginLong = 0.0;
    /**
     * False northing in meters
     */
    private double tranMercFalseNorthing = 0.0;
    /**
     * False easting in meters
     */
    private double tranMercFalseEasting = 0.0;
    /**
     * Scale factor
     */
    private double tranMercScaleFactor = 1.0;

    public TransverseMercatorCRS() {
        super();
        //default parameters
    }

    public TransverseMercatorCRS(double semiMajorAxis, double flattening,
            double originLatitude, double centralMeridian, double falseEasting,
            double falseNorthing, double scale) throws ConversionException {
        super();
        //set specific TM parameters
        this.setParameters(semiMajorAxis, flattening, originLatitude,
                centralMeridian, falseEasting, falseNorthing, scale);
    }

    /**
     *
     * Set alternate Transverse Mercator projection parameters
     *
     * @param semiMajorAxis Semi-major axis of ellipsoid, in meters
     * @param flattening Flattening of ellipsoid
     * @param originLatitude Latitude in radians at the origin of the projection
     * @param centralMeridian Longitude in radians at the center of the projection
     * @param falseEasting Easting/X at the center of the projection
     * @param falseNorthing Northing/Y at the center of the projection
     * @param scale Projection scale factor
     * @return
     */
    private void setParameters(double semiMajorAxis, double flattening,
            double originLatitude, double centralMeridian, double falseEasting,
            double falseNorthing, double scale) throws ConversionException {
        double tn;        /* True Meridianal distance constant  */
        double tn2;
        double tn3;
        double tn4;
        double tn5;
        double tranMercB; /* Semi-minor axis of ellipsoid, in meters */
        double invF = 1 / flattening;

        if (semiMajorAxis <= 0.0) 
        {
            /* Semi-major axis must be greater than zero */
            throw new ConversionException("Semi-major axis less than " +
                    "or equal to zero");
        }
        if ((invF < 250) || (invF > 350)) 
        {
            /* Inverse flattening must be between 250 and 350 */
            throw new ConversionException("Inverse flattening outside of " +
                    "valid range (250 to 350), provided: "+invF);
        }
        if ((originLatitude < -PI_OVER_2) || (originLatitude > PI_OVER_2)) 
        {
            /* origin latitude out of range */
            throw new ConversionException("Origin latitude outside of " +
                    "valid range (-90 to 90 degrees)");
        }
        if ((centralMeridian < -Math.PI) || (centralMeridian > (2 * Math.PI))) 
        {
            /* origin longitude out of range */
            throw new ConversionException("Central meridian outside of " +
                    "valid range (-180 to 360 degrees)");
        }
        if ((scale < MIN_SCALE_FACTOR) || (scale > MAX_SCALE_FACTOR)) {
            throw new ConversionException("Scale factor outside of valid " +
                    "range (0.3 to 3.0)");
        }


        tranMercA = semiMajorAxis;
        tranMercF = flattening;
        tranMercOriginLat = originLatitude;
        if (centralMeridian > Math.PI) {
            centralMeridian -= (2 * Math.PI);
        }
        tranMercOriginLong = centralMeridian;
        tranMercFalseNorthing = falseNorthing;
        tranMercFalseEasting = falseEasting;
        tranMercScaleFactor = scale;

        /* Eccentricity Squared */
        tranMercES = 2 * tranMercF - tranMercF * tranMercF;
        /* Second Eccentricity Squared */
        tranMercEBS = (1 / (1 - tranMercES)) - 1;

        tranMercB = tranMercA * (1 - tranMercF);
        /*True meridianal constants  */
        tn = (tranMercA - tranMercB) / (tranMercA + tranMercB);
        tn2 = tn * tn;
        tn3 = tn2 * tn;
        tn4 = tn3 * tn;
        tn5 = tn4 * tn;

        tranMercAP = tranMercA * (1.e0 - tn + 5.e0 * (tn2 - tn3) / 4.e0
                + 81.e0 * (tn4 - tn5) / 64.e0);
        tranMercBP = 3.e0 * tranMercA * (tn - tn2 + 7.e0 * (tn3 - tn4)
                / 8.e0 + 55.e0 * tn5 / 64.e0) / 2.e0;
        tranMercCP = 15.e0 * tranMercA
                * (tn2 - tn3 + 3.e0 * (tn4 - tn5) / 4.e0) / 16.0;
        tranMercDP = 35.e0 * tranMercA
                * (tn3 - tn4 + 11.e0 * tn5 / 16.e0) / 48.e0;
        tranMercEP = 315.e0 * tranMercA * (tn4 - tn5) / 512.e0;

        AbsolutePosition ap = new CoordinateReferenceSystem.AbsolutePosition();
        ap.latitudeWGS84 = Measure.valueOf(MAX_LAT, SI.RADIAN);
        ap.longitudeWGS84 = Measure.valueOf((MAX_DELTA_LONG + centralMeridian),
                SI.RADIAN);
        tranMercDeltaNorthing = coordinatesOf(ap).northingValue(SI.METER);

        ap.latitudeWGS84 = Measure.valueOf(0, SI.RADIAN);
        tranMercDeltaEasting = coordinatesOf(ap).eastingValue(SI.METER);

        tranMercDeltaNorthing++;
        tranMercDeltaEasting++;
    }

    private double sphtmd(double latitude) {
        return ((double) (tranMercAP * latitude
                - tranMercBP * Math.sin(2.e0 * latitude)
                + tranMercCP * Math.sin(4.e0 * latitude)
                - tranMercDP * Math.sin(6.e0 * latitude)
                + tranMercEP * Math.sin(8.e0 * latitude)));
    }

    private double sphsn(double latitude) {
        return ((double) (tranMercA / Math.sqrt(1.e0 - tranMercES
                * Math.pow(Math.sin(latitude), 2))));
    }

    private double sphsr(double latitude) {
        return ((double) (tranMercA * (1.e0 - tranMercES)
                / Math.pow(denom(latitude), 3)));
    }

    private double denom(double latitude) {
        return ((double) (Math.sqrt(1.e0 - tranMercES
                * Math.pow(Math.sin(latitude), 2))));
    }

    @Override
    protected TransverseMercator coordinatesOf(AbsolutePosition ap)
            throws ConversionException {

        double c;       /* Cosine of latitude                          */
        double c2;
        double c3;
        double c5;
        double c7;
        double dlam;    /* Delta longitude - Difference in Longitude       */
        double eta;     /* constant - TranMerc_ebs *c *c                   */
        double eta2;
        double eta3;
        double eta4;
        double s;       /* Sine of latitude                        */
        double sn;      /* Radius of curvature in the prime vertical       */
        double t;       /* Tangent of latitude                             */
        double tan2;
        double tan3;
        double tan4;
        double tan5;
        double tan6;
        double t1;      /* Term in coordinate conversion formula - GP to Y */
        double t2;      /* Term in coordinate conversion formula - GP to Y */
        double t3;      /* Term in coordinate conversion formula - GP to Y */
        double t4;      /* Term in coordinate conversion formula - GP to Y */
        double t5;      /* Term in coordinate conversion formula - GP to Y */
        double t6;      /* Term in coordinate conversion formula - GP to Y */
        double t7;      /* Term in coordinate conversion formula - GP to Y */
        double t8;      /* Term in coordinate conversion formula - GP to Y */
        double t9;      /* Term in coordinate conversion formula - GP to Y */
        double tmd;     /* True Meridional distance                        */
        double tmdo;    /* True Meridional distance for latitude of origin */
        double temp_Origin;
        double temp_Long;

        double northing;
        double easting;

        double latitude = ap.latitudeWGS84.doubleValue(SI.RADIAN);
        double longitude = ap.longitudeWGS84.doubleValue(SI.RADIAN);

        if ((latitude < -MAX_LAT) || (latitude > MAX_LAT)) 
        {
            /* Latitude out of range */
            throw new ConversionException("Latitude outside of valid range " +
                    "(-90 to 90 degrees)");
        }
        if (longitude > Math.PI) {
            longitude -= (2 * Math.PI);
        }
        if ((longitude < (tranMercOriginLong - MAX_DELTA_LONG))
                || (longitude > (tranMercOriginLong + MAX_DELTA_LONG))) {
            if (longitude < 0) {
                temp_Long = longitude + 2 * Math.PI;
            } else {
                temp_Long = longitude;
            }
            if (tranMercOriginLong < 0) {
                temp_Origin = tranMercOriginLong + 2 * Math.PI;
            } else {
                temp_Origin = tranMercOriginLong;
            }
            if ((temp_Long < (temp_Origin - MAX_DELTA_LONG))
                    || (temp_Long > (temp_Origin + MAX_DELTA_LONG))) {
                throw new ConversionException("Longitude outside of "
                        + "valid range (-180 to 360 degrees, and within "
                        + "+/-90 of Central Meridian)");
            }
        }

        /*
         *  Delta Longitude
         */
        dlam = longitude - tranMercOriginLong;

        //if (Math.abs(dlam) > (9.0 * Math.PI / 180)) {
            /* Distortion will result if Longitude is more than 9 degrees
            from the Central Meridian */
        //}

        if (dlam > Math.PI) {
            dlam -= (2 * Math.PI);
        }
        if (dlam < -Math.PI) {
            dlam += (2 * Math.PI);
        }
        if (Math.abs(dlam) < 2.e-10) {
            dlam = 0.0;
        }

        s = Math.sin(latitude);
        c = Math.cos(latitude);
        c2 = c * c;
        c3 = c2 * c;
        c5 = c3 * c2;
        c7 = c5 * c2;
        t = Math.tan(latitude);
        tan2 = t * t;
        tan3 = tan2 * t;
        tan4 = tan3 * t;
        tan5 = tan4 * t;
        tan6 = tan5 * t;
        eta = tranMercEBS * c2;
        eta2 = eta * eta;
        eta3 = eta2 * eta;
        eta4 = eta3 * eta;

        /* radius of curvature in prime vertical */
        sn = sphsn(latitude);

        /* True Meridianal Distances */
        tmd = sphtmd(latitude);

        /*  Origin  */
        tmdo = sphtmd(tranMercOriginLat);

        /* northing */
        t1 = (tmd - tmdo) * tranMercScaleFactor;
        t2 = sn * s * c * tranMercScaleFactor / 2.e0;
        t3 = sn * s * c3 * tranMercScaleFactor * (5.e0 - tan2 + 9.e0 * eta
                + 4.e0 * eta2) / 24.e0;

        t4 = sn * s * c5 * tranMercScaleFactor * (61.e0 - 58.e0 * tan2
                + tan4 + 270.e0 * eta - 330.e0 * tan2 * eta + 445.e0 * eta2
                + 324.e0 * eta3 - 680.e0 * tan2 * eta2 + 88.e0 * eta4
                - 600.e0 * tan2 * eta3 - 192.e0 * tan2 * eta4) / 720.e0;

        t5 = sn * s * c7 * tranMercScaleFactor * (1385.e0 - 3111.e0
                * tan2 + 543.e0 * tan4 - tan6) / 40320.e0;

        northing = tranMercFalseNorthing + t1 + Math.pow(dlam, 2.e0) * t2
                + Math.pow(dlam, 4.e0) * t3 + Math.pow(dlam, 6.e0) * t4
                + Math.pow(dlam, 8.e0) * t5;

        /* Easting */
        t6 = sn * c * tranMercScaleFactor;
        t7 = sn * c3 * tranMercScaleFactor * (1.e0 - tan2 + eta) / 6.e0;
        t8 = sn * c5 * tranMercScaleFactor * (5.e0 - 18.e0 * tan2 + tan4
                + 14.e0 * eta - 58.e0 * tan2 * eta + 13.e0 * eta2 + 4.e0 * eta3
                - 64.e0 * tan2 * eta2 - 24.e0 * tan2 * eta3) / 120.e0;
        t9 = sn * c7 * tranMercScaleFactor * (61.e0 - 479.e0 * tan2
                + 179.e0 * tan4 - tan6) / 5040.e0;

        easting = tranMercFalseEasting + dlam * t6 + Math.pow(dlam, 3.e0) * t7
                + Math.pow(dlam, 5.e0) * t8 + Math.pow(dlam, 7.e0) * t9;
        return TransverseMercator.valueOf(easting, northing, SI.METER, this);
    }

    @Override
    protected AbsolutePosition positionOf(TransverseMercator tm,
            AbsolutePosition ap) {
        double c;       /* Cosine of latitude                          */
        double de;      /* Delta easting - Difference in Easting (Easting-Fe)*/
        double dlam;    /* Delta longitude - Difference in Longitude       */
        double eta;     /* constant - TranMerc_ebs *c *c                   */
        double eta2;
        double eta3;
        double eta4;
        double ftphi;   /* Footpoint latitude                              */
        int i;       /* Loop iterator                   */
        double s;       /* Sine of latitude                        */
        double sn;      /* Radius of curvature in the prime vertical       */
        double sr;      /* Radius of curvature in the meridian             */
        double t;       /* Tangent of latitude                             */
        double tan2;
        double tan4;
        double t10;     /* Term in coordinate conversion formula - GP to Y */
        double t11;     /* Term in coordinate conversion formula - GP to Y */
        double t12;     /* Term in coordinate conversion formula - GP to Y */
        double t13;     /* Term in coordinate conversion formula - GP to Y */
        double t14;     /* Term in coordinate conversion formula - GP to Y */
        double t15;     /* Term in coordinate conversion formula - GP to Y */
        double t16;     /* Term in coordinate conversion formula - GP to Y */
        double t17;     /* Term in coordinate conversion formula - GP to Y */
        double tmd;     /* True Meridional distance                        */
        double tmdo;    /* True Meridional distance for latitude of origin */

        double easting = tm.eastingValue(SI.METER);
        double northing = tm.northingValue(SI.METER);
        double latitude;
        double longitude;

        if ((easting < (tranMercFalseEasting - tranMercDeltaEasting))
                || (easting > (tranMercFalseEasting + tranMercDeltaEasting))) {
            /* Easting out of range  */
            throw new ConversionException("Easting outside of valid range "
                    + "(depending on ellipsoid and projection parameters)");
        }
        if ((northing < (tranMercFalseNorthing - tranMercDeltaNorthing))
                || (northing > (tranMercFalseNorthing + tranMercDeltaNorthing)))
        {
            /* Northing out of range */
            throw new ConversionException("Northing outside of valid range "
                    + "(depending on ellipsoid and projection parameters)");
        }


        /* True Meridional Distances for latitude of origin */
        tmdo = sphtmd(tranMercOriginLat);

        /*  Origin  */
        tmd = tmdo + (northing - tranMercFalseNorthing)
                / tranMercScaleFactor;

        /* First Estimate */
        sr = sphsr(0.e0);
        ftphi = tmd / sr;

        for (i = 0; i < 5; i++) {
            t10 = sphtmd(ftphi);
            sr = sphsr(ftphi);
            ftphi = ftphi + (tmd - t10) / sr;
        }

        /* Radius of Curvature in the meridian */
        sr = sphsr(ftphi);

        /* Radius of Curvature in the meridian */
        sn = sphsn(ftphi);

        /* Sine Cosine terms */
        s = Math.sin(ftphi);
        c = Math.cos(ftphi);

        /* Tangent Value  */
        t = Math.tan(ftphi);
        tan2 = t * t;
        tan4 = tan2 * tan2;
        eta = tranMercEBS * Math.pow(c, 2);
        eta2 = eta * eta;
        eta3 = eta2 * eta;
        eta4 = eta3 * eta;
        de = easting - tranMercFalseEasting;
        if (Math.abs(de) < 0.0001) {
            de = 0.0;
        }

        /* Latitude */
        t10 = t / (2.e0 * sr * sn * Math.pow(tranMercScaleFactor, 2));
        t11 = t * (5.e0 + 3.e0 * tan2 + eta - 4.e0 * Math.pow(eta, 2)
                - 9.e0 * tan2 * eta) / (24.e0 * sr * Math.pow(sn, 3)
                * Math.pow(tranMercScaleFactor, 4));
        t12 = t * (61.e0 + 90.e0 * tan2 + 46.e0 * eta + 45.E0 * tan4
                - 252.e0 * tan2 * eta - 3.e0 * eta2 + 100.e0
                * eta3 - 66.e0 * tan2 * eta2 - 90.e0 * tan4
                * eta + 88.e0 * eta4 + 225.e0 * tan4 * eta2
                + 84.e0 * tan2 * eta3 - 192.e0 * tan2 * eta4)
                / (720.e0 * sr * Math.pow(sn, 5)
                * Math.pow(tranMercScaleFactor, 6));
        t13 = t * (1385.e0 + 3633.e0 * tan2 + 4095.e0 * tan4 + 1575.e0
                * Math.pow(t, 6)) / (40320.e0 * sr * Math.pow(sn, 7)
                * Math.pow(tranMercScaleFactor, 8));

        latitude = ftphi - Math.pow(de, 2) * t10 + Math.pow(de, 4)
                * t11 - Math.pow(de, 6) * t12 + Math.pow(de, 8) * t13;

        t14 = 1.e0 / (sn * c * tranMercScaleFactor);

        t15 = (1.e0 + 2.e0 * tan2 + eta) / (6.e0 * Math.pow(sn, 3) * c
                * Math.pow(tranMercScaleFactor, 3));

        t16 = (5.e0 + 6.e0 * eta + 28.e0 * tan2 - 3.e0 * eta2
                + 8.e0 * tan2 * eta + 24.e0 * tan4 - 4.e0
                * eta3 + 4.e0 * tan2 * eta2 + 24.e0
                * tan2 * eta3) / (120.e0 * Math.pow(sn, 5) * c
                * Math.pow(tranMercScaleFactor, 5));

        t17 = (61.e0 + 662.e0 * tan2 + 1320.e0 * tan4 + 720.e0
                * Math.pow(t, 6)) / (5040.e0 * Math.pow(sn, 7) * c
                * Math.pow(tranMercScaleFactor, 7));

        /* Difference in Longitude */
        dlam = de * t14 - Math.pow(de, 3) * t15 + Math.pow(de, 5)
                * t16 - Math.pow(de, 7) * t17;

        /* Longitude */
        longitude = tranMercOriginLong + dlam;

        if (Math.abs(latitude) > (90.0 * Math.PI / 180.0)) {
            throw new ConversionException("Northing outside of valid "
                    + "range (depending on ellipsoid and projection "
                    + "parameters)");
        }

        if (longitude > Math.PI) {
            longitude -= (2 * Math.PI);
            if (Math.abs(longitude) > Math.PI) {
                throw new ConversionException("Easting outside of valid "
                        + "range (depending on ellipsoid and projection "
                        + "parameters)");
            }
        } else if (longitude < (-Math.PI)) {
            longitude += (2 * Math.PI);
            if (Math.abs(longitude) > Math.PI) {
                throw new ConversionException("Easting outside of valid "
                        + "range (depending on ellipsoid and projection "
                        + "parameters)");
            }
        }

        //if (Math.abs(dlam) > (9.0 * Math.PI / 180) * Math.cos(latitude)) {
            /* Distortion will result if Longitude is more than
             * 9 degrees from the Central Meridian at the equator
             * and decreases to 0 degrees at the poles
             * As you move towards the poles, distortion will become more
             * significant
             */
       // }
        ap.latitudeWGS84 = Measure.valueOf(latitude, SI.RADIAN);
        ap.longitudeWGS84 = Measure.valueOf(longitude, SI.RADIAN);
        return ap;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return ProjectedCRS.EASTING_NORTHING_CS;
    }
}
