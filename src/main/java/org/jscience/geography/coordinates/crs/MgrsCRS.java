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
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.geoint.jeotrans.coordinate.MGRS;
import org.geoint.jeotrans.coordinate.UPS;
import org.geoint.jeotrans.coordinate.UTM;
import org.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.CoordinateReferenceSystem.AbsolutePosition;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 *
 * @author Steve Siebert
 */
public final class MgrsCRS extends ProjectedCRS<MGRS> {

    private static final double DEG_TO_RAD = 0.017453292519943295;
    /* NUMBER OF LETTERS IN MGRS              */
    private static final int MGRS_LETTERS = 3;
    /* ONE HUNDRED THOUSAND                  */
    private static final double ONEHT = 100000.e0;
    /* TWO MILLION                           */
    private static final double TWOMIL = 2000000.e0;
    /* Maximum precision of easting & northing */
    private static final int MAX_PRECISION = 5;
    private static final int MIN_EAST_NORTH = 0;
    private static final int MAX_EAST_NORTH = 4000000;
    /* Ellipsoid parameters, default to WGS 84 */
 /* Semi-major axis of ellipsoid in meters */
    private double mgrsA = 6378137.0;
    /* Flattening of ellipsoid           */
    private double mgrsF = 1 / 298.257223563;
    private String mgrsEllipsoidCode = "WE";
    /*
     *    CLARKE_1866 : Ellipsoid code for CLARKE_1866
     *    CLARKE_1880 : Ellipsoid code for CLARKE_1880
     *    BESSEL_1841 : Ellipsoid code for BESSEL_1841
     *    BESSEL_1841_NAMIBIA : Ellipsoid code for BESSEL 1841 (NAMIBIA)
     */
    private static final String CLARKE_1866 = "CC";
    private static final String CLARKE_1880 = "CD";
    private static final String BESSEL_1841 = "BR";
    private static final String BESSEL_1841_NAMIBIA = "BN";
    private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
        'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * Constructor
     *
     */
    public MgrsCRS() {
    }

    /**
     * Constructor.
     *
     * Sets the ellipsoid parameters and the corresponding state variables.
     *
     * @param a Semi-major axis of ellipsoid in meters
     * @param f Flattening of ellipsoid
     * @param ellipsoidCode 2-letter code for ellipsoid
     */
    public MgrsCRS(double a, double f, String ellipsoidCode) {
        double inv_f = 1 / f;

        if (a <= 0.0) {
            /* Semi-major axis must be greater than zero */
            throw new ConversionException("Semi-major axis less than or "
                    + "equal to zero");
        }
        if ((inv_f < 250) || (inv_f > 350)) {
            /* Inverse flattening must be between 250 and 350 */
            throw new ConversionException("Inverse flattening outside "
                    + "of valid range (250 to 350)");
        }
        mgrsA = a;
        mgrsF = f;
        mgrsEllipsoidCode = ellipsoidCode;
    }

    /**
     * Convert geodetic to MGRS, this is a two-step process requiring geodetic
     * to UTM/UPS and finally UTM/UPS to MGRS
     *
     * @param ap
     * @return
     */
    @Override
    protected MGRS coordinatesOf(AbsolutePosition ap) {
        LatLong latLong = LatLong.valueOf(ap.latitudeWGS84.doubleValue(SI.RADIAN),
                ap.longitudeWGS84.doubleValue(SI.RADIAN), SI.RADIAN);
        if (ConversionUtil.isNorthPolar(latLong)
                || ConversionUtil.isSouthPolar(latLong)) {
            //convert to UPS
            UPS ups = UPS.latLongToUps(latLong);
            return upsToMgrs(ups);
        } else {
            //convert to UTM
            UTM utm = UTM.latLongToUtm(latLong);

            return utmToMgrs(utm);
        }
    }

    /**
     * Convert MGRS to geodetic, this is a two-step process requiring MGRS be
     * converted to UTM/UPS and finally UTM/UPS to geodetic
     *
     * @param mgrs
     * @param ap
     * @return
     */
    @Override
    protected AbsolutePosition positionOf(MGRS mgrs, AbsolutePosition ap) {
        LatLong latLong;

        if (mgrs.isNorthPolar() || mgrs.isSouthPolar()) {
            //convert to UPS
            UPS ups = mgrsToUps(mgrs);
            latLong = UPS.upsToLatLong(ups);
        } else {
            //convert to UTM
            UTM utm = mgrsToUtm(mgrs);
            latLong = UTM.utmToLatLong(utm);
        }
        ap.latitudeWGS84 = Measure.valueOf(latLong.latitudeValue(SI.RADIAN),
                SI.RADIAN);
        ap.longitudeWGS84 = Measure.valueOf(latLong.longitudeValue(SI.RADIAN),
                SI.RADIAN);
        return ap;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return ProjectedCRS.EASTING_NORTHING_CS;
    }

    /**
     * Convert MGRS to UTM, step 1 in the MGRS to geodetic conversion process
     *
     * @param mgrs
     * @return
     */
    protected UTM mgrsToUtm(MGRS mgrs) {
        /* Easting for 100,000 meter grid square      */
        double gridEasting;
        /* Northing for 100,000 meter grid square     */
        double gridNorthing;
        char[] gridSquare = mgrs.getGridSquare();
        double divisor;
        char hemisphere;
        double easting = mgrs.eastingValue(SI.METER); //meters
        double northing = mgrs.northingValue(SI.METER); //meters

        if ((mgrs.getLatitudeZone() == LETTER.X.getIndex())
                && ((mgrs.getUtmZone() == 32) || (mgrs.getUtmZone() == 34)
                || (mgrs.getUtmZone() == 36))) {
            throw new ConversionException("Invalid MGRS");
        } else {
            if (mgrs.getLatitudeZone() < LETTER.N.getIndex()) {
                hemisphere = 'S';
            } else {
                hemisphere = 'N';
            }

            GridValues gridValues = new GridValues(mgrs.getUtmZone());

            /* Check that the second letter of the MGRS string is within
             * the range of valid second letter values
             * Also check that the third letter is valid */
            LETTER l1 = LETTER.valueOf(String.valueOf(gridSquare[0]));
            LETTER l2 = LETTER.valueOf(String.valueOf(gridSquare[1]));

            if ((l1.getIndex() < gridValues.getSecondLetterLowValue())
                    || (l1.getIndex() > gridValues.getSecondLetterHighValue())
                    || (l2.getIndex() > LETTER.V.getIndex())) {
                throw new ConversionException("Invalid MGRS, determined during "
                        + "MGRS to UTM conversion.");
            }

            double row_letter_northing = (double) (l2.getIndex()) * ONEHT;
            gridEasting = (double) ((l1.getIndex()) - gridValues.getSecondLetterLowValue() + 1) * ONEHT;
            if ((gridValues.getSecondLetterLowValue() == LETTER.J.getIndex())
                    && (l1.getIndex() > LETTER.O.getIndex())) {
                gridEasting = gridEasting - ONEHT;
            }

            if (l2.getIndex() > LETTER.O.getIndex()) {
                row_letter_northing = row_letter_northing - ONEHT;
            }

            if (l2.getIndex() > LETTER.I.getIndex()) {
                row_letter_northing = row_letter_northing - ONEHT;
            }

            if (row_letter_northing >= TWOMIL) {
                row_letter_northing = row_letter_northing - TWOMIL;
            }

            LETTER latBand = LETTER.valueOf(String.valueOf(ALPHABET[LETTER.valueOf(String.valueOf(mgrs.getLatitudeZone())).getIndex()]));

            gridNorthing = row_letter_northing - gridValues.getPatternOffset();
            if (gridNorthing < 0) {
                gridNorthing += TWOMIL;
            }

            gridNorthing += latBand.getNorthingOffset();

            if (gridNorthing < latBand.getMinNorthing()) {
                gridNorthing += TWOMIL;
            }
            easting = gridEasting + easting;
            northing = gridNorthing + northing;

            UTM utm = UTM.valueOf(mgrs.getUtmZone(), mgrs.getLatitudeZone(), easting, northing, SI.METER);

            /* check that point is within Zone Letter bounds */
            LatLong latLong = UTM.utmToLatLong(utm, mgrsA, mgrsF, 0);

            divisor = Math.pow(10.0, MAX_PRECISION);

            LETTER latRange = LETTER.valueOf(String.valueOf(utm.getLatitudeZone()));
            double latitude = latLong.latitudeValue(NonSI.DEGREE_ANGLE);
            if (!(((latRange.getLowerLatitude() - DEG_TO_RAD / divisor) <= latitude)
                    && (latitude <= (latRange.getUpperLatitude() + DEG_TO_RAD / divisor)))) {
                throw new ConversionException("Latitude outside of valid "
                        + "range (-90 to 90 degrees)");
            }
            return utm;
        }

    }

    /**
     * Convert UTM to MGRS, step 2 in the geodetic to MGRS conversion
     *
     * @param utm
     * @return
     */
    protected MGRS utmToMgrs(UTM utm) {
        double gridEasting;
        /* Easting used to derive 2nd letter of MGRS   */
        double gridNorthing;
        /* Northing used to derive 3rd letter of MGRS  */
        int[] letters = new int[MGRS_LETTERS];
        /* Number location of 3 letters in alphabet    */
        double divisor;
        double rounded_easting;

        double easting = utm.eastingValue(SI.METER);
        double northing = utm.northingValue(SI.METER);

        LatLong latLong = UTM.utmToLatLong(utm);

        double latitude = latLong.latitudeValue(NonSI.DEGREE_ANGLE);
        double longitude = latLong.longitudeValue(NonSI.DEGREE_ANGLE);

        divisor = Math.pow(10.0, (5 - MAX_PRECISION));
        rounded_easting = ConversionUtil.roundHalfUp(easting / divisor) * divisor;

        /* Special check for rounding to (truncated) eastern edge of zone 31V */
        if ((utm.getUtmZone() == 31)
                && (((latitude >= 56.0)
                && (latitude < 64.0))
                && ((longitude >= 3.0)
                || (rounded_easting >= 500000.0)))) {
            /* Reconvert to UTM zone 32 */
            utm = UTM.latLongToUtm(latLong, mgrsA, mgrsF, 32);

            /* Round easting value using new easting */
            easting = ConversionUtil.roundHalfUp(easting / divisor) * divisor;
        } else {
            easting = rounded_easting;
        }

        /* Round northing values */
        northing = ConversionUtil.roundHalfUp(northing / divisor) * divisor;

        if (latitude <= 0.0 && northing == 1.0e7) {
            latitude = 0.0;
            northing = 0.0;
        }
        GridValues gridValues = new GridValues(utm.getUtmZone());

        char z = ConversionUtil.getLatitudeZone(latitude, NonSI.DEGREE_ANGLE);
        letters[0] = LETTER.valueOf(String.valueOf(z)).getIndex();

        gridNorthing = northing;

        while (gridNorthing >= TWOMIL) {
            gridNorthing = gridNorthing - TWOMIL;
        }
        gridNorthing = gridNorthing + gridValues.getPatternOffset();
        if (gridNorthing >= TWOMIL) {
            gridNorthing = gridNorthing - TWOMIL;
        }

        letters[2] = (int) (gridNorthing / ONEHT);
        if (letters[2] > LETTER.H.getIndex()) {
            letters[2] = letters[2] + 1;
        }

        if (letters[2] > LETTER.N.getIndex()) {
            letters[2] = letters[2] + 1;
        }

        gridEasting = easting;
        if (((letters[0] == LETTER.V.getIndex()) && (utm.getUtmZone() == 31))
                && (gridEasting == 500000.0)) {
            gridEasting = gridEasting - 1.0;
            /* SUBTRACT 1 METER */
        }

        letters[1] = gridValues.getSecondLetterLowValue() + ((int) (gridEasting / ONEHT) - 1);
        if ((gridValues.getSecondLetterLowValue() == LETTER.J.getIndex()) && (letters[1] > LETTER.N.getIndex())) {
            letters[1] = letters[1] + 1;
        }

        char[] gridSquare = {ALPHABET[letters[1]], ALPHABET[letters[2]]};

        gridEasting = gridEasting % 100000.0;
        if (gridEasting >= 99999.5) {
            gridEasting = 99999.0;
        }
        double east = (gridEasting / divisor);

        gridEasting = east;

        northing = northing % 100000.0;
        if (northing >= 99999.5) {
            northing = 99999.0;
        }
        double north = (northing / divisor);
        northing = north;
        return MGRS.valueOf(utm.getUtmZone(), ALPHABET[letters[0]], gridSquare, gridEasting, northing, SI.METER);
    }

    /**
     * Convert MGRS to UPS, step 1 in the MGRS to geodetic conversion process
     * when the MGRS location falls within the polar regions
     *
     * @param mgrs
     * @return
     */
    protected UPS mgrsToUps(MGRS mgrs) {
        char hemisphere;
        if (mgrs.isNorthPolar()) {
            hemisphere = 'N';
        } else if (mgrs.isSouthPolar()) {
            hemisphere = 'S';
        } else {
            throw new ConversionException("MGRS does not represent a polar"
                    + "region, convert to UTM.");
        }
        char bandOfLatitude = mgrs.getLatitudeZone();
        UPS_CONSTANT upsConst = UPS_CONSTANT.valueOf(String.valueOf(bandOfLatitude));

        char[] gridSquare = mgrs.getGridSquare();
        LETTER letter2 = LETTER.valueOf(String.valueOf(gridSquare[0]));
        LETTER letter3 = LETTER.valueOf(String.valueOf(gridSquare[1]));

        //calculate northing
        double gridNorthing = letter3.getIndex() * 100000 + upsConst.getFalseNorthing();
        if (letter3.compareTo(LETTER.I) > 0) {
            gridNorthing -= 100000.0;
        }
        if (letter3.compareTo(LETTER.O) > 0) {
            gridNorthing -= 100000.0;
        }

        //calculate easting
        double gridEasting = ((letter2.getIndex() - upsConst.getSecondLetterLow())
                * 100000 + upsConst.getFalseEasting());

        if (upsConst.getSecondLetterLow() != LETTER.A.getIndex()) {
            if (letter2.compareTo(LETTER.L) > 0) {
                gridEasting -= 300000.0;
            }
            if (letter2.compareTo(LETTER.U) > 0) {
                gridEasting -= 200000.0;
            }
        } else {
            if (letter2.compareTo(LETTER.C) > 0) {
                gridEasting -= 200000.0;
            }
            if (letter2.compareTo(LETTER.I) > 0) {
                gridEasting -= 100000.0;
            }
            if (letter2.compareTo(LETTER.L) > 0) {
                gridEasting -= 300000.0;
            }
        }

        double easting = mgrs.eastingValue(SI.METER) + gridEasting;
        double northing = mgrs.northingValue(SI.METER) + gridNorthing;

        return UPS.valueOf(easting, northing, hemisphere, SI.METER);
    }

    /**
     * Convert MGRS to UPS, step 2 in the geodetic to MGRS conversion when the
     * location falls within the polar regions
     *
     * @param ups
     * @return
     */
    protected MGRS upsToMgrs(UPS ups) {
        double northing = ups.northingValue(SI.METER);
        double easting = ups.eastingValue(SI.METER);
        int[] letters = new int[MGRS_LETTERS];

        if ((ups.eastingValue(SI.METER) < MIN_EAST_NORTH)
                || (ups.eastingValue(SI.METER) > MAX_EAST_NORTH)) {
            throw new ConversionException("Easting outside of valid range "
                    + "(100,000 to 900,000 meters for UTM) "
                    + "(0 to 4,000,000 meters for UPS)");
        }
        if ((ups.northingValue(SI.METER) < MIN_EAST_NORTH)
                || (ups.northingValue(SI.METER) > MAX_EAST_NORTH)) {
            throw new ConversionException("Northing outside of valid range "
                    + "(0 to 10,000,000 meters for UTM) "
                    + "(0 to 4,000,000 meters for UPS)");
        }

        double divisor = Math.pow(10.0, (5 - MAX_PRECISION));
        //easting = ConversionUtil.roundHalfUp(easting / divisor) * divisor;
        //northing = ConversionUtil.roundHalfUp(northing / divisor) * divisor;
        easting = (easting / divisor) * divisor;
        northing = (northing / divisor) * divisor;
        UPS_CONSTANT upsConst;

        if (ups.isNorthernHemisphere()) {
            if (easting >= TWOMIL) {
                letters[0] = LETTER.Z.getIndex();
                upsConst = UPS_CONSTANT.Z;
            } else {
                letters[0] = LETTER.Y.getIndex();
                upsConst = UPS_CONSTANT.Y;
            }
        } else if (easting >= TWOMIL) {
            letters[0] = LETTER.B.getIndex();
            upsConst = UPS_CONSTANT.B;
        } else {
            letters[0] = LETTER.A.getIndex();
            upsConst = UPS_CONSTANT.A;
        }

        double grid_northing = northing;
        grid_northing = grid_northing - upsConst.getFalseNorthing();
        letters[2] = (int) (grid_northing / ONEHT);

        if (letters[2] > LETTER.H.getIndex()) {
            letters[2] = letters[2] + 1;
        }

        if (letters[2] > LETTER.N.getIndex()) {
            letters[2] = letters[2] + 1;
        }

        double grid_easting = easting;
        grid_easting = grid_easting - upsConst.getFalseEasting();
        letters[1] = upsConst.getSecondLetterLow() + ((int) (grid_easting / ONEHT));

        if (easting < TWOMIL) {
            if (letters[1] > LETTER.L.getIndex()) {
                letters[1] = letters[1] + 3;
            }

            if (letters[1] > LETTER.U.getIndex()) {
                letters[1] = letters[1] + 2;
            }
        } else {
            if (letters[1] > LETTER.C.getIndex()) {
                letters[1] = letters[1] + 2;
            }

            if (letters[1] > LETTER.H.getIndex()) {
                letters[1] = letters[1] + 1;
            }

            if (letters[1] > LETTER.L.getIndex()) {
                letters[1] = letters[1] + 3;
            }
        }

        char[] gridSquare = {ALPHABET[letters[1]], ALPHABET[letters[2]]};

        easting = easting % 100000.0;

        if (easting >= 99999.5) {
            easting = 99999.0;
        }
        double east = (easting / divisor);
        easting = Double.valueOf(String.format("%" + MAX_PRECISION + "f", east).trim());
        northing = northing % 100000.0;
        if (northing >= 99999.5) {
            northing = 99999.0;
        }
        northing = (northing / divisor);

        return MGRS.valueOf(0, ALPHABET[letters[0]], gridSquare, easting, northing, SI.METER);
    }

    /**
     * The GridValues encapulates the algorithm used to determine the letter
     * range used for the 2nd letter in the MGRS coordinate string, based on the
     * set number of the utm zone. It also sets the pattern offset using a value
     * of A for the second letter of the grid square, based on the grid pattern
     * and set number of the utm zone.
     */
    private final class GridValues {

        private int secondLetterLow;
        private int secondLetterHigh;
        private final double patternOffset;

        public GridValues(int utmZone) {
            // Set number (1-6) based on UTM zone number
            int setNum;
            //Pattern based on ellipsoid code
            boolean aaPattern;

            setNum = utmZone % 6;

            if (setNum == 0) {
                setNum = 6;
            }

            aaPattern = !(mgrsEllipsoidCode.matches(CLARKE_1866) || mgrsEllipsoidCode.matches(CLARKE_1880)
                    || mgrsEllipsoidCode.matches(BESSEL_1841) || mgrsEllipsoidCode.matches(BESSEL_1841_NAMIBIA));

            switch (setNum) {
                case 1:
                //fall through to 4
                case 4:
                    secondLetterLow = LETTER.A.getIndex();
                    secondLetterHigh = LETTER.H.getIndex();
                    break;
                case 2:
                case 5:
                    secondLetterLow = LETTER.J.getIndex();
                    secondLetterHigh = LETTER.R.getIndex();
                    break;
                case 3:
                case 6:
                    secondLetterLow = LETTER.S.getIndex();
                    secondLetterHigh = LETTER.Z.getIndex();
                    break;
                default:
                    assert (false) : "Unexpected set number in GridValues";
            }

            /* False northing at A for second letter of grid square */
            if (aaPattern) {
                if ((setNum % 2) == 0) {
                    patternOffset = 500000.0;
                } else {
                    patternOffset = 0.0;
                }
            } else if ((setNum % 2) == 0) {
                patternOffset = 1500000.0;
            } else {
                patternOffset = 1000000.00;
            }
        }

        public double getPatternOffset() {
            return patternOffset;
        }

        public int getSecondLetterHighValue() {
            return secondLetterHigh;
        }

        public int getSecondLetterLowValue() {
            return secondLetterLow;
        }
    }
}

/**
 * Letter numeration, ported from geotrans
 *
 * @author steven.siebert
 */
enum LETTER {

    A,
    B,
    C(1100000.0, -72.0, -80.5, 0.0),
    D(2000000.0, -64.0, -72.0, 2000000.0),
    E(2800000.0, -56.0, -64.0, 2000000.0),
    F(3700000.0, -48.0, -56.0, 2000000.0),
    G(4600000.0, -40.0, -48.0, 4000000.0),
    H(5500000.0, -32.0, -40.0, 4000000.0),
    I,
    J(6400000.0, -24.0, -32.0, 6000000.0),
    K(7300000.0, -16.0, -24.0, 6000000.0),
    L(8200000.0, -8.0, -16.0, 8000000.0),
    M(9100000.0, 0.0, -8.0, 8000000.0),
    N(0.0, 8.0, 0.0, 0.0),
    O,
    P(800000.0, 16.0, 8.0, 0.0),
    Q(1700000.0, 24.0, 16.0, 0.0),
    R(2600000.0, 32.0, 24.0, 2000000.0),
    S(3500000.0, 40.0, 32.0, 2000000.0),
    T(4400000.0, 48.0, 40.0, 4000000.0),
    U(5300000.0, 56.0, 48.0, 4000000.0),
    V(6200000.0, 64.0, 56.0, 6000000.0),
    W(7000000.0, 72.0, 64.0, 6000000.0),
    X(7900000.0, 84.5, 72.0, 6000000.0),
    Y,
    Z;

    /* minimum northing for latitude band
     */
    private double minNorthing;
    /*
     * upper latitude for latitude band
     */
    private double upperLatitude;
    /*
     * lower latitude for latitude band
     */
    private double lowerLatitude;
    /*
     * latitude band northing offset
     */
    private double northingOffset;

    private LETTER() {
    }

    private LETTER(double minNorthing, double upperLatitude,
            double lowerLatitude, double northingOffset) {
        this();
        this.minNorthing = minNorthing;
        this.upperLatitude = upperLatitude;
        this.lowerLatitude = lowerLatitude;
        this.northingOffset = northingOffset;
    }

    public double getLowerLatitude() {
        return lowerLatitude;
    }

    public double getMinNorthing() {
        return minNorthing;
    }

    public double getNorthingOffset() {
        return northingOffset;
    }

    public double getUpperLatitude() {
        return upperLatitude;
    }

    /**
     * returns the integer index for the number
     *
     * currently we exploit the ordinal value of enumeration. by proxying
     * through this method, instead of directly using ordinal(), we provide a
     * layer of abstraction to permit flexible refactoring as needed.
     * Additionally, it keeps the vocabulary the same as is in geotrans
     *
     * @return
     */
    public int getIndex() {
        return this.ordinal();
    }
}

enum UPS_CONSTANT {

    A(LETTER.A.getIndex(), LETTER.J.getIndex(), LETTER.Z.getIndex(), LETTER.Z.getIndex(), 800000.0, 800000.0),
    B(LETTER.B.getIndex(), LETTER.A.getIndex(), LETTER.R.getIndex(), LETTER.Z.getIndex(), 2000000.0, 800000.0),
    Y(LETTER.Y.getIndex(), LETTER.J.getIndex(), LETTER.Z.getIndex(), LETTER.P.getIndex(), 800000.0, 1300000.0),
    Z(LETTER.Z.getIndex(), LETTER.A.getIndex(), LETTER.J.getIndex(), LETTER.P.getIndex(), 2000000.0, 1300000.0);
    private final int letter;
    private final int secondLetterLow;
    private final int secondLetterHigh;
    private final int thirdLetterHigh;
    private final double falseEasting;
    private final double falseNorthing;

    private UPS_CONSTANT(int letter, int secondLetterLow, int secondLetterHigh, int thirdLetterHigh, double falseEasting, double falseNorthing) {
        this.letter = letter;
        this.secondLetterLow = secondLetterLow;
        this.secondLetterHigh = secondLetterHigh;
        this.thirdLetterHigh = thirdLetterHigh;
        this.falseEasting = falseEasting;
        this.falseNorthing = falseNorthing;
    }

    public double getFalseEasting() {
        return falseEasting;
    }

    public double getFalseNorthing() {
        return falseNorthing;
    }

    public int getLetter() {
        return letter;
    }

    public int getSecondLetterHigh() {
        return secondLetterHigh;
    }

    public int getSecondLetterLow() {
        return secondLetterLow;
    }

    public int getThirdLetterHigh() {
        return thirdLetterHigh;
    }
}
