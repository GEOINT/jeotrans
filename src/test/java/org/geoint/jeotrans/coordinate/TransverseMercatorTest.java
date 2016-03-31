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

import org.geoint.jeotrans.coordinate.TransverseMercator;
import java.util.ArrayList;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.LatLong;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author steven.siebert.sa
 */
public class TransverseMercatorTest {

    private static ArrayList<CoordinateTestCase> testCases;

    public TransverseMercatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testCases = CoordinateTestCase.getDefaultTestCases();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of copy method, of class TransverseMercator.
     */
    @Test
    public void testCopy() {
        System.out.println("TransverseMercator.copy()");
        TransverseMercator first = TransverseMercator.valueOf(3845070, 8248570, SI.METER);
        TransverseMercator second = first.copy();
        assertFalse("Did not make a copy", first == second);
        assertTrue("TM value mismatch", first.equals(second));
    }

    /**
     * Test of latLongToTransverseMercator method, of class TransverseMercator.
     */
    @Test
    public void testLatLongToTransverseMercator_LatLong() {
        
        System.out.println("TransverseMercator.latLongToTransverseMercator(LatLong)");
        //iterate over the test cases
        for (CoordinateTestCase t : testCases)
        {
            TransverseMercator expected = t.getExpectedTM();
            if (expected != null)
            {
                TransverseMercator result =
                        TransverseMercator.latLongToTransverseMercator(t.getLatLong());
                System.out.print("Testing "+t.getLatLong().toString()+" " +
                        "(expected: "+expected.toText()+")   result: "+result.toText());

                //we need to assert equals based on reduced precision
                assertTrue("Resulting easting value is not valid",
                        expected.eastingValue(SI.METER, 0) ==
                        result.eastingValue(SI.METER, 0));
                assertTrue("Resulting northing value is not valid",
                        expected.northingValue(SI.METER, 0) ==
                        result.northingValue(SI.METER, 0));
                System.out.println("successful");
            }
        }
    }

    /**
     * In this test, we convert a known good coordinate (that will convert to
     * TM without a problem) to TM and back again to LatLong to ensure that
     * the reconvert back to geodetic returns the initial value.
     * 
     */
    @Test
    public void testTransverseMercatorToLatLong ()
    {
        /*
        System.out.println("TransverseMercator.transverseMercatorToLatLong()");
        //iterate over the test cases
        
        for (CoordinateTestCase t : testCases)
        {
            LatLong latLong = t.getLatLong();
            if (latLong != null)
            {
                System.out.print("Testing "+latLong+": ");

                TransverseMercator tm = TransverseMercator.latLongToTransverseMercator(latLong);

                LatLong result = TransverseMercator.transverseMercatorToLatLong(tm);

                System.out.print("   result:  "+result);
                //we want to ensure the decimal precision is 100% accurate after
                //the return conversion
                assertTrue("Resulting value not valid", latLong.equals(result));
                System.out.println("successful");
            }
        }

         * 
         */
         
    }
}