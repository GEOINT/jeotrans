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

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javolution.text.Text;
import org.jscience.geography.coordinates.Coordinates;
import org.jscience.geography.coordinates.LatLong;
import org.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.crs.CoordinatesConverter;
import org.jscience.geography.coordinates.crs.ProjectedCRS;
import org.jscience.geography.coordinates.crs.UtmCRS;

/**
 * Universal Transverse Mercator projection
 *
 * The UTM projection is a complex system that uses 120 Transverse Mercator
 * projections to make up the main part of the globe, and uses Universal Polar
 * Stereographic (UPS) to represent locations in the northern and southern polar
 * regions.
 *
 * NOTE ABOUT PERSISTENCE ---------------------- The non-geodetic classs are
 * designed for conversion/presentation of geodetic coordinates and not intented
 * to be serialized or persisted. The intent is to store all coordinates in
 * geodetic form and convert to other forms (projected, geocentric, etc) as
 * needed.
 *
 * NOTATION -------- UTM notation is somewhat confusion as a result of a single
 * UTM point may actually be in two places on the earth (the northern and
 * southern hemispheres). In response to the lack of hemisphere notations, users
 * of the UTM system typically use one of two "shorthand" notations that add
 * this information but each results in confusion in their own way:
 *
 * The two notiations below reference the same point: 92°W, 38°N
 *
 * Prefix the UTM location with the longitude zone and hemisphere: 15N 587798
 * 4206287
 *
 * Prefix the UTM with the longitude zone and latitude zone: 15S 587798 4206287
 *
 * As you can see, this can get confusing (was the person talking about the
 * southern hemisphere or the 'S' band of latitude?).
 *
 * NGA has yet to come out with guidance on how to annotate this properly. For
 * this reason, this class provides methods specifically designed to allow
 * developers to annotate UTM how they desire. The important thing is that it
 * stays consistant throughout an application/problem domain. It should be noted
 * that this class defaults to the longitude zone/latitude zone format (this
 * differs from geotrans).
 *
 * @author Steve Siebert
 */
public final class UTM extends Coordinates<ProjectedCRS<?>> {

    /**
     * easting value in meters
     */
    private double easting;
    /**
     * northing value in meters
     */
    private double northing;
    /**
     * longitude zone number
     */
    private int utmZone;
    /**
     * latitude zone
     */
    private char latitudeZone;

    private static final int MIN_EASTING = 100000;
    private static final int MAX_EASTING = 900000;
    private static final int MIN_NORTHING = 0;
    private static final int MAX_NORTHING = 10000000;

    private static final int MAX_PRECISION = 3;

    /**
     * default CRS, used for most projections, so caching it here
     */
    private static final UtmCRS DEFAULT_CRS
            = new UtmCRS();
    /**
     * the CRS used to generate this projection
     */
    private UtmCRS CRS;

    //prevent object instantiation
    private UTM() {
    }

    /**
     * set the easting in meters
     *
     * @param easting
     */
    private void setEasting(double easting) {
        this.easting = easting;
    }

    /**
     * set the northing in meters
     *
     * @param northing
     */
    private void setNorthing(double northing) {
        this.northing = northing;
    }

    private void setLatitudeZone(char latitudeZone) {
        this.latitudeZone = latitudeZone;
    }

    private void setLongitudeZone(int longitudeZone) {
        this.utmZone = longitudeZone;
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
     * Return the longitude zone
     *
     * @return
     */
    public int getUtmZone() {
        return utmZone;
    }

    /**
     * Return the latitude zone
     *
     * @return
     */
    public char getLatitudeZone() {
        return latitudeZone;
    }

    /**
     * Determine if this UTM projection is within the northern hemisphere (not
     * including the northern polar)
     *
     * @return
     */
    public boolean isNorthernHemisphere() {
        return (latitudeZone > 'M');
    }

    /**
     * Determine if this UTM projection is within the southern hemisphere (not
     * including the southern polar)
     *
     * @return
     */
    public boolean isSouthernHemisphere() {
        return (latitudeZone < 'N');
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
     * Copy instance state to another object (different object reference)
     *
     * @return
     */
    @Override
    public UTM copy() {
        return UTM.valueOf(utmZone, latitudeZone, easting, northing, SI.METER);
    }

    /**
     * Return the UTM for the provided values
     *
     * NOTE: This method does minimal validation the values provided, use the
     * conversion utilities to validate content
     *
     * @param longitudeZone
     * @param latitudeZone
     * @param easting
     * @param northing
     * @param unit
     * @return
     */
    public static UTM valueOf(int longitudeZone, char latitudeZone, double easting,
            double northing, Unit<Length> unit) throws ConversionException {
        if ((easting < MIN_EASTING) || (easting > MAX_EASTING)) {
            throw new ConversionException("Easting outside of valid "
                    + "range (100,000 to 900,000 meters)");
        }
        if ((northing < MIN_NORTHING) || (northing > MAX_NORTHING)) {
            throw new ConversionException("Northing outside of valid "
                    + "range (0 to 10,000,000 meters)");
        }

        UTM utm = new UTM();

        if (unit == SI.METER) {
            utm.setEasting(easting);
            utm.setNorthing(northing);
        } else {
            UnitConverter toMeter = unit.getConverterTo(SI.METER);
            utm.setEasting(toMeter.convert(easting));
            utm.setNorthing(toMeter.convert(northing));
        }

        utm.setLongitudeZone(longitudeZone);
        utm.setLatitudeZone(latitudeZone);
        return utm;
    }

    /**
     * Create an instance using alternate CRS
     *
     * @param longitudeZone
     * @param latitudeZone
     * @param easting
     * @param northing
     * @param unit
     * @param crs
     * @return
     * @throws ConversionException
     */
    public static UTM valueOf(int longitudeZone, char latitudeZone, double easting,
            double northing, Unit<Length> unit, UtmCRS crs) throws ConversionException {
        UTM utm = valueOf(longitudeZone, latitudeZone, easting, northing, unit);
        utm.CRS = crs;
        return utm;
    }

    /**
     * Convenience method to convert geodetic to UTM
     *
     * @param latLong
     * @return
     */
    public static UTM latLongToUtm(LatLong latLong) {
        CoordinatesConverter<LatLong, UTM> latLongToUtm
                = LatLong.CRS.getConverterTo(UTM.DEFAULT_CRS);
        return latLongToUtm.convert(latLong);
    }

    /**
     * Convenience method to convert geodetic to UTM using alternate CRS
     * paramters
     *
     * @param latLong
     * @param semiMajor
     * @param flattening
     * @param override
     * @return
     */
    public static UTM latLongToUtm(LatLong latLong, double semiMajor,
            double flattening, int override) {
        UtmCRS crs = new UtmCRS(semiMajor, flattening, override);
        CoordinatesConverter<LatLong, UTM> latLongToUtm
                = LatLong.CRS.getConverterTo(crs);
        return latLongToUtm.convert(latLong);
    }

    /**
     * Convenience method to convert UTM to geodetic
     *
     * @param utm
     * @return
     */
    public static LatLong utmToLatLong(UTM utm) {
        CoordinatesConverter<UTM, LatLong> utmToLatLong
                = UTM.DEFAULT_CRS.getConverterTo(LatLong.CRS);
        return utmToLatLong.convert(utm);
    }

    /**
     * Convenience method to convert UTM to geodetic using alternate parameters
     *
     * @param utm
     * @param semiMajor
     * @param flattening
     * @param override
     * @return
     */
    public static LatLong utmToLatLong(UTM utm, double semiMajor,
            double flattening, int override) {
        UtmCRS crs = new UtmCRS(semiMajor, flattening, override);
        CoordinatesConverter<UTM, LatLong> utmToLatLong
                = crs.getConverterTo(LatLong.CRS);
        return utmToLatLong.convert(utm);
    }

    /**
     * Present in default UTM form. Easting and Northings are in meters.
     *
     * example: 15H 12345 12345
     *
     * @return
     */
    @Override
    public Text toText() {
        return new Text(asString());
    }

    String asString() {
        return String.format("%d%s %f %f",
                this.getUtmZone(),
                String.valueOf(this.getLatitudeZone()),
                eastingValue(SI.METER, MAX_PRECISION),
                northingValue(SI.METER, MAX_PRECISION));
    }

    /**
     * check if both UTM coordinates are equal
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UTM)) {
            return false;
        }

        UTM ref = (UTM) o;
        return (this.eastingValue(SI.METER, MAX_PRECISION) == ref.eastingValue(SI.METER, MAX_PRECISION))
                && (this.northingValue(SI.METER, MAX_PRECISION) == ref.northingValue(SI.METER, MAX_PRECISION))
                && (this.latitudeZone == ref.getLatitudeZone())
                && (this.utmZone == ref.getUtmZone());
    }

    /**
     * Simply groups UTM objects by their utm zone
     *
     * This is done simply because UTM is designed to be used strictly for
     * presentation. Anything more that simple presentation (ie. persistance)
     * should be done in geodetic form (LatLong).
     *
     * @return
     */
    @Override
    public int hashCode() {
        return getUtmZone();
    }

}
