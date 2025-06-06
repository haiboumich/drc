package com.ctrip.framework.drc.console.monitor.delay.task;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.ctrip.framework.drc.console.config.DefaultConsoleConfig;
import com.ctrip.framework.drc.console.dao.entity.v2.MhaTblV2;
import com.ctrip.framework.drc.console.enums.ActionEnum;
import com.ctrip.framework.drc.console.monitor.DefaultCurrentMetaManager;
import com.ctrip.framework.drc.console.monitor.delay.config.DataCenterService;
import com.ctrip.framework.drc.console.monitor.delay.config.MonitorTableSourceProvider;
import com.ctrip.framework.drc.console.pojo.MetaKey;
import com.ctrip.framework.drc.console.service.v2.CacheMetaService;
import com.ctrip.framework.drc.console.service.v2.CentralService;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.DefaultEndPoint;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.MySqlEndpoint;
import com.ctrip.framework.drc.core.entity.Drc;
import com.ctrip.framework.drc.core.monitor.datasource.DataSourceManager;
import com.ctrip.framework.drc.core.server.observer.endpoint.MasterMySQLEndpointObservable;
import com.ctrip.xpipe.api.observer.Observable;
import com.ctrip.xpipe.api.observer.Observer;
import com.google.common.collect.Sets;
import java.util.Set;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.tuple.Triple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ctrip.framework.drc.console.monitor.delay.config.MonitorTableSourceProvider.SWITCH_STATUS_ON;
import static com.ctrip.framework.drc.console.utils.UTConstants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author shenhaibo
 * @version 1.0
 * date: 2020-05-08
 */
public class PeriodicalUpdateDbTaskTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @InjectMocks private PeriodicalUpdateDbTask task;

    @Mock private DataCenterService dataCenterService;

    @Mock private DefaultCurrentMetaManager currentMetaManager;

    @Mock private MonitorTableSourceProvider monitorTableSourceProvider;

    @Mock private DefaultConsoleConfig consoleConfig;

    @Mock private CentralService centralService;

    @Mock private PeriodicalUpdateDbTaskV2 periodicalUpdateDbTaskV2;
    
    @Mock private CacheMetaService cacheMetaService;
    



    private Drc drc;

//    private Set<Long> mhaIdsInSameGroup;

//    private Map<String, DelayMonitorConfig> delayMonitorConfigs;

//    private DelayMonitorConfig delayMonitorConfig1;
//    private DelayMonitorConfig delayMonitorConfig3;

    private DataSource dataSource;

    private static final int PORT = 9876;

    private static final String IP = "127.0.0.1";

    private static final String MYSQL_USER = "root";

    private static final String MYSQL_PASSWORD = "";

//    private static final String CLUSTER = "drc-Test01";

//    private static final String MHA_NT1 = "fxdrcnt";
//    private static final String MHA_ST1 = "fxdrcst";
//    private static final String MHA_NT2 = "fxdrcnt2";
//    private static final String MHA_ST2 = "fxdrcst2";

    private static DB db;

    private static final String CREATE_DB = "create database drcmonitordb;";

    private static final String USE_DB = "use drcmonitordb;";

    private static final String CREATE_TABLE = "CREATE TABLE `delaymonitor` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
            "  `src_ip` varchar(15) NOT NULL,\n" +
            "  `dest_ip` varchar(255) NOT NULL,\n" +
            "  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;";

    private static final String INIT_TBL = "INSERT INTO `drcmonitordb`.`delaymonitor`(`src_ip`, `dest_ip`) VALUES('dc1', 'mha1dc1');";

    private static final String SELECT_ALL = "select * from `drcmonitordb`.`delaymonitor`;";

    private static final MySqlEndpoint mysqlEndpoint = new MySqlEndpoint("127.0.0.1", 9876, "root", "", true);

    private MetaKey metaKey1;
    private MetaKey metaKey2;

    @Before
    public void setUp() throws Exception {
        // for db
        createDb();

        metaKey1 = new MetaKey.Builder()
                .dc(DC1)
                .clusterId(CLUSTER_ID1)
                .clusterName(CLUSTER1)
                .mhaName(MHA1DC1)
                .build();
        metaKey2 = new MetaKey.Builder()
                .dc(DC2)
                .clusterId(CLUSTER_ID1)
                .clusterName(CLUSTER1)
                .mhaName(MHA1DC2)
                .build();

        MockitoAnnotations.openMocks(this);
        task.setLocalDcName(DC1);
        when(dataCenterService.getDc()).thenReturn(DC1);
        when(consoleConfig.getLocalConfigCloudDc()).thenReturn(Sets.newHashSet("dc-readMhaFromConfig"));
        when(consoleConfig.getRegion()).thenReturn("sha");
        when(consoleConfig.getRegionForDc(Mockito.anyString())).thenReturn("sha");
        when(consoleConfig.getDcsInLocalRegion()).thenReturn(Sets.newHashSet(Lists.newArrayList(DC1)));
        Mockito.doNothing().when(currentMetaManager).addObserver(Mockito.any());
        when(monitorTableSourceProvider.getDelayMonitorUpdatedbSwitch()).thenReturn(SWITCH_STATUS_ON);

        MhaTblV2 mhaTbl1 = new MhaTblV2();
        mhaTbl1.setMhaName(MHA1DC1);
        mhaTbl1.setId(4L);
        MhaTblV2 mhaTbl2 = new MhaTblV2();
        mhaTbl2.setId(2L);
        mhaTbl2.setMhaName(MHA1DC2);

        Mockito.doReturn(Lists.newArrayList(mhaTbl1)).when(centralService).getMhaTblV2s(Mockito.eq(DC1));
        Mockito.doReturn(Lists.newArrayList(mhaTbl2)).when(centralService).getMhaTblV2s(Mockito.eq(DC2));
        task.isleader();
    }

    @Test
    public void testReconnect() throws Exception {
        Triple<MetaKey, MySqlEndpoint, ActionEnum> triple = new Triple<>(metaKey1, mysqlEndpoint, ActionEnum.ADD);
        task.update(triple, new LocalMasterMySQLEndpointObservable());
        task.setInitialDelay(0);
        task.setPeriod(100);
        task.setTimeUnit(TimeUnit.MILLISECONDS);
        String timestamp1 = getTimestamp();
        task.start();
        Thread.sleep(1000);
        String timestamp2 = getTimestamp();
        System.out.println("time1: " + timestamp1 + ", time2: " + timestamp2);
        Assert.assertNotEquals(timestamp1, timestamp2);

        // simulate db disconnection
        logger.info("db is being stopped for simulation");
        db.stop();
        DataSourceManager.getInstance().clearDataSource(new DefaultEndPoint(IP, PORT, MYSQL_USER, MYSQL_PASSWORD));
        logger.info("db is stopped for simulation");
        createDb();
        String timestamp3 = getTimestamp();
        Thread.sleep(1000);
        String timestamp4 = getTimestamp();
        System.out.println("time3: " + timestamp3 + ", time4: " + timestamp4);
        Assert.assertNotEquals(timestamp3, timestamp4);
    }

    @Test
    public void testUpdate() {
        task.setLocalDcName(DC1);
        Triple<MetaKey, MySqlEndpoint, ActionEnum> triple1 = new Triple<>(metaKey2, MYSQL_ENDPOINT1_MHA1DC1, ActionEnum.ADD);
        Triple<MetaKey, MySqlEndpoint, ActionEnum> triple2 = new Triple<>(metaKey1, MYSQL_ENDPOINT1_MHA1DC1, ActionEnum.ADD);
        Triple<MetaKey, MySqlEndpoint, ActionEnum> triple3 = new Triple<>(metaKey1, MYSQL_ENDPOINT2_MHA1DC1, ActionEnum.UPDATE);
        Triple<MetaKey, MySqlEndpoint, ActionEnum> triple4 = new Triple<>(metaKey1, MYSQL_ENDPOINT2_MHA1DC1, ActionEnum.DELETE);
        task.update(triple1, new Observable() {
            @Override
            public void addObserver(Observer observer) {
            }
            @Override
            public void removeObserver(Observer observer) {
            }
        });
        Assert.assertEquals(0, task.getMasterMySQLEndpointMap().size());

        task.update(triple2, new LocalMasterMySQLEndpointObservable());
        Assert.assertEquals(1, task.getMasterMySQLEndpointMap().size());
        Assert.assertEquals(MYSQL_ENDPOINT1_MHA1DC1, task.getMasterMySQLEndpointMap().get(metaKey1));

        task.update(triple3, new LocalMasterMySQLEndpointObservable());
        Assert.assertEquals(1, task.getMasterMySQLEndpointMap().size());
        Assert.assertEquals(MYSQL_ENDPOINT2_MHA1DC1, task.getMasterMySQLEndpointMap().get(metaKey1));

        task.update(triple4, new LocalMasterMySQLEndpointObservable());
        Assert.assertEquals(0, task.getMasterMySQLEndpointMap().size());
    }

    @Test
    public void testGetSrcMhasShouldMonitor() {
        Map<MetaKey, MySqlEndpoint> origin = task.getMasterMySQLEndpointMap();

        // test getMhaInLocalRegionRelatedByDstMha
        Map<MetaKey, MySqlEndpoint> mock = new HashMap<>();
        mock.put(new MetaKey(null, null, null, "mha1"), null);
        mock.put(new MetaKey(null, null, null, "mha2"), null);
        mock.put(new MetaKey(null, null, null, "mha_db_1_src"), null);
        task.setMasterMySQLEndpointMap(mock);

        Map<String, List<String>> map = new HashMap<>();
        map.put("mha_db_1_src", Lists.newArrayList("mha_db_1_dst"));
        when(periodicalUpdateDbTaskV2.getMhaDbRelatedByDestMha(eq("dstMha"))).thenReturn(map);
        Set<String> test = task.getMhaInLocalRegionRelatedByDstMha("dstMha");
        Assert.assertEquals(2, test.size());
        Assert.assertFalse(test.contains("mha_db_1_src"));
        
        // test getSrcMhasShouldMonitor
        when(cacheMetaService.getSrcMhasHasReplication(eq("dstMha"))).thenReturn(Sets.newHashSet("mha1","mha2","mha_db_1_src","mha3InOtherDc"));
        test = task.getSrcMhasShouldMonitor("dstMha");
        Assert.assertEquals(2, test.size());

        task.setMasterMySQLEndpointMap(origin);
    }
    

    private void createDb() throws InterruptedException {
        try {
            db = getDb(PORT);
        } catch (ManagedProcessException e) {
            logger.error("Create db error: ", e);
        }

        dataSource = DataSourceManager.getInstance().getDataSource(new DefaultEndPoint(IP, PORT, MYSQL_USER, MYSQL_PASSWORD));
        try(Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_DB);
            stmt.execute(USE_DB);
            stmt.execute(CREATE_TABLE);
            stmt.execute(INIT_TBL);
        } catch (Exception e) {
            logger.error("init db error: ", e);
        }
    }

    private String getTimestamp() {
        try(Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            if(rs.next()) {
                return rs.getString(4);
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return "";
    }

    /**
     * brew update && brew upgrade
     * brew uninstall --ignore-dependencies openssl
     * brew install https://github.com/tebelorg/Tump/releases/download/v1.0.0/openssl.rb
     *
     *
     * ssl need 1.0.0 see https://github.com/kelaberetiv/TagUI/issues/635
     * @param port
     * @return
     * @throws ManagedProcessException
     */
    private static DB getDb(int port) throws ManagedProcessException {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(port);
        builder.addArg("--user=root");
        DB db = DB.newEmbeddedDB(builder.build());
        db.start();
        return db;
    }

    @After
    public void tearDown() {
        try {
            db.stop();
            DataSourceManager.getInstance().clearDataSource(new DefaultEndPoint(IP, PORT, MYSQL_USER, MYSQL_PASSWORD));
            logger.info("db is stopped");
        } catch (Exception e) {
            logger.error("Stop db error: ", e);
        }
    }

    public static final class LocalMasterMySQLEndpointObservable implements MasterMySQLEndpointObservable {

        @Override
        public void addObserver(Observer observer) {

        }

        @Override
        public void removeObserver(Observer observer) {

        }
    }
}
