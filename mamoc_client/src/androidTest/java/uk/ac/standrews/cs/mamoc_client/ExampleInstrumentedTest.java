package uk.ac.standrews.cs.mamoc_client;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.TaskExecution;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private MamocFramework framework;

    @Before
    public void init_framework() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
    }

    @Test
    public void framework_only_init_once() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        MamocFramework framework2 = MamocFramework.getInstance(appContext);

        assertEquals(framework, framework2);
    }

    @Test
    public void start_framework_and_test_profilers(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        framework.start();

        assertEquals(framework.deviceProfiler, framework.deviceProfiler);
        assertEquals(framework.networkProfiler, framework.networkProfiler);
    }

    @Test
    public void decision_engine_local_execution(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        framework.start();

        TaskExecution task = new TaskExecution();
        task.setTaskName("task1");
        ExecutionLocation execLoc = framework.decisionEngine.makeDecision(task.getTaskName(), false);

        assertEquals(execLoc.getValue(), "LOCAL");
    }

    @Test
    public void decision_engine_remote_execution(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        framework.start();

        EdgeNode edgeNode = new EdgeNode("192.168.0.12", 1);
        edgeNode.setIp("192.168.0.12");

        framework.commController.addEdgeDevice(edgeNode);
        TaskExecution task = new TaskExecution();
        task.setTaskName("task2");
        ExecutionLocation execLoc = framework.decisionEngine.makeDecision(task.getTaskName(), false);

        assertEquals(execLoc.getValue(), "EDGE");
    }

    @Test
    public void dex_decompiling(){

    }

    @Test
    public void class_indexing(){

    }

    @Test
    public void device_profiling_cpu(){

    }

    @Test
    public void network_profiling_netType(){

    }

    @Test
    public void ahp_init(){

    }
}
