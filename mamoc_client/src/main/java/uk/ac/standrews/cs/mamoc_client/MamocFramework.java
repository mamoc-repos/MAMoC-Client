package uk.ac.standrews.cs.mamoc_client;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.standrews.cs.mamoc_client.Annotation.Offloadable;
import uk.ac.standrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.standrews.cs.mamoc_client.DB.DBAdapter;
import uk.ac.standrews.cs.mamoc_client.Decompiler.DexDecompiler;
import uk.ac.standrews.cs.mamoc_client.DecisionMaker.DecisionEngine;
import uk.ac.standrews.cs.mamoc_client.Execution.ExceptionHandler;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionController;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Profilers.DeviceProfiler;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Profilers.NetworkProfiler;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

public class MamocFramework {

    private final String TAG = "MamocFramework";
    private Context mContext;

    public CommunicationController commController;
    private ExecutionController execController;
    public DecisionEngine decisionEngine;
    public DeviceProfiler deviceProfiler;
    public NetworkProfiler networkProfiler;
    public DBAdapter dbAdapter;

//    private ArrayList<Class> offloadableClasses = new ArrayList<>();

    private MobileNode selfNode;

    private static MamocFramework instance;
    private ExceptionHandler exceptionHandler;
    public String lastExecution;

    private MamocFramework(Context context) {
        this.mContext = context;
    }

    public void start() {
        this.commController = CommunicationController.getInstance(mContext);
        this.execController = ExecutionController.getInstance(mContext);
        this.decisionEngine = DecisionEngine.getInstance(mContext);

        this.deviceProfiler = new DeviceProfiler(mContext);
        this.networkProfiler = new NetworkProfiler(mContext);

        this.dbAdapter = DBAdapter.getInstance(mContext);

        // We need to perform class indexing and decompiling after a fresh install of the app
        if (isFirstInstall(mContext)) {
//            findOffloadableTasks();
            decompileAnnotatedClassFiles();
        }

        if (selfNode == null) {
            createSelfNode(mContext);
        }

        if (lastExecution == null){
            lastExecution = "Local";
        }
    }

    private void createSelfNode(Context context) {
        selfNode = new MobileNode(context);
        selfNode.setNodeName("SelfNode");
        selfNode.setBatteryLevel(deviceProfiler.getBatteryLevel());
        selfNode.setBatteryState(deviceProfiler.isDeviceCharging());
        selfNode.setCpuFreq((int)deviceProfiler.getTotalCpuFreq(context));
        selfNode.setMemoryMB(deviceProfiler.getTotalMemory());
    }

    public static MamocFramework getInstance(Context context) {
        if (instance == null) {
            synchronized (MamocFramework.class) {
                if (instance == null) {
                    instance = new MamocFramework(context);
                }
            }
        }
        return instance;
    }

//    public ArrayList<Class> getOffloadableClasses() {
//        return offloadableClasses;
//    }

//    private boolean checkAnnotatedIndexing() {
//        String mamocDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mamoc";
//        File mamocDir = new File(mamocDirPath);
//        Log.d(TAG, mamocDir.exists()? "index file exists": "index file does not exist");
//        return mamocDir.exists();
//    }

    private static boolean isFirstInstall(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0).lastUpdateTime;
            return firstInstallTime == lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

//    private void findOffloadableTasks() {
//
//        ThreadGroup group = new ThreadGroup("Offloadable tasks group");
//
//        // increase stack size from 8k (around 260 calls) to 2M (enough for not getting the StackOverFlowException)
//        int STACK_SIZE = 20 * 1024 * 1024;
//
//        Thread offloadableTasksThread = new Thread(group, () -> {
//            Iterable<Class<?>> klasses = ClassIndex.getAnnotated(Offloadable.class);
//
//            for (Class<?> klass : klasses) {
//                offloadableClasses.add(klass);
//                Log.d("annotation", "new annotated class found: " + klass.getName());
//            }
//
//        }, "Offloadable Tasks Thread", STACK_SIZE);
//
//        offloadableTasksThread.setPriority(Thread.MAX_PRIORITY);
//        offloadableTasksThread.setUncaughtExceptionHandler(exceptionHandler);
//        offloadableTasksThread.start();
//    }

    private void decompileAnnotatedClassFiles() {

        ArrayList<String> classNames = new ArrayList<>();

        for (String s : ClassIndex.getAnnotatedNames(Offloadable.class)) {
            classNames.add(s);
        }

//        for (Class klass: offloadableClasses){
//            classNames.add(klass.getName());
//        }

        DexDecompiler decompiler = new DexDecompiler(mContext, classNames);
        decompiler.start();
    }

    public String fetchSourceCode(String className) {
        String ExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        String[] sourceClassPaths = className.split("\\.");

        StringBuilder sourceClass = new StringBuilder();

        for (String path : sourceClassPaths) {
            sourceClass.append(path).append("/");
        }

        sourceClass.deleteCharAt(sourceClass.length() - 1); // remove the extra / at the end
        sourceClass.append(".java");

        try {
            String fullPath = ExternalStoragePath + "/" + "mamoc" + "/" + sourceClass.toString();
            File sourceFile = new File(fullPath);
            Log.d("SourceFile", sourceFile.getAbsolutePath());
            return Utils.readFile(mContext, sourceFile.getAbsolutePath());

        } catch (Throwable x) {
            Log.e("error", "could not fetch the output directory");
        }

        return null;
    }

    public void execute(ExecutionLocation location, String task_name, String resource_name, Object... params) {
        if (location == ExecutionLocation.DYNAMIC){
            execController.runDynamically(mContext, task_name, resource_name, params);
        } else if (location == ExecutionLocation.LOCAL) {
            execController.runLocally(task_name, resource_name, params);
        } else {
            execController.runRemotely(mContext, location, task_name, resource_name, params);
        }
    }

    public MobileNode getSelfNode(){
        return selfNode;
    }
}
