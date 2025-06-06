package com.ctrip.framework.drc.manager.ha.cluster.impl;

import com.ctrip.framework.drc.core.entity.*;
import com.ctrip.framework.drc.core.http.ApiResult;
import com.ctrip.framework.drc.core.server.config.RegistryKey;
import com.ctrip.framework.drc.core.server.config.applier.dto.ApplierInfoDto;
import com.ctrip.framework.drc.core.server.config.applier.dto.ApplyMode;
import com.ctrip.framework.drc.core.server.config.replicator.dto.ReplicatorInfoDto;
import com.ctrip.framework.drc.core.server.utils.ThreadUtils;
import com.ctrip.framework.drc.manager.ha.config.ClusterManagerConfig;
import com.ctrip.framework.drc.manager.ha.meta.CurrentMetaManager;
import com.ctrip.framework.drc.manager.ha.meta.RegionCache;
import com.ctrip.framework.drc.manager.healthcheck.inquirer.ApplierInfoInquirer;
import com.ctrip.framework.drc.manager.healthcheck.inquirer.ReplicatorInfoInquirer;
import com.ctrip.framework.drc.manager.zookeeper.AbstractDbClusterTest;
import com.ctrip.xpipe.tuple.Pair;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @Author limingdong
 * @create 2020/5/19
 */
public class DefaultInstanceStateControllerTest extends AbstractDbClusterTest {

    @InjectMocks
    private DefaultInstanceStateController instanceStateController = new DefaultInstanceStateController();

    private ExecutorService executorService = ThreadUtils.newSingleThreadExecutor("UT_DefaultInstanceStateControllerTest");

    @Mock
    private RegionCache regionMetaCache;

    @Mock
    private CurrentMetaManager currentMetaManager;

    @Mock
    private ClusterManagerConfig config;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        instanceStateController.initialize();
        instanceStateController.start();

        when(config.getMigrationBlackIps()).thenReturn(StringUtils.EMPTY);
    }

    @After
    public void tearDown() {
        super.tearDown();
        try {
            instanceStateController.stop();
            instanceStateController.dispose();
        } catch (Exception e) {
        }
    }

    @Test
    public void registerReplicator() throws InterruptedException {
        List<Replicator> replicatorList = Lists.newArrayList();
        newReplicator.setIp(LOCAL_IP);
        newReplicator.setPort(backupPort);
        replicatorList.add(newReplicator);
        when(currentMetaManager.getSurviveReplicators(CLUSTER_ID)).thenReturn(replicatorList);

        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.registerReplicator(CLUSTER_ID, newReplicator);
        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertEquals(body.getAppliers(), dbCluster.getAppliers());
        List<Replicator> replicators = body.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0), newReplicator);
        Thread.sleep(100);
    }

    @Test
    public void addReplicator() throws InterruptedException {
        List<Replicator> replicatorList = Lists.newArrayList();
        newReplicator.setIp(LOCAL_IP);
        newReplicator.setPort(backupPort);
        replicatorList.add(newReplicator);
        when(currentMetaManager.getSurviveReplicators(CLUSTER_ID)).thenReturn(replicatorList);

        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.addReplicator(CLUSTER_ID, newReplicator);
        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertEquals(body.getAppliers(), dbCluster.getAppliers());
        List<Replicator> replicators = body.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0), newReplicator);
        Thread.sleep(100);
    }

    @Test
    public void registerApplierWithMasterNull() throws InterruptedException {
        Applier applier = new Applier();
        applier.setTargetMhaName("mockTargetMha_*&^%");
        applier.setTargetIdc("mockTargetIdc_*&^%");
        applier.setMaster(true);
        applier.setIp(LOCAL_IP);
        applier.setPort(backupPort);
        applier.setApplyMode(ApplyMode.transaction_table.getType());

        List<Replicator> replicatorList = Lists.newArrayList();
        newReplicator.setIp(LOCAL_IP);
        newReplicator.setPort(backupPort);
        replicatorList.add(newReplicator);
        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(null);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.registerApplier(CLUSTER_ID, applier);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Applier> appliers = body.getAppliers();
        Assert.assertEquals(appliers.size(), 1);
        Assert.assertEquals(appliers.get(0), applier);

        Thread.sleep(100);
    }

    @Test
    public void registerMessengerWithMasterNull() throws InterruptedException {
        Messenger messenger = new Messenger();
        messenger.setMaster(true);
        messenger.setIp(LOCAL_IP);
        messenger.setPort(backupPort);
        messenger.setApplyMode(ApplyMode.mq.getType());

        when(currentMetaManager.getActiveReplicator(anyString())).thenReturn(null);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.registerMessenger(CLUSTER_ID, messenger);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Messenger> messengers = body.getMessengers();
        Assert.assertEquals(messengers.size(), 1);
        Assert.assertEquals(messengers.get(0), messenger);

        Thread.sleep(100);
    }

    @Test
    public void registerApplierWithMasterNotNull() throws InterruptedException {
        Applier applier = new Applier();
        applier.setTargetMhaName("mockTargetMha_*&^%");
        applier.setTargetIdc("mockTargetIdc_*&^%");
        applier.setMaster(true);
        applier.setIp(LOCAL_IP);
        applier.setPort(backupPort);
        applier.setApplyMode(ApplyMode.transaction_table.getType());

        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(applierMaster);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.registerApplier(CLUSTER_ID, applier);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Replicator> replicators = body.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0).getIp(), newReplicator.getIp());
        Assert.assertEquals(replicators.get(0).getApplierPort(), newReplicator.getApplierPort());
        Assert.assertEquals(replicators.get(0).getMaster(), true);

        List<Applier> appliers = body.getAppliers();
        Assert.assertEquals(appliers.size(), 1);
        Assert.assertEquals(appliers.get(0), applier);

        Thread.sleep(100);
    }

    @Test
    public void registerMessengerWithMasterNotNull() throws InterruptedException {
        Messenger messenger = new Messenger();
        messenger.setMaster(true);
        messenger.setIp(LOCAL_IP);
        messenger.setPort(backupPort);
        messenger.setApplyMode(ApplyMode.mq.getType());

        when(currentMetaManager.getActiveReplicator(anyString())).thenReturn(dbCluster.getReplicators().get(0));
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.registerMessenger(CLUSTER_ID, messenger);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Messenger> messengers = body.getMessengers();
        Assert.assertEquals(messengers.size(), 1);
        Assert.assertEquals(messengers.get(0), messenger);

        Thread.sleep(100);
    }

    @Test
    public void addApplierWithMasterNull() throws InterruptedException {
        Applier applier = new Applier();
        applier.setTargetMhaName("mockTargetMha_*&^%");
        applier.setTargetIdc("mockTargetIdc_*&^%");
        applier.setMaster(true);
        applier.setIp(LOCAL_IP);
        applier.setPort(backupPort);
        applier.setApplyMode(ApplyMode.transaction_table.getType());

        List<Replicator> replicatorList = Lists.newArrayList();
        newReplicator.setIp(LOCAL_IP);
        newReplicator.setPort(backupPort);
        replicatorList.add(newReplicator);
        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(null);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.addApplier(CLUSTER_ID, applier);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Applier> appliers = body.getAppliers();
        Assert.assertEquals(appliers.size(), 1);
        Assert.assertEquals(appliers.get(0), applier);

        Thread.sleep(100);
    }

    @Test
    public void addMessengerWithMasterNull() throws InterruptedException {
        Messenger messenger = new Messenger();
        messenger.setMaster(true);
        messenger.setIp(LOCAL_IP);
        messenger.setPort(backupPort);
        messenger.setApplyMode(ApplyMode.mq.getType());

        when(currentMetaManager.getActiveReplicator(anyString())).thenReturn(null);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.addMessenger(CLUSTER_ID, messenger);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Messenger> messengers = body.getMessengers();
        Assert.assertEquals(messengers.size(), 1);
        Assert.assertEquals(messengers.get(0), messenger);

        Thread.sleep(100);
    }

    @Test
    public void addApplierWithMasterNotNull() throws InterruptedException {
        String cluster_multi = "integration-test.fxdrc_multi";
        Applier applier = new Applier();
        applier.setTargetMhaName("mockTargetMha");
        applier.setTargetName("integration-test");
        applier.setTargetIdc("mockTargetIdc_*&^%");
        applier.setMaster(true);
        applier.setIp(LOCAL_IP);
        applier.setPort(backupPort);
        applier.setApplyMode(ApplyMode.transaction_table.getType());

        Applier multiapplier = new Applier();
        multiapplier.setTargetMhaName("mockTargetMha_multi");
        multiapplier.setTargetIdc("mockTargetIdc_*&^%");
        multiapplier.setMaster(true);
        multiapplier.setIp("127.0.0.3");
        multiapplier.setPort(backupPort);
        multiapplier.setApplyMode(ApplyMode.transaction_table.getType());

        Applier slaveApplier = new Applier();
        slaveApplier.setTargetMhaName("mockTargetMha");
        slaveApplier.setTargetIdc("mockTargetIdc_*&^%");
        slaveApplier.setMaster(false);
        slaveApplier.setIp("127.0.0.3");
        slaveApplier.setPort(backupPort);
        slaveApplier.setApplyMode(ApplyMode.transaction_table.getType());
        List<Applier> surviveAppliers = Lists.newArrayList(applier, slaveApplier, multiapplier);

        Pair<String, Integer> applierMaster = new Pair<>(newReplicator.getIp(), newReplicator.getApplierPort());
        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(applierMaster);
        when(regionMetaCache.getCluster(cluster_multi)).thenReturn(dbCluster);
        when(currentMetaManager.getSurviveAppliers(cluster_multi, RegistryKey.from(applier.getTargetName(), applier.getTargetMhaName()))).thenReturn(surviveAppliers);

        DbCluster body = instanceStateController.addApplier(cluster_multi, applier);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Replicator> replicators = body.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0).getIp(), newReplicator.getIp());
        Assert.assertEquals(replicators.get(0).getApplierPort(), newReplicator.getApplierPort());
        Assert.assertEquals(replicators.get(0).getMaster(), true);

        List<Applier> appliers = body.getAppliers();
        Assert.assertEquals(appliers.size(), 1);
        Assert.assertEquals(appliers.get(0), applier);

        Thread.sleep(100);
    }

    @Test
    public void addMessengerWithMasterNotNull() throws InterruptedException {
        Messenger messenger = new Messenger();
        messenger.setMaster(true);
        messenger.setIp(LOCAL_IP);
        messenger.setPort(backupPort);
        messenger.setApplyMode(ApplyMode.mq.getType());

        when(currentMetaManager.getActiveReplicator(anyString())).thenReturn(dbCluster.getReplicators().get(0));
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.addMessenger(CLUSTER_ID, messenger);

        Assert.assertEquals(body.getDbs(), dbCluster.getDbs());
        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Messenger> messengers = body.getMessengers();
        Assert.assertEquals(messengers.size(), 1);
        Assert.assertEquals(messengers.get(0), messenger);

        Thread.sleep(100);
    }

    @Test
    public void applierMasterChange() throws InterruptedException {
        Pair<String, Integer> applierMaster = new Pair<>(newReplicator.getIp(), newReplicator.getApplierPort());
        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(applierMaster);
        when(currentMetaManager.getMySQLMaster(anyString())).thenReturn(switchedMySQLMaster);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        DbCluster body = instanceStateController.applierMasterChange(CLUSTER_ID, applierMaster, newApplier);

        Assert.assertNotEquals(body.getReplicators(), dbCluster.getReplicators());
        List<Replicator> replicators = body.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0).getIp(), newReplicator.getIp());
        Assert.assertEquals(replicators.get(0).getApplierPort(), newReplicator.getApplierPort());
        Assert.assertEquals(replicators.get(0).getMaster(), true);

        List<Applier> appliers = body.getAppliers();
        Assert.assertEquals(appliers.size(), 1);
        Assert.assertEquals(appliers.get(0), newApplier);

        Assert.assertNotEquals(body.getDbs(), dbCluster.getDbs());
        List<Db> bodyDbs = body.getDbs().getDbs();
        for (Db db : bodyDbs) {
            if (db.isMaster()) {
                Assert.assertEquals(db.getIp(), switchedMySQLMaster.getIp());
                Assert.assertEquals(db.getPort().intValue(), switchedMySQLMaster.getPort());
            }
        }

        Thread.sleep(100);
    }

    @Test
    public void mysqlMasterChanged() {
        Applier applier = new Applier();
        applier.setTargetMhaName("mockTargetMha_*&^%");
        applier.setTargetIdc("mockTargetIdc_*&^%");
        applier.setMaster(true);
        applier.setIp(LOCAL_IP);
        applier.setPort(backupPort);
        applier.setApplyMode(ApplyMode.transaction_table.getType());
        List<Applier> appliers = Lists.newArrayList(applier);

        Pair<String, Integer> applierMaster = new Pair<>(newReplicator.getIp(), newReplicator.getApplierPort());

        when(currentMetaManager.getApplierMaster(anyString(), anyString())).thenReturn(applierMaster);
        when(regionMetaCache.getCluster(CLUSTER_ID)).thenReturn(dbCluster);

        List<DbCluster> body = instanceStateController.mysqlMasterChanged(CLUSTER_ID, mysqlMaster, appliers, newReplicator);

        Assert.assertEquals(body.size(), appliers.size() + 1);
        DbCluster applierDbCluster = body.get(0);

        Assert.assertNotEquals(applierDbCluster.getReplicators(), dbCluster.getReplicators()); //
        List<Replicator> replicators = applierDbCluster.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0).getIp(), applierMaster.getKey());
        Assert.assertEquals(replicators.get(0).getApplierPort(), applierMaster.getValue());
        Assert.assertEquals(replicators.get(0).getMaster(), true);

        Db masterDb = applierDbCluster.getDbs().getDbs().get(0);
        Assert.assertEquals(masterDb.getIp(), mysqlMaster.getHost());
        Assert.assertEquals(masterDb.getPort().intValue(), mysqlMaster.getPort());

        DbCluster replicatorDbCluster = body.get(1);
        Assert.assertNotEquals(replicatorDbCluster.getReplicators(), dbCluster.getReplicators()); //
        replicators = replicatorDbCluster.getReplicators();
        Assert.assertEquals(replicators.size(), 1);
        Assert.assertEquals(replicators.get(0), newReplicator);

        masterDb = replicatorDbCluster.getDbs().getDbs().get(0);
        Assert.assertEquals(masterDb.getIp(), mysqlMaster.getHost());
        Assert.assertEquals(masterDb.getPort().intValue(), mysqlMaster.getPort());

    }

    @Test
    public void testRemoveApplier() throws InterruptedException {
        instanceStateController.removeApplier(CLUSTER_ID, newApplier, false);
        Thread.sleep(50);
    }

    @Test
    public void testRemoveMessenger() throws InterruptedException {
        instanceStateController.removeMessenger(CLUSTER_ID, newMessenger, false);
        Thread.sleep(50);
    }

    @Test
    public void testGetApplierInfo(){
        List<Applier> appliers = Lists.newArrayList(
                new Applier().setIp("127.0.0.1").setPort(8080)
        );
        ApplierInfoInquirer instance = ApplierInfoInquirer.getInstance();
        RestOperations mock = mock(RestOperations.class);
        ApplierInfoDto dto = new ApplierInfoDto();
        dto.setIp("127.0.0.1");
        dto.setRegistryKey("test.test");
        dto.setPort(8080);
        dto.setReplicatorIp("127.1.0.1");
        ApiResult<List<ApplierInfoDto>> objectApiResult = ApiResult.getSuccessInstance(Lists.newArrayList(dto));
        ResponseEntity value = new ResponseEntity(objectApiResult, HttpStatus.ACCEPTED);
        when(mock.exchange(anyString(),any(),any(HttpEntity.class),any(Class.class))).thenReturn(value);
        instance.setRestTemplate(mock);

        Pair<List<String>, List<ApplierInfoDto>> applierInfo = instanceStateController.getApplierInfoInner(appliers);
        System.out.println(applierInfo);
        Assert.assertEquals(1,applierInfo.getKey().size());
    }


    @Test
    public void testGetReplicatorInfo(){
        List<Applier> appliers = Lists.newArrayList(
                new Applier().setIp("127.0.0.1").setPort(8080)
        );
        ReplicatorInfoInquirer instance = ReplicatorInfoInquirer.getInstance();
        RestOperations mock = mock(RestOperations.class);
        ReplicatorInfoDto dto = new ReplicatorInfoDto();
        dto.setIp("127.0.0.1");
        dto.setRegistryKey("test.test");
        dto.setPort(8080);
        dto.setUpstreamMasterIp("127.1.0.1");
        ApiResult<List<ReplicatorInfoDto>> objectApiResult = ApiResult.getSuccessInstance(Lists.newArrayList(dto));
        ResponseEntity value = new ResponseEntity(objectApiResult, HttpStatus.ACCEPTED);
        when(mock.exchange(anyString(),any(),any(HttpEntity.class),any(Class.class))).thenReturn(value);
        instance.setRestTemplate(mock);

        Pair<List<String>, List<ReplicatorInfoDto>> replicatorInfo = instanceStateController.getReplicatorInfo(appliers);
        System.out.println(replicatorInfo);
        Assert.assertEquals(1,replicatorInfo.getKey().size());
    }
}
