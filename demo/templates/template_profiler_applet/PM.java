package jcprofiler; // TODO: change to your applet package 

import javacard.framework.ISOException;

/**
 * Utility class for performance profiling. Contains currently set trap stop and trap reaction method. 
 * @author Petr Svenda
 */
public class PM {
    public static short m_perfStop = -1; // Performace measurement stop indicator

    // if m_perfStop equals to stopCondition, exception is throws (trap hit)
    public static void check(short stopCondition) { 
        if (PM.m_perfStop == stopCondition) {
            ISOException.throwIt(stopCondition);
        }
    }

}

!!! TODO: Move code below into your main applet class into process() method. 
If INS value 0xf5 is already taken, change it to any other, but don't forget 
to modify correspondingly also the value of JCProfiler_client.PerfTests.INS_PERF_SETTRAPID 
// ----- begin of code to be moved 
public final static byte INS_PERF_SETSTOP           = (byte) 0xf5;
case INS_PERF_SETSTOP:
    PM.m_perfStop = Util.makeShort(apdubuf[ISO7816.OFFSET_CDATA], apdubuf[(short) (ISO7816.OFFSET_CDATA + 1)]);
    break;
// ----- end of code to be moved 
