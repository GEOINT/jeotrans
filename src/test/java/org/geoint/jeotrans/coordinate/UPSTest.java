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

import org.geoint.jeotrans.coordinate.UPS;
import java.util.ArrayList;
import javax.measure.unit.SI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
        System.out.println("UPS.copy");
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
        System.out.println("UPS.latLongToUps");
        for (CoordinateTestCase t : testCases)
        {
            UPS expected = t.getExpectedUPS();
            if (expected != null)
            {
                System.out.print("Testing "+t.getLatLong().toString()+" " +
                        "(expected: "+expected.toText()+")");
                UPS result =
                        UPS.latLongToUps(t.getLatLong());
                System.out.print("   result: "+result.toText());

                assertTrue("Resulting value not valid", expected.equals(result));
                System.out.println("  successful");
            }
        }
    }

    @Test
    public void testUpsToLatLong ()
    {
        System.out.println("UPS.upsToLatLong");
        for (CoordinateTestCase t : testCases)
        {
            UPS expected = t.getExpectedUPS();
            if (expected != null)
            {
                System.out.print("Testing "+t.getLatLong().toString()+" " +
                        "(expected: "+expected.toText()+")");
                UPS result =
                        UPS.latLongToUps(t.getLatLong());
                System.out.print("   result: "+result.toText());

                assertTrue("Resulting value not valid", expected.equals(result));
                System.out.println("  successful");
            }
        }
    }

}