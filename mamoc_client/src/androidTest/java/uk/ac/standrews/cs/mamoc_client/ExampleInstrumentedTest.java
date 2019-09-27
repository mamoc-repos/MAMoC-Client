package uk.ac.standrews.cs.mamoc_client;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.ac.standrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
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
    private MobileNode mn;
    private EdgeNode en;
    private CloudNode cn;

    @Before
    public void init_framework_and_nodes() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        mn = new MobileNode(appContext);
        en = new EdgeNode("192.168.0.12", 1);
        cn = new CloudNode("136.54.65.23", 1);
    }

    // MAMoC Framework Tests
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

        en.setIp("192.168.0.12");

        framework.commController.addEdgeDevice(en);
        TaskExecution task = new TaskExecution();
        task.setTaskName("task2");
        ExecutionLocation execLoc = framework.decisionEngine.makeDecision(task.getTaskName(), false);

        assertEquals(execLoc.getValue(), "EDGE");
    }

    // CommunicationController Tests
    @Test
    public void getCommControllerInstance() {
        CommunicationController commController = framework.commController;
        CommunicationController commController2 = framework.commController;

        assertEquals(commController, commController2);
    }

    @Test
    public void stopConnectionListener() {
        framework.commController.stopConnectionListener();
        assertEquals(framework.commController.isConnectionListenerRunning(), false);
    }

    @Test
    public void startConnectionListener() {
        framework.commController.startConnectionListener();
        assertEquals(framework.commController.isConnectionListenerRunning(), true);
    }

    @Test
    public void getMyPort() {
        assertTrue(framework.commController.getMyPort() > 0);
    }

    @Test
    public void addMobileDevices() {
        mn.setDeviceID("mn1");
        mn.setIp("136.64.53.132");
        framework.commController.addMobileDevice(mn);

        assertFalse(framework.commController.getMobileDevices().isEmpty());
    }

    @Test
    public void removeMobileDevice() {
        framework.commController.removeMobileDevice(mn);

        assertTrue(framework.commController.getMobileDevices().isEmpty());
    }

    @Test
    public void addEdgeDevice() {
        framework.commController.addEdgeDevice(en);

        assertFalse(framework.commController.getEdgeDevices().isEmpty());
    }

    @Test
    public void removeEdgeDevice() {
        framework.commController.removeEdgeDevice(en);

        assertTrue(framework.commController.getEdgeDevices().isEmpty());
    }

    @Test
    public void addCloudDevices() {
        framework.commController.addCloudDevices(cn);

        assertFalse(framework.commController.getCloudDevices().isEmpty());
    }

    @Test
    public void removeCloudDevice() {
        framework.commController.removeCloudDevice(cn);

        assertTrue(framework.commController.getCloudDevices().isEmpty());
    }

    // DexDecompiler Tests
    @Test
    public void dex_decompiling(){

    }

    @Test
    public void class_indexing(){

    }

    // Profiler Tests
    @Test
    public void device_profiling_cpu(){

    }

    @Test
    public void network_profiling_netType(){

    }

    // AHP and TOPSIS Tests
    @Test
    public void ahp_init(){

    }


}
