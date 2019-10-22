package uk.ac.standrews.cs.mamoc_client;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import uk.ac.standrews.cs.mamoc_client.DecisionMaker.NodeOffloadingPercentage;
import uk.ac.standrews.cs.mamoc_client.ServiceDiscovery.ServiceDiscovery;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.Task;

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
        en = new EdgeNode("192.168.0.12", 8080);
        cn = new CloudNode("136.54.65.23", 8080);
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

        Task task = new Task();
        task.setTaskName("task1");

        framework.deploymentController.runLocally(task, "None");
        assertSame(task.getExecLocation(), ExecutionLocation.LOCAL);
    }

    @Test
    public void decision_engine_remote_execution(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        framework = MamocFramework.getInstance(appContext);
        framework.start();

        en.setIp("192.168.0.12");

        framework.serviceDiscovery.addEdgeDevice(en);
        Task task = new Task();
        task.setTaskName("task2");
        ArrayList<NodeOffloadingPercentage> nodeOffPerc = framework.decisionEngine.makeDecision(task, false, 1,0);

        assertTrue(nodeOffPerc.get(0).getNode() instanceof EdgeNode);
    }

    // ServiceDiscovery Tests
    @Test
    public void getCommControllerInstance() {
        ServiceDiscovery commController = framework.serviceDiscovery;
        ServiceDiscovery commController2 = framework.serviceDiscovery;

        assertEquals(commController, commController2);
    }

    @Test
    public void stopConnectionListener() {
        framework.serviceDiscovery.stopConnectionListener();
        assertFalse(framework.serviceDiscovery.isConnectionListenerRunning());
    }

    @Test
    public void startConnectionListener() {
        framework.serviceDiscovery.startConnectionListener();
        assertTrue(framework.serviceDiscovery.isConnectionListenerRunning());
    }

    @Test
    public void getMyPort() {
        assertTrue(framework.serviceDiscovery.getMyPort() > 0);
    }

    @Test
    public void addMobileDevices() {
        mn.setDeviceID("mn1");
        mn.setIp("136.64.53.132");
        framework.serviceDiscovery.addMobileDevice(mn);

        assertFalse(framework.serviceDiscovery.listMobileNodes().isEmpty());
    }

//    @Test
//    public void removeMobileDevice() {
//        if (framework.serviceDiscovery.listMobileNodes().size() > 0) {
//            framework.serviceDiscovery.removeMobileDevice(mn);
//        }
//
//        assertTrue(framework.serviceDiscovery.listMobileNodes().isEmpty());
//    }

    @Test
    public void addEdgeDevice() {
        framework.serviceDiscovery.addEdgeDevice(en);

        assertFalse(framework.serviceDiscovery.listEdgeNodes().isEmpty());
    }

//    @Test
//    public void removeEdgeDevice() {
//        if (framework.serviceDiscovery.listEdgeNodes().size() > 0) {
//            framework.serviceDiscovery.removeEdgeDevice(en);
//        }
//
//        assertTrue(framework.serviceDiscovery.listEdgeNodes().isEmpty());
//    }

    @Test
    public void addCloudDevice() {
        framework.serviceDiscovery.addCloudDevices(cn);

        assertFalse(framework.serviceDiscovery.listPublicNodes().isEmpty());
    }

//    @Test
//    public void removeCloudDevice() {
//        if (framework.serviceDiscovery.listPublicNodes().size() > 0) {
//            framework.serviceDiscovery.removeCloudDevice(cn);
//        }
//
//        assertTrue(framework.serviceDiscovery.listPublicNodes().isEmpty());
//    }

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
