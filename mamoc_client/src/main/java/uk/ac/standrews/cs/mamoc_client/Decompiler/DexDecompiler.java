package uk.ac.standrews.cs.mamoc_client.Decompiler;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jadx.api.JadxDecompiler;
import uk.ac.standrews.cs.mamoc_client.Execution.ExceptionHandler;

public class DexDecompiler {

    private static final Logger LOG = LoggerFactory.getLogger(DexDecompiler.class);

    private final int STACK_SIZE = 20 * 1024 * 1024;
    private ExceptionHandler exceptionHandler;

    private Context context;
    private ArrayList<String> classNames;

    public DexDecompiler(Context context, ArrayList<String> classNames) {
        this.context = context;
        this.classNames = classNames;
    }

    public void runDecompiler(){
        extractDexFiles();
    }

    private void extractDexFiles() {
        File sourceDir = getAPKSourceDir(context);
        File outputDir = getOutputDir(context);

        /* extract classes.dex or any other dex files from APK */
        extractDexFilesFromAPK(sourceDir, outputDir);

        ArrayList<File> dexFiles = new ArrayList<>();
        File[] apkFiles = new File[0];

        if (outputDir.exists()) {
            apkFiles = outputDir.listFiles();
        } else{
            LOG.error("No outputDir found");
        }

        for (File file : apkFiles) {
            if (file.getName().endsWith("dex")) {
//                Log.d("DexFile", "Found Dex File:" + file.getName());
                dexFiles.add(file);
            }
        }

        if (!dexFiles.isEmpty()) {
            for (File dexFile : dexFiles) {
                decompileDex(dexFile);
            }
        } else {
            Log.d("DexFile", "No Dex File Found!");
        }
    }

    private void decompileDex(File dexInputFile) {
        ThreadGroup group = new ThreadGroup("Dex to Java Group");
        Thread javaExtractionThread = new Thread(group, (Runnable) () -> {
            boolean javaError = false;
            try {
                JadxDecompiler jadx = new JadxDecompiler();
                jadx.setOutputDir(getOutputDir(context));
                jadx.loadFile(dexInputFile);
                jadx.saveAnnotatedClassSources(classNames);
                // We don't need to decompile and save all the dexes
//                jadx.saveSources();
            } catch (Exception | StackOverflowError e) {
                Log.e("error", e.getLocalizedMessage());
                javaError = true;
            }

            if (dexInputFile.exists() && dexInputFile.isFile()) {
                boolean dexDeleted = dexInputFile.delete();
                if (dexDeleted) {
                    LOG.info(dexInputFile.getName() + " successfully deleted");
                } else{
                    LOG.info("could not delete: " + dexInputFile.getName());
                }
            }
        }, "Dex to Java Thread", STACK_SIZE);

        javaExtractionThread.setPriority(Thread.MAX_PRIORITY);
        javaExtractionThread.setUncaughtExceptionHandler(exceptionHandler);
        javaExtractionThread.start();
    }

    /**
     * Get the apk path of this application.
     *
     * @param context any context (e.g. an Activity or a Service)
     * @return full apk file path, or null if an exception happened (it should not happen)
     */
    private File getAPKSourceDir(Context context) {
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            String apk = ai.sourceDir;
            return new File(apk);
        } catch (Throwable x) {
            LOG.error("cannot get APK"); // this should not happen
        }
        return null;
    }

    private File getOutputDir(Context context){

//        String packageName = context.getPackageName();
        String ExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        String folder_main ="mamoc";

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }

        Log.d("externalstorage", ExternalStoragePath);

        try{
            return new File(ExternalStoragePath  + "/" + folder_main);
        } catch (Throwable x) {
            LOG.error("could not create an output directory");
        }

        return null;
    }

    private void extractDexFilesFromAPK(File zipFile, File extractFolder) {
        try {
            int BUFFER = 2048;

            ZipFile zip = new ZipFile(zipFile);

            extractFolder.mkdir();

            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(extractFolder, currentEntry);

                if (!entry.isDirectory() && destFile.getName().endsWith("dex")) {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }

                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            LOG.error("APK Unzipping Error: ", e.getLocalizedMessage());
        }
    }
}
