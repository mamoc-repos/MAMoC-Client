package uk.ac.st_andrews.cs.mamoc_client.Communication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import uk.ac.st_andrews.cs.mamoc_client.DexDecompiler;
import uk.ac.st_andrews.cs.mamoc_client.ExceptionHandler;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.Offloadable;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;

public class CommunicationController {
    private final boolean AnnotatedClassesAreIndexed;
    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static CommunicationController instance;

    private TreeSet<MobileNode> mobileDevices = new TreeSet<>();
    private TreeSet<EdgeNode> edgeDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private boolean isConnectionListenerRunning = false;

    private ArrayList<Class> offloadableClasses = new ArrayList<>();

    private final int STACK_SIZE = 20 * 1024 * 1024;
    private ExceptionHandler exceptionHandler;

    private CommunicationController(Context context) {
        this.mContext = context;
        myPort = Utils.getPort(mContext);
        connListener = new ConnectionListener(mContext, myPort);

        AnnotatedClassesAreIndexed = checkAnnotatedIndexing();
        if (!AnnotatedClassesAreIndexed) {
            findOffloadableClasses();
        }
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

//        Log.d("externalstorage", ExternalStoragePath);

        String[] sourceClassPaths = className.split("\\.");

        StringBuilder sourceClass = new StringBuilder();

        for (String path : sourceClassPaths) {
            sourceClass.append(path).append("/");
//            Log.d("class", path);
        }

        sourceClass.deleteCharAt(sourceClass.length() - 1); // remove the extra / at the end
        sourceClass.append(".java");

//        Log.d("sourceClass", sourceClass.toString());

        try {
            String fullPath = ExternalStoragePath + "/" + "mamoc" + "/" + sourceClass.toString();
//            Log.d("fullpath", fullPath);

            File sourceFile = new File(fullPath);
//            Log.d("outputdir", outputDir.getAbsolutePath());
            //    File[] sourceFiles = outputDir.listFiles();
            //    for (File sourceFile: sourceFiles) {
            Log.d("SourceFile", sourceFile.getAbsolutePath());
            //    if (sourceFile.getName().equals()){
            return Utils.readFile(mContext, sourceFile.getAbsolutePath());
            //    }
//            }
        } catch (Throwable x) {
            Log.e("error", "could not fetch the output directory");
        }

        return null;
    }

    public static CommunicationController getInstance(Context context) {
        if (instance == null) {
            synchronized (CommunicationController.class) {
                if (instance == null) {
                    instance = new CommunicationController(context);
                }
            }
        }
        return instance;
    }

    public void stopConnectionListener() {
        if (!isConnectionListenerRunning) {
            return;
        }
        if (connListener != null) {
            connListener.tearDown();
            connListener = null;
        }
        isConnectionListenerRunning = false;
    }

    public void startConnectionListener() {
        if (isConnectionListenerRunning) {
            return;
        }
        if (connListener == null) {
            connListener = new ConnectionListener(mContext, myPort);
        }
        if (!connListener.isAlive()) {
            connListener.interrupt();
            connListener.tearDown();
            connListener = null;
        }
        connListener = new ConnectionListener(mContext, myPort);
        connListener.start();
        isConnectionListenerRunning = true;
    }

    public void startConnectionListener(int port) {
        myPort = port;
        startConnectionListener();
    }

    public void restartConnectionListenerWith(int port) {
        stopConnectionListener();
        startConnectionListener(port);
    }

    public int getMyPort() {
        return myPort;
    }

    public boolean isConnectionListenerRunning() {
        return isConnectionListenerRunning;
    }

    public TreeSet<MobileNode> getMobileDevices() {
        return mobileDevices;
    }

    public void setMobileDevices(TreeSet<MobileNode> mobileDevices) {
        this.mobileDevices = mobileDevices;
    }

    public TreeSet<EdgeNode> getEdgeDevices() {
        return edgeDevices;
    }

    public void addEdgeDevice(EdgeNode edge) {
        this.edgeDevices.add(edge);
    }

    public void removeEdgeDevice(EdgeNode edge) {
        this.edgeDevices.remove(edge);
    }

    public TreeSet<CloudNode> getCloudDevices() {
        return cloudDevices;
    }

    public void setCloudDevices(TreeSet<CloudNode> cloudDevices) {
        this.cloudDevices = cloudDevices;
    }

    public ArrayList<Class> getOffloadableClasses() {
        return offloadableClasses;
    }

    public void runLocally() {

    }
}
