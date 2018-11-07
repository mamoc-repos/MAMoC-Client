package uk.ac.st_andrews.cs.mamoc_client.Execution;

import android.content.Context;

import uk.ac.st_andrews.cs.mamoc_client.Profilers.ExecutionLocation;

public class DecisionEngine {

    private static DecisionEngine instance;
    Context mContext;

    private DecisionEngine(Context context) {
        this.mContext = context;
    }

    public static DecisionEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (DecisionEngine.class) {
                if (instance == null) {
                    instance = new DecisionEngine(context);
                }
            }
        }
        return instance;
    }


    public ExecutionLocation makeDecision(String taskName, Boolean isParallel){


        // check if task has previously been offloaded

        // if exists
            // take average execution speed and energy consumption
            //

        // else
            // check if there is a more powerful surrogate
            // if yes: offload it to remote and save execution time on DB
                // Edge and remote: yes
                // D2D: compare it to me
            // else: execute locally

        return ExecutionLocation.LOCAL;
    }
}
