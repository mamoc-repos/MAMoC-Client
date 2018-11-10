package uk.ac.standrews.cs.mamoc_client;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.util.ArrayList;

import uk.ac.standrews.cs.mamoc_client.Annotation.Offloadable;
import uk.ac.standrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.standrews.cs.mamoc_client.Decompiler.DexDecompiler;
import uk.ac.standrews.cs.mamoc_client.Execution.DecisionEngine;
import uk.ac.standrews.cs.mamoc_client.Execution.ExceptionHandler;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionController;
import uk.ac.standrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Utils.Utils;

public class MamocFramework {

    private final String TAG = "MamocFramework";
    private Context mContext;

    public CommunicationController commController;
    public ExecutionController execController;
    public DecisionEngine decisionEngine;

    private ArrayList<Class> offloadableClasses = new ArrayList<>();

    private static MamocFramework instance;

    private final int STACK_SIZE = 20 * 1024 * 1024;
    private ExceptionHandler exceptionHandler;

    private MamocFramework(Context context) {
        this.mContext = context;
    }

    public void start() {
        commController = CommunicationController.getInstance(mContext);
        execController = ExecutionController.getInstance(mContext);
        decisionEngine = DecisionEngine.getInstance(mContext);

        // TODO: find a better mechanism for reindexing the changed annotated classes
        if (!checkAnnotatedIndexing()) {
            findOffloadableClasses();
        }
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

    public ArrayList<Class> getOffloadableClasses() {
        return offloadableClasses;
    }

    private boolean checkAnnotatedIndexing() {
        String mamocDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mamoc";
        File mamocDir = new File(mamocDirPath);
        return mamocDir.exists();
    }

    private void findOffloadableClasses() {

        ThreadGroup group = new ThreadGroup("Dex to Java Group");

        Thread annotationIndexingThread = new Thread(group, (Runnable) () -> {

            Iterable<Class<?>> klasses = ClassIndex.getAnnotated(Offloadable.class);
            ArrayList<String> annotatedClasses = new ArrayList<>();

            for (Class<?> klass : klasses) {
                offloadableClasses.add(klass);
                annotatedClasses.add(klass.getName());
                Log.d("annotation", "new annotated class found: " + klass.getName());
            }

            decompileAnnotatedClassFiles(annotatedClasses);

        }, "Annotation Indexing Thread", STACK_SIZE);

        annotationIndexingThread.setPriority(Thread.MAX_PRIORITY);
        annotationIndexingThread.setUncaughtExceptionHandler(exceptionHandler);
        annotationIndexingThread.start();
    }

    private void decompileAnnotatedClassFiles(ArrayList<String> offloadableClasses) {
        DexDecompiler decompiler = new DexDecompiler(mContext, offloadableClasses);
        decompiler.runDecompiler();
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

    public void execute(ExecutionLocation location, String rpc_name, String resource_name, Object... params) {
        execController.runRemote(mContext, location, rpc_name, resource_name, params);
    }
}
