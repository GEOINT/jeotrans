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

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javolution.text.Text;
import org.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.Coordinates;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.CoordinatesConverter;
import org.jscience.geography.coordinates.crs.ProjectedCRS;
import org.jscience.geography.coordinates.crs.TransverseMercatorCRS;

/**
 * Transverse Mercator projection coordinate
 *
 * NOTE ABOUT PERSISTENCE ---------------------- The non-geodetic classs are
 * designed for conversion/presentation of geodetic coordinates and not intented
 * to be serialized or persisted. The intent is to store all coordinates in
 * geodetic form and convert to other forms (projected, geocentric, etc) as
 * needed.
 *
 * @author Steve Siebert
 */
public final class TransverseMercator extends Coordinates<ProjectedCRS<?>> {

    private double northing; //in meters
    private double easting; //in meters

    private static final int MAX_PRECISION = 3;

    /**
     * default CRS, used for most projections, so caching it here
     */
    private static final TransverseMercatorCRS DEFAULT_CRS
            = new TransverseMercatorCRS();
    /**
     * the CRS used to generate this projection
     */
    private TransverseMercatorCRS CRS;

    /**
     * Use valueOf()
     */
    private TransverseMercator() {
    }

    /**
     * set the easting in meters
     *
     * @param easting
     */
    private void setEasting(double easting) {
        this.easting = ConversionUtil.roundHalfUp(easting, MAX_PRECISION);
    }

    /**
     * set the northing in meters
     *
     * @param northing
     */
    private void setNorthing(double northing) {
        this.northing = ConversionUtil.roundHalfUp(northing, MAX_PRECISION);
    }

    /**
     * Return the northing value in requested unit
     *
     * @param unit
     * @return
     */
    public double northingValue(Unit<Length> unit) {
        return unit.equals(SI.METER) ? northing : SI.METER.getConverterTo(unit).convert(northing);
    }

    /**
     * Return easting value in requested unit
     *
     * @param unit
     * @return
     */
    public double eastingValue(Unit<Length> unit) {
        return unit.equals(SI.METER) ? easting : SI.METER.getConverterTo(unit).convert(easting);
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

    /**
     * Returns the CRS used in this projection instance
     *
     * @return
     */
    @Override
    public TransverseMercatorCRS getCoordinateReferenceSystem() {
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
     * Copy instance state to another object (different object reference)
     *
     * @return
     */
    @Override
    public TransverseMercator copy() {
        return TransverseMercator.valueOf(easting, northing, SI.METER);
    }

    /**
     * Convenience method that converts geodetic (LatLong) coordinates to
     * Transverse Mercator projection according to current ellipsoid and default
     * Transverse Mercator projection parameters.
     *
     * @param latLong
     * @return
     */
    public static TransverseMercator latLongToTransverseMercator(LatLong latLong) {
        CoordinatesConverter<LatLong, TransverseMercator> latLongToTM
                = LatLong.CRS.getConverterTo(DEFAULT_CRS);
        return latLongToTM.convert(latLong);
    }

    /**
     * Convenience method that converts geodetic (LatLong) coordinates to
     * Transverse Mercator projection according to current ellipsoid and
     * provided Transverse Mercator projection parameters
     *
     * @param latLong
     * @param semiMajorAxis
     * @param flattening
     * @param originLatitude
     * @param centralMeridian
     * @param falseEasting
     * @param falseNorthing
     * @param scale
     * @return
     */
    public static TransverseMercator latLongToTransverseMercator(LatLong latLong,
            double semiMajorAxis, double flattening,
            double originLatitude, double centralMeridian, double falseEasting,
            double falseNorthing, double scale) {
        TransverseMercatorCRS tmcrs = new TransverseMercatorCRS(semiMajorAxis,
                flattening, originLatitude, centralMeridian, falseEasting,
                falseNorthing, scale);
        CoordinatesConverter<LatLong, TransverseMercator> latLongToTM
                = LatLong.CRS.getConverterTo(tmcrs);
        return latLongToTM.convert(latLong);
    }

    /**
     * Convenience method converts Transverse Mercator projection coordinates to
     * geodetic (LatLong) coordinates according to current ellisoid and default
     * Transverse Mercator projection parameters.
     *
     * @param tm
     * @return
     */
    public static LatLong transverseMercatorToLatLong(TransverseMercator tm) {
        CoordinatesConverter<TransverseMercator, LatLong> tmToLatLong
                = TransverseMercator.DEFAULT_CRS.getConverterTo(LatLong.CRS);
        return tmToLatLong.convert(tm);
    }

    /**
     * Converts Transverse Mercator projection coordinates to geodetic (LatLong)
     * coordinates according to current ellisoid and provided Transverse
     * Mercator projection parameters.
     *
     * @param tm
     * @param semiMajorAxis
     * @param flattening
     * @param falseEasting
     * @param originLatitude
     * @param centralMeridian
     * @param falseNorthing
     * @param scale
     * @return
     */
    public static LatLong transverseMercatorToLatLong(TransverseMercator tm, double semiMajorAxis, double flattening,
            double originLatitude, double centralMeridian, double falseEasting,
            double falseNorthing, double scale) {
        TransverseMercatorCRS tmcrs = new TransverseMercatorCRS(semiMajorAxis,
                flattening, originLatitude, centralMeridian, falseEasting,
                falseNorthing, scale);
        tm.CRS = tmcrs;
        CoordinatesConverter<TransverseMercator, LatLong> tmToLatLong
                = tmcrs.getConverterTo(LatLong.CRS);
        return tmToLatLong.convert(tm);
    }

    /**
     * Return the Transverse Mercator projection for the provided values,
     * assuming the default CRS
     *
     * @param easting
     * @param northing
     * @param unit
     * @return
     */
    public static TransverseMercator valueOf(double easting, double northing,
            Unit<Length> unit) {
        TransverseMercator tm = new TransverseMercator();
        if (unit == SI.METER) {
            tm.setEasting(easting);
            tm.setNorthing(northing);
        } else {
            UnitConverter toMeter = unit.getConverterTo(SI.METER);
            tm.setEasting(toMeter.convert(easting));
            tm.setNorthing(toMeter.convert(northing));
        }
        return tm;
    }

    /**
     * Return the Transverse Mercator projection for the provided values,
     * setting a custom CRS
     *
     * NOTE: This method does not validate the values provided, use the
     * conversion utilities to validate content
     *
     * @param easting
     * @param northing
     * @param unit
     * @param crs
     * @return
     */
    public static TransverseMercator valueOf(double easting, double northing,
            Unit<Length> unit, TransverseMercatorCRS crs) {
        TransverseMercator tm = valueOf(easting, northing, unit);
        tm.CRS = crs;
        return tm;
    }

    /**
     * Return default TM format
     *
     * Example: 1234.123m 1234.123m
     *
     * @return
     */
    @Override
    public Text toText() {
        return new Text(asString());
    }

    String asString() {
        return String.format("%fm %fm",
                eastingValue(SI.METER, 0),
                northingValue(SI.METER, 0));
    }

    /**
     * Check if the values are equal
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransverseMercator)) {
            return false;
        }
        TransverseMercator tm = (TransverseMercator) o;
        return easting == tm.eastingValue(SI.METER)
                && northing == tm.northingValue(SI.METER);
    }

    /**
     * Standard generated hashcode
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.northing) ^ (Double.doubleToLongBits(this.northing) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.easting) ^ (Double.doubleToLongBits(this.easting) >>> 32));
        return hash;
    }

}
