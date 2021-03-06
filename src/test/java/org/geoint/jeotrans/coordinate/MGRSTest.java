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
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.geoint.jeotrans.util.ConversionUtil;
import org.jscience.geography.coordinates.LatLong;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Steve Siebert
 */
public class MGRSTest {

    private static ArrayList<CoordinateTestCase> testCases;
    private static final double MAX_GEODETIC_DELTA = .00001;

    public MGRSTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testCases = CoordinateTestCase.getDefaultTestCases();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of copy method, of class MGRS.
     */
    @Test
    public void testCopy() {
        //AUW9868322343
        char[] gridSquare = {'U', 'W'};
        MGRS first = MGRS.valueOf(0, 'A', gridSquare, 98683, 22343, SI.METER);
        MGRS second = first.copy();
        assertFalse("Did not make a copy", first == second);
        assertTrue("PS value mismatch", first.equals(second));
    }

    /**
     * Test of latLongToMgrs method, of class MGRS.
     */
    @Test
    public void testLatLongToMgrs() {

        //iterate over the test cases
        for (CoordinateTestCase t : testCases) {
            MGRS expected = t.getExpectedMGRS();

            if (t.getExpectedMGRS() != null) {
                MGRS result = MGRS.latLongToMgrs(t.getLatLong());

                assertTrue("Resulting easting value not valid",
                        expected.eastingValue(SI.METER, 0)
                        == result.eastingValue(SI.METER, 0));
                assertTrue("Result northing value is not valid",
                        expected.northingValue(SI.METER, 0)
                        == result.northingValue(SI.METER, 0));
            }
        }
    }

    /**
     * Test of mgrsToLatLong method, of class MGRS
     */
    @Test
    public void testMgrsToLatLong() {
        //iterate over the test cases
        for (CoordinateTestCase t : testCases) {
            LatLong expected = t.getLatLong();

            if (t.getExpectedMGRS() != null) {
                MGRS mgrs = MGRS.latLongToMgrs(t.getLatLong());
                LatLong result = MGRS.mgrsToLatLong(mgrs);

                assertEquals("Resulting latitude value not valid",
                        ConversionUtil.roundHalfUp(expected.latitudeValue(NonSI.DEGREE_ANGLE), 6),
                        ConversionUtil.roundHalfUp(result.latitudeValue(NonSI.DEGREE_ANGLE), 6),
                        MAX_GEODETIC_DELTA);
                assertEquals("Resulting longitude value not valid",
                        ConversionUtil.roundHalfUp(expected.longitudeValue(NonSI.DEGREE_ANGLE), 6),
                        ConversionUtil.roundHalfUp(result.longitudeValue(NonSI.DEGREE_ANGLE), 6),
                        MAX_GEODETIC_DELTA);
            }
        }
    }

    @Test
    public void tempTestCaseIssue3() {
        CoordinateTestCase is = new CoordinateTestCase(LatLong.valueOf(-0.338917, -90.669525, NonSI.DEGREE_ANGLE));
        MGRS.latLongToMgrs(is.getLatLong());
    }
}
