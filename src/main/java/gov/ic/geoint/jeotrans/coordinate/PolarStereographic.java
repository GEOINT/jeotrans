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

package gov.ic.geoint.jeotrans.coordinate;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javolution.text.Text;
import org.jscience.geography.coordinates.Coordinates;
import org.jscience.geography.coordinates.LatLong;
import gov.ic.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.crs.CoordinatesConverter;
import org.jscience.geography.coordinates.crs.PolarStereographicCRS;
import org.jscience.geography.coordinates.crs.ProjectedCRS;

/**
 * Polar Stereographic projection coordinate
 *
 * NOTE ABOUT PERSISTENCE
 * ----------------------
 * The non-geodetic classs are designed for conversion/presentation of
 * geodetic coordinates and not intented to be serialized or persisted.
 * The intent is to store all coordinates in geodetic form and convert to
 * other forms (projected, geocentric, etc) as needed.
 * 
 * @author Steve Siebert
 */
public final class PolarStereographic extends Coordinates<ProjectedCRS<?>>
{

    private double northing;
    private double easting;
    private static PolarStereographicCRS DEFAULT_CRS =
            new PolarStereographicCRS();
    private PolarStereographicCRS CRS;

    private static final int MAX_PRECISION = 3;

    @Override
    public PolarStereographicCRS getCoordinateReferenceSystem() {
        if (CRS == null)
        {
            CRS = DEFAULT_CRS;
        }
        return CRS;
    }

    private void setEasting(double easting) {
        this.easting = easting;
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
        return unit.equals(SI.METER) ? northing :
            SI.METER.getConverterTo(unit).convert(northing);
    }

    /**
     * Return easting value in requested unit
     *
     * @param unit
     * @return
     */
    public double eastingValue(Unit<Length> unit) {
        return unit.equals(SI.METER) ? easting :
            SI.METER.getConverterTo(unit).convert(easting);
    }

    /**
     * Return the northing value in the requested unit with the requested
     * decimal precision
     *
     * @param unit
     * @param precision
     * @return
     */
    public double northingValue (Unit<Length> unit, int precision)
    {
        if (precision > MAX_PRECISION)
        {
            precision = MAX_PRECISION;
        }
        if (precision < 0)
        {
            precision = 0;
        }
        return ConversionUtil.roundHalfUp(northingValue(unit),  precision);
    }

    /**
     * Return the easting value in the requested unit with the requested
     * decimal precision
     *
     * @param unit
     * @param percision
     * @return
     */
    public double eastingValue (Unit<Length> unit, int precision)
    {
        if (precision > MAX_PRECISION)
        {
            precision = MAX_PRECISION;
        }
        if (precision < 0)
        {
            precision = 0;
        }
        return ConversionUtil.roundHalfUp(eastingValue(unit), precision);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        if (dimension == 1) {
            Unit<?> u = ProjectedCRS.EASTING_NORTHING_CS.getAxis(0).getUnit();
            return SI.METER.getConverterTo(u).convert(easting);
        } else if (dimension == 1) {
            Unit<?> u = ProjectedCRS.EASTING_NORTHING_CS.getAxis(1).getUnit();
            return SI.METER.getConverterTo(u).convert(northing);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Copy the instance state to another object (different object reference)
     *
     * @return
     */
    @Override
    public PolarStereographic copy() {
        return valueOf(easting, northing, SI.METER);
    }

    /**
     * Create an instance from component values
     *
     * @param easting
     * @param northing
     * @param unit
     * @return
     */
    public static PolarStereographic valueOf (double easting, double northing,
            Unit<Length> unit)
    {
        PolarStereographic ps = new PolarStereographic();
        if (unit == SI.METER) {
            ps.setEasting(easting);
            ps.setNorthing(northing);
        } else {
            UnitConverter toMeter = unit.getConverterTo(SI.METER);
            ps.setEasting(toMeter.convert(easting));
            ps.setNorthing(toMeter.convert(northing));
        }
        return ps;
    }

    /**
     * Create an instance from given component values and alternate CRS
     *
     * @param easting
     * @param northing
     * @param unit
     * @param crs
     * @return
     */
    public static PolarStereographic valueOf (double easting, double northing,
            Unit<Length> unit, PolarStereographicCRS crs)
    {
        PolarStereographic ps = valueOf(easting, northing, unit);
        ps.CRS = crs;
        return ps;
    }

    /**
     * Convenience method to convert Geodetic to Polar Stereographic
     *
     * @param latLong
     * @return
     */
    public static PolarStereographic latLongToPolarStereographic (LatLong latLong)
    {
        CoordinatesConverter<LatLong, PolarStereographic> latLongToPS =
                LatLong.CRS.getConverterTo(DEFAULT_CRS);
        return latLongToPS.convert(latLong);
    }

    /**
     * Convenience method to convert geodetic to Polar Stereographic with
     * alternate algorithm values
     *
     * NOTE:  This should only be needed from within the jeotrans library
     * 
     * @param latLong
     * @param a semiMaj
     * @param f flattening
     * @param latitudeScale
     * @param longitudeFromPole
     * @param falseEasting
     * @param falseNorthing
     * @return
     */
    public static PolarStereographic latLongToPolarStereographic (LatLong latLong,
            double a, double f, double latitudeScale, double longitudeFromPole,
            double falseEasting, double falseNorthing)
    {
        PolarStereographicCRS crs = new PolarStereographicCRS
                (a, f, latitudeScale, longitudeFromPole, falseEasting,
                falseNorthing);
        CoordinatesConverter<LatLong, PolarStereographic> latLongToPS =
                LatLong.CRS.getConverterTo(crs);
        return latLongToPS.convert(latLong);
    }

    /**
     * Concenience method to convert Polar Stereographic to geodetic
     *
     * @param ps
     * @return
     */
    public static LatLong polarStereographicToLatLong (PolarStereographic ps)
    {
        CoordinatesConverter<PolarStereographic, LatLong> psToLatLong =
                PolarStereographic.DEFAULT_CRS.getConverterTo(LatLong.CRS);
        return psToLatLong.convert(ps);
    }

    /**
     * Convenience method to convert polar stereographic to geodetic using
     * alternate CRS parameters
     * 
     * @param ps
     * @param a semiMaj
     * @param f flattening
     * @param latitudeScale
     * @param longitudeFromPole
     * @param falseEasting
     * @param falseNorthing
     * @return
     */
    public static LatLong polarStereographicToLatLong (PolarStereographic ps,
            double a, double f, double latitudeScale, double longitudeFromPole,
            double falseEasting, double falseNorthing)
    {
        PolarStereographicCRS crs = new PolarStereographicCRS
                (a, f, latitudeScale, longitudeFromPole, falseEasting,
                falseNorthing);
        CoordinatesConverter<PolarStereographic, LatLong> psToLatLong =
                crs.getConverterTo(LatLong.CRS);
        return psToLatLong.convert(ps);
    }

    /**
     * Default Polar Stereographic output
     *
     * Example: 01234.123m 01234.123m
     * @return
     */
    @Override
    public Text toText ()
    {
        return new Text(eastingValue(SI.METER, MAX_PRECISION)+"m "+northingValue(SI.METER, MAX_PRECISION)+"m");
    }

    @Override
    public boolean equals (Object o)
    {
        if (!(o instanceof PolarStereographic))
        {
            return false;
        }
        PolarStereographic ps = (PolarStereographic) o;
        if (ps.eastingValue(SI.METER, MAX_PRECISION) == eastingValue(SI.METER, MAX_PRECISION) &&
                ps.northingValue(SI.METER, MAX_PRECISION) == northingValue(SI.METER, MAX_PRECISION))
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.northing) ^ (Double.doubleToLongBits(this.northing) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.easting) ^ (Double.doubleToLongBits(this.easting) >>> 32));
        return hash;
    }

}
