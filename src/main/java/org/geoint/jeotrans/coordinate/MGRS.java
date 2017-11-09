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
package org.geoint.jeotrans.coordinate;

import java.util.Arrays;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javolution.text.Text;
import org.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.Coordinates;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.CoordinatesConverter;
import org.jscience.geography.coordinates.crs.MgrsCRS;
import org.jscience.geography.coordinates.crs.ProjectedCRS;

/**
 * Military Grid Reference System projection coordinate
 *
 * NOTE ABOUT PERSISTENCE ---------------------- The non-geodetic classs are
 * designed for conversion/presentation of geodetic coordinates and not intented
 * to be serialized or persisted. The intent is to store all coordinates in
 * geodetic form and convert to other forms (projected, geocentric, etc) as
 * needed.
 *
 * @author Steve Siebert
 */
public final class MGRS extends Coordinates<ProjectedCRS<?>> {

    /**
     * northing in meters
     */
    private double northing;
    /**
     * easting in meters
     */
    private double easting;
    /**
     * utmZone zone
     *
     */
    private int utmZone;
    /**
     * latitude zone
     *
     */
    private char latitudeZone;
    /**
     * 2-character grid square
     */
    private char[] gridSquare = new char[2];
    /**
     * default CRS, used for most projections, so caching it here
     */
    private static final MgrsCRS DEFAULT_CRS
            = new MgrsCRS();
    /**
     * the CRS used to generate this projection
     */
    private MgrsCRS CRS;

    private static final int MAX_PRECISION = 3;

    private MGRS() {
    }

    private void setEasting(double easting) {
        this.easting = easting;
    }

    private void setGridSquare(char[] gridSquare) {
        this.gridSquare = gridSquare;
    }

    private void setLatitudeZone(char latitudeZone) {
        this.latitudeZone = latitudeZone;
    }

    private void setUtmZone(int longitudeZone) {
        this.utmZone = longitudeZone;
    }

    private void setNorthing(double northing) {
        this.northing = northing;
    }

    /**
     * Return the northing value in requested unit
     *
     * @param unit
     * @return
     */
    public double northingValue(Unit<Length> unit) {
        return unit.equals(SI.METER) ? northing
                : SI.METER.getConverterTo(unit).convert(northing);
    }

    /**
     * Return easting value in requested unit
     *
     * @param unit
     * @return
     */
    public double eastingValue(Unit<Length> unit) {
        return unit.equals(SI.METER) ? easting
                : SI.METER.getConverterTo(unit).convert(easting);
    }

    /**
     * Return the northing value in requested unit with requested decimal
     * precision
     *
     * @param unit
     * @param precision
     * @return
     */
    public double northingValue(Unit<Length> unit, int precision) {
        if (precision > MAX_PRECISION) {
            precision = MAX_PRECISION;
        }
        if (precision < 0) {
            precision = 0;
        }

        return ConversionUtil.roundHalfUp(northingValue(unit), precision);
    }

    /**
     * Return the easting value in requested unit with requested decimal
     * precision
     *
     * @param unit
     * @param precision
     * @return
     */
    public double eastingValue(Unit<Length> unit, int precision) {
        if (precision > MAX_PRECISION) {
            precision = MAX_PRECISION;
        }
        if (precision < 0) {
            precision = 0;
        }
        return ConversionUtil.roundHalfUp(eastingValue(unit), precision);
    }

    public char[] getGridSquare() {
        return gridSquare;
    }

    public char getLatitudeZone() {
        return latitudeZone;
    }

    public int getUtmZone() {
        return utmZone;
    }

    public boolean isNorthPolar() {
        return latitudeZone == 'Y' || latitudeZone == 'Z';
    }

    public boolean isSouthPolar() {
        return latitudeZone == 'A' || latitudeZone == 'B';
    }

    @Override
    public ProjectedCRS<?> getCoordinateReferenceSystem() {
        if (CRS == null) {
            CRS = DEFAULT_CRS;
        }
        return CRS;
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:
                Unit<?> u0 = ProjectedCRS.EASTING_NORTHING_CS.getAxis(0).getUnit();
                return SI.METER.getConverterTo(u0).convert(easting);

            case 1:
                Unit<?> u1 = ProjectedCRS.EASTING_NORTHING_CS.getAxis(1).getUnit();
                return SI.METER.getConverterTo(u1).convert(northing);

            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Copy the MGRS state value to another MGRS object (different object
     * reference)
     *
     * @return
     */
    @Override
    public MGRS copy() {
        return valueOf(getUtmZone(), getLatitudeZone(), getGridSquare(),
                eastingValue(SI.METER), northingValue(SI.METER), SI.METER);
    }

    /**
     * Create an MGRS value from components
     *
     * @param longitudeZone
     * @param latitudeZone
     * @param gridSquare
     * @param easting
     * @param northing
     * @param unit
     * @return
     */
    public static MGRS valueOf(int longitudeZone, char latitudeZone,
            char[] gridSquare, double easting, double northing, Unit<Length> unit) {
        MGRS mgrs = new MGRS();
        mgrs.setUtmZone(longitudeZone);
        mgrs.setLatitudeZone(latitudeZone);
        mgrs.setGridSquare(gridSquare);

        if (unit == SI.METER) {
            mgrs.setEasting(easting);
            mgrs.setNorthing(northing);
        } else {
            UnitConverter toMeter = unit.getConverterTo(SI.METER);
            mgrs.setEasting(toMeter.convert(easting));
            mgrs.setNorthing(toMeter.convert(northing));
        }
        return mgrs;
    }

    /**
     * Create MGRS instance from components and alternate CRS values
     *
     * @param longitudeZone
     * @param latitudeZone
     * @param gridSquare
     * @param easting
     * @param northing
     * @param unit
     * @param crs
     * @return
     */
    public static MGRS valueOf(int longitudeZone, char latitudeZone,
            char[] gridSquare, double easting, double northing,
            Unit<Length> unit, MgrsCRS crs) {
        MGRS mgrs = valueOf(longitudeZone, latitudeZone, gridSquare, easting,
                northing, unit);
        mgrs.CRS = crs;
        return mgrs;
    }

    /**
     * Convenience method to convert MGRS to LatLong
     *
     * @param mgrs
     * @return
     */
    public static LatLong mgrsToLatLong(MGRS mgrs) {
        CoordinatesConverter<MGRS, LatLong> mgrsToLatLong
                = MGRS.DEFAULT_CRS.getConverterTo(LatLong.CRS);
        return mgrsToLatLong.convert(mgrs);
    }

    /**
     * Convenience method to convert LatLong to MGRS
     *
     * @param latLong
     * @return
     */
    public static MGRS latLongToMgrs(LatLong latLong) {
        CoordinatesConverter<LatLong, MGRS> latLongToMgrs
                = LatLong.CRS.getConverterTo(MGRS.DEFAULT_CRS);
        return latLongToMgrs.convert(latLong);
    }

    /**
     * Default text output for MGRS
     *
     * Example: A UW 00123 00123
     *
     * @return
     */
    @Override
    public Text toText() {
        return new Text(asString());
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        if (utmZone != 0) {
            sb.append(utmZone);
        }
        sb.append(latitudeZone)
                .append(" ")
                .append(gridSquare)
                .append(" ")
                .append(String.format("%05d", (int) eastingValue(SI.METER, 0)))
                .append(" ")
                .append(String.format("%05d", (int) northingValue(SI.METER, 0)));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MGRS)) {
            return false;
        }
        MGRS mgrs = (MGRS) o;
        return mgrs.eastingValue(SI.METER, MAX_PRECISION) == eastingValue(SI.METER, MAX_PRECISION)
                && mgrs.northingValue(SI.METER, MAX_PRECISION) == northingValue(SI.METER, MAX_PRECISION)
                && mgrs.getLatitudeZone() == getLatitudeZone()
                && mgrs.getUtmZone() == getUtmZone()
                && Arrays.equals(mgrs.getGridSquare(), getGridSquare());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.northing) ^ (Double.doubleToLongBits(this.northing) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.easting) ^ (Double.doubleToLongBits(this.easting) >>> 32));
        hash = 37 * hash + this.utmZone;
        hash = 37 * hash + this.latitudeZone;
        hash = 37 * hash + Arrays.hashCode(this.gridSquare);
        return hash;
    }

}
