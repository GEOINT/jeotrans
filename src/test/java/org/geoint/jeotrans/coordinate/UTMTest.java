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

import org.geoint.jeotrans.coordinate.UTM;
import java.util.ArrayList;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.LatLong;
import org.geoint.jeotrans.util.ConversionUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author steven.siebert.sa
 */
public class UTMTest {

    private static ArrayList<CoordinateTestCase> testCases;

    public UTMTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testCases = CoordinateTestCase.getDefaultTestCases();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of copy method, of class UTM.
     */
    @Test
    public void testCopy() {
        System.out.println("UTM.copy()");
        UTM first = UTM.valueOf(50, 'S', 666792, 3547343, SI.METER);
        UTM second = first.copy();
        assertFalse("Did not make a copy", first == second);
        assertTrue("UTM value mismatch", first.equals(second));
    }

    /**
     * Test of latLongToUtm method, of class UTM.
     */
    @Test
    public void testLatLongToUtm_LatLong() {
        System.out.println("UTM.latLongToUtm()");

        //iterate over the test cases
        for (CoordinateTestCase t : testCases)
        {
            UTM expected = t.getExpectedUTM();
            if (expected != null)
            {
                UTM result = UTM.latLongToUtm(t.getLatLong());
                System.out.print("Testing "+t.getLatLong().toString()+" " +
                        "(expected: "+expected.toText()+")   result: "+result.toText());
                
                assertTrue("Resulting value not valid", expected.equals(result));
                System.out.println("  successful");
            }
        }
    }

    /**
     * Test UTM to LatLong
     */
    @Test
    public void TestUtmToLatLong ()
    {
        System.out.println("UTM.utmToLatLong()");

        //iterate over the test cases
        for (CoordinateTestCase t : testCases)
        {
            UTM utm = t.getExpectedUTM();
            if (utm != null)
            {
                LatLong expected = t.getLatLong();
                System.out.print("Testing "+utm+" " +
                        "(expected: "+expected.toText());
                LatLong result = UTM.utmToLatLong(utm);
                System.out.println("   result: "+result.toText());
                assertTrue("Resulting latitude value not valid",
                        ConversionUtil.roundHalfUp(expected.latitudeValue(NonSI.DEGREE_ANGLE), 5) ==
                        ConversionUtil.roundHalfUp(result.latitudeValue(NonSI.DEGREE_ANGLE), 5));
                assertTrue ("Resulting longitude value is not valid",
                        ConversionUtil.roundHalfUp(expected.longitudeValue(NonSI.DEGREE_ANGLE), 5) ==
                        ConversionUtil.roundHalfUp(result.longitudeValue(NonSI.DEGREE_ANGLE), 5));
                System.out.println("  successful");
            }
        }
    }
}