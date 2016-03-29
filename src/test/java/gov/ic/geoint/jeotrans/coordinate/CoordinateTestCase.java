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

import gov.ic.geoint.jeotrans.coordinate.UPS;
import gov.ic.geoint.jeotrans.coordinate.MGRS;
import gov.ic.geoint.jeotrans.coordinate.UTM;
import gov.ic.geoint.jeotrans.coordinate.TransverseMercator;
import gov.ic.geoint.jeotrans.coordinate.PolarStereographic;
import java.util.ArrayList;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.LatLong;

/**
 * CoordinateTestCase instances act as a container (of sorts) to group
 * test cases with anticipated results for coordinate conversion.
 *
 * This class provides a set of default test cases that's available through
 * the static method getDefaultTestCases().
 *
 * @author Steve Siebert
 */
public class CoordinateTestCase {

    /**
     * The coordinate to test against
     */
    private LatLong latLong;

    /*****************  EXPECTED OUTCOMES  ************************/

    private MGRS expectedMGRS;
    private UTM expectedUTM;
    private UPS expectedUPS;
    private TransverseMercator expectedTM;
    private PolarStereographic expectedPS;
    
    /**
     * Default world-wide test cases
     */
    private static ArrayList<CoordinateTestCase> defaultTestCases;

    public CoordinateTestCase (LatLong latLong)
    {
        this.latLong = latLong;
    }

    public MGRS getExpectedMGRS() {
        return expectedMGRS;
    }

    public void setExpectedMGRS(MGRS expectedMGRS) {
        this.expectedMGRS = expectedMGRS;
    }

    public UPS getExpectedUPS() {
        return expectedUPS;
    }

    public void setExpectedUPS(UPS expectedUPS) {
        this.expectedUPS = expectedUPS;
    }

    public UTM getExpectedUTM() {
        return expectedUTM;
    }

    public void setExpectedUTM(UTM expectedUTM) {
        this.expectedUTM = expectedUTM;
    }

    public LatLong getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLong latLong) {
        this.latLong = latLong;
    }

    public PolarStereographic getExpectedPS() {
        return expectedPS;
    }

    public void setExpectedPS(PolarStereographic expectedPS) {
        this.expectedPS = expectedPS;
    }

    public TransverseMercator getExpectedTM() {
        return expectedTM;
    }

    public void setExpectedTM(TransverseMercator expectedTM) {
        this.expectedTM = expectedTM;
    }

    /**
     * Generate (and cache) default test cases for the world
     *
     * @return
     */
    public static ArrayList<CoordinateTestCase> getDefaultTestCases ()
    {
        if (defaultTestCases != null)
        {
            return defaultTestCases;
        }

        defaultTestCases = new ArrayList<CoordinateTestCase> ();

        /*
         * polar south, A-region
         *
         * Geodetic: -82.123456789, -20.123456789
         * UPS 1698683.394 2822342.984
         * MGRS AUW9868322343
         * TM -301929.427 -9175347.727
         * PS LATITUDE OUT OF RANGE
         */
        CoordinateTestCase psA = new CoordinateTestCase(
                LatLong.valueOf(-82.123456789, -20.123456789, NonSI.DEGREE_ANGLE));
        psA.setExpectedMGRS(MGRS.valueOf(0, 'A', "UW".toCharArray(), 98683, 22343, SI.METER));
        psA.setExpectedUPS(UPS.valueOf(1698683.394, 2822342.984, UPS.HEMISPHERE_SOUTH, SI.METER));
        psA.setExpectedTM(TransverseMercator.valueOf(-301929.427, -9175347.727, SI.METER));
        defaultTestCases.add(psA);

        /*
         * polar south, B-region
         *
         * Geodetic -82.123456789, 20.123456789
         * UPS 2301316.606 2822342.984
         * MGRS BFW0131722343
         * TM 301929.427 -9175347.727
         * PS LATITUDE OUT OF RANGE EXCEPTION
         */
        CoordinateTestCase psB = new CoordinateTestCase(
                LatLong.valueOf(-82.123456789, 20.123456789, NonSI.DEGREE_ANGLE));
        psB.setExpectedMGRS(MGRS.valueOf(0, 'B', "FW".toCharArray(), 1317, 22343, SI.METER));
        psB.setExpectedUPS(UPS.valueOf(2301316.606, 2822342.984, UPS.HEMISPHERE_SOUTH, SI.METER));
        psB.setExpectedTM(TransverseMercator.valueOf(301929.427, -9175347.727, SI.METER));
        defaultTestCases.add(psB);

        /*
         * polar north, Y-region
         *
         * Geodetic 87.123456789, -20.123456789
         * UPS 1890102.261 1700070.847
         * MGRS YYE9010200071
         * TM -110502.432 9700259.533
         * PS -110561.104 -301739.586
         */
        CoordinateTestCase pnY = new CoordinateTestCase(
                LatLong.valueOf(87.123456789, -20.123456789, NonSI.DEGREE_ANGLE));
        pnY.setExpectedMGRS(MGRS.valueOf(0, 'Y', "YE".toCharArray(), 90102, 71, SI.METER));
        pnY.setExpectedUPS(UPS.valueOf(1890102.261, 1700070.847, UPS.HEMISPHERE_NORTH, SI.METER));
        pnY.setExpectedTM(TransverseMercator.valueOf(-110502.432, 9700259.533, SI.METER));
        pnY.setExpectedPS(PolarStereographic.valueOf(-110561.104, -301739.586, SI.METER));
        defaultTestCases.add(pnY);

        /*
         * polar north, Z-region
         *
         * Geodetic 87.123456789, 20.123456789
         * UPS 2109897.739 1700070.847
         * MGRS ZBE0989800071
         * TM 110502.432 9700259.533
         * PS 110561.104 -301739.586
         */
        CoordinateTestCase pnZ = new CoordinateTestCase(
                LatLong.valueOf(87.123456789, 20.123456789, NonSI.DEGREE_ANGLE));
        pnZ.setExpectedMGRS(MGRS.valueOf(0, 'Z', "BE".toCharArray(), 9898, 71, SI.METER));
        pnZ.setExpectedUPS(UPS.valueOf(2109897.739, 1700070.847, UPS.HEMISPHERE_NORTH, SI.METER));
        pnZ.setExpectedTM(TransverseMercator.valueOf(110502.432, 9700259.533, SI.METER));
        pnZ.setExpectedPS(PolarStereographic.valueOf(110561.104, -301739.586, SI.METER));
        defaultTestCases.add(pnZ);
        
        /*
         * northern-eastern hemisphere (Nanjing, China)
         *
         * Geodetic 32.0500000, 118.7666667
         * UTM 666792.126 3547342.666
         * MGRS 50SPA6679247343
         * TM LONGITUDE OUT OF RANGE EXCEPTION
         * PS 6193158.515 3400027.231
         */
        CoordinateTestCase ne = new CoordinateTestCase(
                LatLong.valueOf(32.0500000, 118.7666667, NonSI.DEGREE_ANGLE));
        ne.setExpectedMGRS(MGRS.valueOf(50, 'S', "PA".toCharArray(), 66792, 47343, SI.METER));
        ne.setExpectedUTM(UTM.valueOf(50, 'S', 666792.126, 3547342.666, SI.METER));
        ne.setExpectedPS(PolarStereographic.valueOf(6193158.515, 3400027.231, SI.METER));
        defaultTestCases.add(ne);

        /*
         * north-western hemisphere (Augusta, GA)
         *
         * Geodetic 33.4700000, -081.9750000
         * UTM 409403.806 3703818.299
         * MGRS 17SMT0940403818
         * TM -7795745.182 9027269.580
         * PS -6793667.477 -957810.730
         */
        CoordinateTestCase nw = new CoordinateTestCase(
                LatLong.valueOf(33.4700000, -081.9750000, NonSI.DEGREE_ANGLE));
        nw.setExpectedMGRS(MGRS.valueOf(17, 'S', "MT".toCharArray(), 9404, 3818, SI.METER));
        nw.setExpectedUTM(UTM.valueOf(17, 'S', 409403.806, 3703818.299, SI.METER));
        nw.setExpectedTM(TransverseMercator.valueOf(-7795745.182, 9027269.580, SI.METER));
        nw.setExpectedPS(PolarStereographic.valueOf(-6793667.477, -957810.730, SI.METER));
        defaultTestCases.add(nw);

        /*
         * south-eastern hemispehere (Mt. Taranaki, New Zealand)
         *
         * Geodetic -39.295357, 174.063148
         * UTM 246731.330 5646333.644
         * MGRS 60HTB4673146334
         * TM LONGITUDE OUT OF RANGE
         * PS LATITUDE OUT OF RANGE
         */
        CoordinateTestCase se = new CoordinateTestCase(
                LatLong.valueOf(-39.295357, 174.063148, NonSI.DEGREE_ANGLE));
        se.setExpectedMGRS(MGRS.valueOf(60, 'H', "TB".toCharArray(), 46731, 46334, SI.METER));
        se.setExpectedUTM(UTM.valueOf(60, 'H', 246731.330, 5646333.644, SI.METER));
        defaultTestCases.add(se);

        /*
         * south-western hemisphere (San José, Costa Rica)
         *
         * Geodetic 9.929827, -84.077797
         * UTM 820460.371 1099064.054
         * MGRS 16PHR2046099064
         * TM LONGITUDE OUT OF RANGE
         * PS LATITUDE OUT OF RANGE
         */
        CoordinateTestCase sw = new CoordinateTestCase(
                LatLong.valueOf(9.929827, -84.077797, NonSI.DEGREE_ANGLE));
        sw.setExpectedMGRS(MGRS.valueOf(16, 'P', "HR".toCharArray(), 20460, 99064, SI.METER));
        sw.setExpectedUTM(UTM.valueOf(16, 'P', 820460.371, 1099064.054, SI.METER));
        defaultTestCases.add(sw);

        /*
         * zone 31 mgrs areas
         *
         * Geodetic 57.123456789, 50.123456789
         * UTM 446929.160 6331469.638
         * MGRS 39VVD4692931470
         * TM 2834753.785 7490556.063
         * PS 2894912.055 -2418509.135
         *
         * Geodetic 54.123456789, 67.123456789
         * UTM 377364.546 5998885.176
         * MGRS 42UUE7736598885
         * TM 3845070.377 8248569.746
         * PS 3812689.985 -1608704.193
         */
        CoordinateTestCase z31_1 = new CoordinateTestCase(
                LatLong.valueOf(57.123456789, 50.123456789, NonSI.DEGREE_ANGLE));
        z31_1.setExpectedMGRS(MGRS.valueOf(39, 'V', "VD".toCharArray(), 46929, 31470, SI.METER));
        z31_1.setExpectedUTM(UTM.valueOf(39, 'V', 446929.160, 6331469.638, SI.METER));
        z31_1.setExpectedTM(TransverseMercator.valueOf(2834753.785, 7490556.063, SI.METER));
        z31_1.setExpectedPS(PolarStereographic.valueOf(2894912.055, -2418509.135, SI.METER));
        defaultTestCases.add(z31_1);

        CoordinateTestCase z31_2 = new CoordinateTestCase(
                LatLong.valueOf(54.123456789, 67.123456789, NonSI.DEGREE_ANGLE));
        z31_2.setExpectedMGRS(MGRS.valueOf(42, 'U', "UE".toCharArray(), 77365, 98885, SI.METER));
        z31_2.setExpectedUTM(UTM.valueOf(42, 'U', 377364.546, 5998885.176, SI.METER));
        z31_2.setExpectedTM(TransverseMercator.valueOf(3845070.377, 8248569.746, SI.METER));
        z31_2.setExpectedPS(PolarStereographic.valueOf(3812689.985, -1608704.193, SI.METER));
        defaultTestCases.add(z31_2);

        
        return defaultTestCases;
    }

}
