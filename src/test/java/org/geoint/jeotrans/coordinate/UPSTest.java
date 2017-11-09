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

import java.util.ArrayList;
import javax.measure.converter.ConversionException;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.LatLong;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Steve Siebert
 */
public class UPSTest {

    private static ArrayList<CoordinateTestCase> testCases;

    public UPSTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testCases = CoordinateTestCase.getDefaultTestCases();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of copy method, of class UPS.
     */
    @Test
    public void testCopy() {
        UPS first = UPS.valueOf(1698683, 2822343, 'S', SI.METER);
        UPS second = first.copy();
        assertFalse("Did not make a copy", first == second);
        assertTrue("UPS value mismatch", first.equals(second));
    }

    /**
     * Test of latLongToUps method, of class UPS.
     */
    @Test
    public void testLatLongToUps() {
        for (CoordinateTestCase t : testCases)
        {
            UPS expected = t.getExpectedUPS();
            if (expected != null)
            {
                UPS result =
                        UPS.latLongToUps(t.getLatLong());

                assertTrue("Resulting value not valid", expected.equals(result));
            }
        }
    }

    @Test
    public void testUpsToLatLong ()
    {
        for (CoordinateTestCase t : testCases)
        {
            UPS expected = t.getExpectedUPS();
            if (expected != null)
            {
                UPS result =
                        UPS.latLongToUps(t.getLatLong());
                assertTrue("Resulting value not valid", expected.equals(result));
            }
        }
    }
    
    /**
     * Ensure ConversionException is thrown for invalid southern latitude.
     */
    @Test(expected=ConversionException.class)
    public void testInvalidSouthernLatitude() {
        UPS.latLongToUps(
                LatLong.valueOf(-92.123456789, 20.123456789, NonSI.DEGREE_ANGLE));
    }
    
    /**
     * Ensure ConversionException is thrown for invalid northern latitude.
     */
    @Test(expected=ConversionException.class)
    public void testInvalidNorthernLatitude() {
         UPS.latLongToUps(
                LatLong.valueOf(92.123456789, 20.123456789, NonSI.DEGREE_ANGLE));
    }

}