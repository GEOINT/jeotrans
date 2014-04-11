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
import org.jscience.geography.coordinates.crs.ProjectedCRS;
import org.jscience.geography.coordinates.crs.UpsCRS;

/**
 * Universal Polar Stereographic projection coordinate
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
public final class UPS extends Coordinates<ProjectedCRS<?>> {

    public static final char HEMISPHERE_SOUTH = 'S';
    public static final char HEMISPHERE_NORTH = 'N';

    /**
     * Northing in meters
     */
    private double northing;
    /**
     * Easting in meters
     */
    private double easting;
    /**
     * Hemisphere ('N' || 'S')
     */
    private char hemisphere;
    /**
     * default CRS, used for most projections, so caching it here
     */
    private static final UpsCRS DEFAULT_CRS =
            new UpsCRS();
    /**
     * the CRS used to generate this projection
     */
    private UpsCRS CRS;

    private static final int MAX_PRECISION = 3;
    
    private UPS() {
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
    public double northingValue(Unit<Length> unit, int precision)
    {
        if (precision > MAX_PRECISION)
        {
            precision = MAX_PRECISION;
        }
        if (precision < 0)
        {
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

    /**
     * Return the hemisphere character
     *
     * @return
     */
    public char getHemisphere() {
        return hemisphere;
    }

    /**
     * Determine if this coordinate falls within the northern hemisphere
     *
     * @return
     */
    public boolean isNorthernHemisphere() {
        return (hemisphere == HEMISPHERE_NORTH) ? true : false;
    }

    /**
     * Determine if this coordinate falls within the southern hemisphere
     *
     * @return
     */
    public boolean isSouthernHemisphere() {
        return (hemisphere == HEMISPHERE_SOUTH) ? true : false;
    }

    @Override
    public ProjectedCRS<?> getCoordinateReferenceSystem() {
        return CRS;
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
     * Copy instance state to another object (difference object reference)
     *
     * @return
     */
    @Override
    public UPS copy() {
        return valueOf(easting, northing, hemisphere, SI.METER);
    }

    /**
     * Create a UPS coordinate instance from component values
     *
     * @param easting
     * @param northing
     * @param hemisphere
     * @param unit
     * @return
     */
    public static UPS valueOf(double easting, double northing, char hemisphere,
            Unit<Length> unit) {
        UPS ups = new UPS();
        ups.hemisphere = hemisphere;

        if (unit == SI.METER) {
            ups.setEasting(easting);
            ups.setNorthing(northing);
        } else {
            UnitConverter toMeter = unit.getConverterTo(SI.METER);
            ups.setEasting(toMeter.convert(easting));
            ups.setNorthing(toMeter.convert(northing));
        }
        return ups;
    }

    /**
     * Create a UPS coordinate instance from component values with alternate
     * CRS parameters
     *
     * @param easting
     * @param northing
     * @param hemisphere
     * @param unit
     * @param crs
     * @return
     */
    public static UPS valueOf(double easting, double northing, char hemisphere,
            Unit<Length> unit, UpsCRS crs) {
        UPS ups = valueOf(easting, northing, hemisphere, unit);
        ups.CRS = crs;
        return ups;
    }

    /**
     * Convenience method to convert geodetic to UPS
     *
     * @param latLong
     * @return
     */
    public static UPS latLongToUps(LatLong latLong) {
        CoordinatesConverter<LatLong, UPS> latLongToUps =
                LatLong.CRS.getConverterTo(UPS.DEFAULT_CRS);
        return latLongToUps.convert(latLong);
    }

    /**
     * Convenience method to convert geodetic to UPS using alternate
     * CRS parameters
     *
     * @param latLong
     * @param semiMajor
     * @param flattening
     * @return
     */
    public static UPS latLongToUps(LatLong latLong, double semiMajor,
            double flattening) {
        UpsCRS crs = new UpsCRS(semiMajor, flattening);
        CoordinatesConverter<LatLong, UPS> latLongToUps =
                LatLong.CRS.getConverterTo(crs);
        return latLongToUps.convert(latLong);
    }

    /**
     * Convenience method to convert UPS to geodetic
     *
     * @param ups
     * @return
     */
    public static LatLong upsToLatLong(UPS ups) {
        CoordinatesConverter<UPS, LatLong> upsToLatLong =
                UPS.DEFAULT_CRS.getConverterTo(LatLong.CRS);
        return upsToLatLong.convert(ups);
    }

    /**
     * Convenience method to convert UPS to geodetic using alternate CRS
     * parameters
     *
     * @param ups
     * @param semiMajor
     * @param flattening
     * @return
     */
    public static LatLong upsToLatLong(UPS ups, double semiMajor, double flattening) {
        UpsCRS crs = new UpsCRS(semiMajor, flattening);
        CoordinatesConverter<UPS, LatLong> upsToLatLong =
                crs.getConverterTo(LatLong.CRS);
        return upsToLatLong.convert(ups);
    }

    /**
     * Default UPS output format
     *
     * Example: 1234.123 1234.123N or 1234.123 1234.123S (north/south hemisphere)
     * @return
     */
    @Override
    public Text toText ()
    {
        return new Text(eastingValue(SI.METER, MAX_PRECISION)+" "+northingValue(SI.METER, MAX_PRECISION)+hemisphere);
    }

    @Override
    public boolean equals (Object o)
    {
        if (!(o instanceof UPS))
        {
            return false;
        }
        UPS ups = (UPS) o;
        if (ups.eastingValue(SI.METER, MAX_PRECISION) == eastingValue(SI.METER, MAX_PRECISION) &&
                ups.northingValue(SI.METER, MAX_PRECISION) == northingValue(SI.METER, MAX_PRECISION) &&
                ups.getHemisphere() == hemisphere)
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.northing) ^ (Double.doubleToLongBits(this.northing) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.easting) ^ (Double.doubleToLongBits(this.easting) >>> 32));
        hash = 17 * hash + this.hemisphere;
        return hash;
    }
}
