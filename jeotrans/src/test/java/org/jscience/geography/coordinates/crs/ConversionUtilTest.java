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

import gov.ic.geoint.jeotrans.util.ConversionUtil;
import javax.measure.unit.NonSI;
import org.jscience.geography.coordinates.LatLong;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Steve Siebert
 */
public class ConversionUtilTest {

    public ConversionUtilTest() {
    }

    /**
     * Test of getLatitudeZone method, of class ConversionUtil.
     */
    @Test
    public void testGetLatitudeZone_double_Unit() {
        System.out.println("ConversionUtil.getLatitudeZone()");

        //iterate over all the Zone values, testing the correct zone code
        //is resolved
        for (Zones z : Zones.values())
        {
            LatLong latLong = LatLong.valueOf(z.getLowerLatitude()+2,
                    z.getLongitude(), NonSI.DEGREE_ANGLE);
            char result = ConversionUtil.getLatitudeZone(latLong);
            System.out.print("Testing Zone: "+z.name()+"   " +
                    "Lat: "+latLong.latitudeValue(NonSI.DEGREE_ANGLE)+"    " +
                    "Long: "+latLong.longitudeValue(NonSI.DEGREE_ANGLE));
            String error = "Latitude "+(z.lowerLatitude+2)+" incorrectly mapped to "+
                        Zones.valueOf(String.valueOf(result)).name();
            assertEquals(error, z, Zones.valueOf(String.valueOf(result)));
            System.out.println("   successful");
        }
    }

    enum Zones {

        A(-80.5, -90.0, -5.0),
        B(-80.5, -90.0, 5.0),
        C(-72.0, -80.5, 100.2),
        D(-64.0, -72.0, 100.2),
        E(-56.0, -64.0, 100.2),
        F(-48.0, -56.0, 100.2),
        G(-40.0, -48.0, 100.2),
        H(-32.0, -40.0, 100.2),
        J(-24.0, -32.0, 100.2),
        K(-16.0, -24.0, 100.2),
        L(-8.0, -16.0, 100.2),
        M(0.0, -8.0, 100.2),
        N(8.0, 0.0, 100.2),
        P(16.0, 8.0, 100.2),
        Q(24.0, 16.0, 100.2),
        R(32.0, 24.0, 100.2),
        S(40.0, 32.0, 100.2),
        T(48.0, 40.0, 100.2),
        U(56.0, 48.0, 100.2),
        V(64.0, 56.0, 100.2),
        W(72.0, 64.0, 100.2),
        X(84.5, 72.0, 100.2),
        Y(90.0, 84.5, -5.0),
        Z(90.0, 84.5, 5.0);

        private double upperLatitude;
        private double lowerLatitude;
        private double longitude;

        private Zones(  double upperLatitude,
                        double lowerLatitude,
                        double longitude) {
            this.upperLatitude = upperLatitude;
            this.lowerLatitude = lowerLatitude;
            this.longitude = longitude;
        }

        public double getLowerLatitude() {
            return lowerLatitude;
        }

        public double getUpperLatitude() {
            return upperLatitude;
        }

        public double getLongitude() {
            return longitude;
        }

        
    }
}
