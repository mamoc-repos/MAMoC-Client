package uk.ac.st_andrews.cs.mamoc_client.profilers;

import android.util.Log;

public class Profiler {
    public static final String TAG = "profiler";

    private ExecutionLocation mLocation;

//    private ProgramProfiler progProfiler;
    private NetworkProfiler netProfiler;
    private DeviceProfiler devProfiler;

//    public Profiler(ProgramProfiler progProfiler, NetworkProfiler netProfiler, DeviceProfiler devProfiler) {
//        this.progProfiler = progProfiler;
//        this.netProfiler = netProfiler;
//        this.devProfiler = devProfiler;
//    }
//
//    public void startExecutionInfoTracking() {
//
//        if (netProfiler != null) {
//            netProfiler.startTransmittedDataCounting();
//            mLocation = ExecutionLocation.REMOTE;
//        } else {
//            mLocation = ExecutionLocation.LOCAL;
//        }
//        Log.d(TAG, mLocation + " " + progProfiler.methodName);
//        progProfiler.startExecutionInfoTracking();
//
//        if (mRegime == REGIME_CLIENT) {
//            devProfiler.startDeviceProfiling();
//        }
//    }
}
