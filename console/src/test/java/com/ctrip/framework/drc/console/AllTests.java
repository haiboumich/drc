package com.ctrip.framework.drc.console;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.ctrip.framework.drc.console.aop.RemoteHttpAspectTest;
import com.ctrip.framework.drc.console.aop.log.LogRecordAspectTest;
import com.ctrip.framework.drc.console.aop.permission.AccessTokenAspectTest;
import com.ctrip.framework.drc.console.config.DbClusterRetrieverTest;
import com.ctrip.framework.drc.console.config.DefaultConsoleConfigTest;
import com.ctrip.framework.drc.console.config.UdlMigrateConfigurationTest;
import com.ctrip.framework.drc.console.controller.*;
import com.ctrip.framework.drc.console.controller.log.OperationLogControllerTest;
import com.ctrip.framework.drc.console.controller.v2.CentralServiceControllerTest;
import com.ctrip.framework.drc.console.controller.v2.MessengerControllerV2Test;
import com.ctrip.framework.drc.console.controller.v2.MetaControllerV2Test;
import com.ctrip.framework.drc.console.controller.v2.MhaReplicationControllerTest;
import com.ctrip.framework.drc.console.dto.MhaInstanceGroupDtoTest;
import com.ctrip.framework.drc.console.dto.v2.MhaDbDelayInfoDtoTest;
import com.ctrip.framework.drc.console.dto.v3.*;
import com.ctrip.framework.drc.console.enums.EnvEnumTest;
import com.ctrip.framework.drc.console.enums.EstablishStatusEnumTest;
import com.ctrip.framework.drc.console.monitor.*;
import com.ctrip.framework.drc.console.monitor.consistency.cases.RangeQueryCheckPairCaseTest;
import com.ctrip.framework.drc.console.monitor.consistency.sql.operator.SqlOperatorTest;
import com.ctrip.framework.drc.console.monitor.consistency.table.DefaultTableProviderTest;
import com.ctrip.framework.drc.console.monitor.delay.DelayMapTest;
import com.ctrip.framework.drc.console.monitor.delay.MqDelayMonitorServerTest;
import com.ctrip.framework.drc.console.monitor.delay.config.*;
import com.ctrip.framework.drc.console.monitor.delay.config.v2.MetaProviderV2Test;
import com.ctrip.framework.drc.console.monitor.delay.impl.driver.DelayMonitorPooledConnectorTest;
import com.ctrip.framework.drc.console.monitor.delay.impl.execution.GeneralSingleExecution;
import com.ctrip.framework.drc.console.monitor.delay.impl.operator.WriteSqlOperatorWrapper;
import com.ctrip.framework.drc.console.monitor.delay.server.StaticDelayMonitorServerTest2;
import com.ctrip.framework.drc.console.monitor.delay.task.ListenReplicatorTaskTest;
import com.ctrip.framework.drc.console.monitor.delay.task.PeriodicalUpdateDbTaskTest;
import com.ctrip.framework.drc.console.monitor.delay.task.PeriodicalUpdateDbTaskV2Test;
import com.ctrip.framework.drc.console.monitor.gtid.function.CheckDbGtidTest;
import com.ctrip.framework.drc.console.monitor.gtid.function.CheckGtidTest;
import com.ctrip.framework.drc.console.monitor.healthcheck.task.ExecutedGtidQueryTaskTest;
import com.ctrip.framework.drc.console.monitor.task.*;
import com.ctrip.framework.drc.console.param.v2.DrcAutoBuildReqTest;
import com.ctrip.framework.drc.console.param.v2.MhaReplicationQueryTest;
import com.ctrip.framework.drc.console.param.v2.RowsFilterCreateParamTest;
import com.ctrip.framework.drc.console.pojo.CreatePojoTest;
import com.ctrip.framework.drc.console.pojo.TableConfigsTest;
import com.ctrip.framework.drc.console.service.checker.ConflictLogCheckerTest;
import com.ctrip.framework.drc.console.service.filter.QConfigApiServiceTest;
import com.ctrip.framework.drc.console.service.filter.RowsFilterMetaMappingServiceTest;
import com.ctrip.framework.drc.console.service.filter.RowsFilterMetaServiceTest;
import com.ctrip.framework.drc.console.service.impl.*;
import com.ctrip.framework.drc.console.service.log.ConflictApprovalServiceTest;
import com.ctrip.framework.drc.console.service.log.ConflictLogManagerTest;
import com.ctrip.framework.drc.console.service.log.ConflictLogServiceTest;
import com.ctrip.framework.drc.console.service.log.DbBlacklistCacheTest;
import com.ctrip.framework.drc.console.service.remote.qconfig.QConfigServiceImplTest;
import com.ctrip.framework.drc.console.service.v2.*;
import com.ctrip.framework.drc.console.service.v2.dbmigration.MigrationTaskManagerTest;
import com.ctrip.framework.drc.console.service.v2.dbmigration.impl.DbMigrationServiceImplTest;
import com.ctrip.framework.drc.console.service.v2.external.dba.DbaApiServiceTest;
import com.ctrip.framework.drc.console.service.v2.impl.*;
import com.ctrip.framework.drc.console.service.v2.impl.migrate.MetaCompareServiceImplTest;
import com.ctrip.framework.drc.console.service.v2.security.impl.AccountServiceImplTest;
import com.ctrip.framework.drc.console.service.v2.security.impl.MetaAccountServiceImplTest;
import com.ctrip.framework.drc.console.task.SyncMhaTaskTest;
import com.ctrip.framework.drc.console.utils.*;
import com.ctrip.framework.drc.console.utils.convert.TableNameBuilderTest;
import com.ctrip.framework.drc.console.vo.display.v2.MhaReplicationPreviewDtoTest;
import com.ctrip.framework.drc.console.vo.request.MhaDbQueryDtoTest;
import com.ctrip.framework.drc.console.vo.request.MhaDbReplicationQueryDtoTest;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.DefaultEndPoint;
import com.ctrip.xpipe.api.endpoint.Endpoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.ctrip.framework.drc.console.utils.UTConstants.*;


/**
 * @author shenhaibo
 * @version 1.0
 * date: 2019-12-23
 * There are two empty docker MySQL instances set up by CI/CD while doing pipeline, we can check it through DRC gitlab main page - Settings - CI/CD - Expand Ctrip Auto DevOps - Edit Jobs - Package - Services.
 * Some tests may depends on the MySQL provided by CI/CD, which means the tests may not pass locally, for instance, check MySQLMonitorTest for CI/CD provided MySQL usage.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MonitorTableSourceProviderTest.class,
        MysqlConfigsMonitorTest.class,
        DalServiceImplTest.class,
        DbClusterRetrieverTest.class,

        TableConfigsTest.class,

        DaoConfigTest.class,
        RemoteConfigTest.class,

        // jdbc
        SqlOperatorTest.class,

        ConfigServiceImplTest.class,
        SwitchServiceImplTest.class,

        DbClusterSourceProviderTest.class,
        ConflictLogCheckerTest.class,

        OpenApiServiceImplTest.class,
        RemoteHttpAspectTest.class,
        AccessTokenAspectTest.class,
        LogRecordAspectTest.class,
        QConfigServiceImplTest.class,

        RowsFilterMetaServiceTest.class,
        RowsFilterMetaMappingServiceTest.class,
        QConfigApiServiceTest.class,

        ColumnsFilterServiceV2Test.class,
        DataMediaServiceV2Test.class,
        MessengerServiceV2Test.class,
        MetaGeneratorV5Test.class,
        RowsFilterServiceV2Test.class,
        MetaCompareServiceImplTest.class,
        CacheMetaServiceImplTest.class,
        DbMetaCorrectServiceImplTest.class,
        MetaInfoServiceV2ImplTest.class,
        MhaServiceV2ImplTest.class,
        MhaReplicationServiceV2ImplTest.class,
        MhaDbMappingServiceTest.class,
        DrcBuildServiceV2Test.class,
        MessengerServiceV2ImplTest.class,
        MetaInfoServiceV2ImplTest2.class,
        ResourceServiceTest.class,
        DbMigrationServiceImplTest.class,
        DbMigrationServiceTest.class,
        DbaApiServiceTest.class,
        DrcAutoBuildServiceImplTest.class,
        MachineServiceImplTest.class,
        MigrationTaskManagerTest.class,
        ConflictLogServiceTest.class,
        ConflictApprovalServiceTest.class,
        CentralServiceTest.class,
        RouteServiceTest.class,
        DbDrcBuildServiceImplTest.class,
        MhaDbReplicationServiceImplTest.class,
        MysqlServiceV2ImplTest.class,
        CentralServiceImplTest.class,
        DbBlacklistCacheTest.class,
        DrcApplicationServiceTest.class,
        MetaAccountServiceImplTest.class,
//
        // controller
        OperationLogControllerTest.class,
        MetaControllerV2Test.class,
        MhaReplicationControllerTest.class,
        MessengerControllerV2Test.class,
        AccessControllerTest.class,
        SwitchControllerTest.class,
        ConfigControllerTest.class,
//        UserControllerTest.class,
        MetaControllerTest.class,
        OpenApiControllerTest.class,
        RowsFilterMetaControllerTest.class,
        CentralServiceControllerTest.class,

        //task
        ExecutedGtidQueryTaskTest.class,
        MetaProviderV2Test.class,
        // monitor
        SyncMhaTaskTest.class,
        MySqlUtilsTest.class,
        JsonUtilsTest.class,
        CheckGtidTest.class,
        CheckDbGtidTest.class,
        PeriodicalUpdateDbTaskTest.class,
        PeriodicalUpdateDbTaskV2Test.class,
        AbstractMonitorTest.class,
        UuidMonitorTest.class,
        MqDelayMonitorServerTest.class,
        AutoIncrementCheckTaskTest.class,
        TableStructureCheckTaskTest.class,
        ShardedDbReplicationConsistencyCheckTaskTest.class,
        MultiTruncateMonitorTest.class,
        ConflictRowsLogCountTaskTest.class,
        DrcApplicationFormCheckTaskTest.class,
        AccountServiceImplTest.class,

        // config
        DefaultConsoleConfigTest.class,
        DelayMonitorSlaveConfigTest.class,
        UdlMigrateConfigurationTest.class,

        // utils
        JacksonUtilsTest.class,
        XmlUtilsTest.class,
        EnvEnumTest.class,
        TableNameBuilderTest.class,
        StreamUtilsTest.class,

        DefaultTableProviderTest.class,
        RangeQueryCheckPairCaseTest.class,

        OPSServiceImplTest.class,
        SSOServiceImplTest.class,

        EstablishStatusEnumTest.class,
        DelayMapTest.class,

        DefaultCurrentMetaManagerTest.class,
        CurrentDstMetaManagerTest.class,
        MonitorServiceV2ImplTest.class,

        FileConfigTest.class,
        CompositeConfigTest.class,

        DelayMonitorPooledConnectorTest.class,

        ListenReplicatorTaskTest.class,
        ConflictLogManagerTest.class,
        StaticDelayMonitorServerTest2.class,

        //entity
        MhaInstanceGroupDtoTest.class,
        MhaReplicationQueryTest.class,
        RowsFilterCreateParamTest.class,
        CreatePojoTest.class,
        MhaDbDelayInfoDtoTest.class,
        MhaDbQueryDtoTest.class,
        MhaApplierDtoTest.class,
        DbApplierSwitchReqDtoTest.class,
        MhaDbReplicationQueryDtoTest.class,
        DbMqCreateDtoTest.class,
        DbMqEditDtoTest.class,
        DrcAutoBuildReqTest.class,
        MhaDbReplicationCreateDtoTest.class,
        MhaReplicationPreviewDtoTest.class,
        DbMqConfigInfoDtoTest.class
})
public class AllTests {

    private static Logger logger = LoggerFactory.getLogger(AllTests.class);

    public static String DRC_XML_one2many;

    public static String DRC_XML;

    public static String DRC_XML1;

    public static String DRC_XML2;

    public static String DRC_XML2_OLD;

    public static String DRC_XML2_1;

    public static String DRC_XML2_2;

    public static String META_COMPLICATED;

    private static DB srcDb;

    private static DB dstDb;

    private static DB testDb;

    private static WriteSqlOperatorWrapper writeSqlOperatorWrapper;

    public static Endpoint ciEndpoint = new DefaultEndPoint(CI_MYSQL_IP, CI_PORT1, CI_MYSQL_USER, CI_MYSQL_PASSWORD);

    public static WriteSqlOperatorWrapper ciWriteSqlOperatorWrapper;

    @BeforeClass
    public static void setUp() {
        try {
            init();
        } catch (Throwable t) {
            logger.error("Fail start test db", t);
        }
    }

    public static void init() throws ManagedProcessException {
        if(DRC_XML_one2many == null){
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META_one2many).getPath();
            DRC_XML_one2many = readFileContent(file);
        }
        if (DRC_XML == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META).getPath();
            DRC_XML = readFileContent(file);
            logger.info("meta: {}", DRC_XML);
        }
        if (DRC_XML1 == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META1).getPath();
            DRC_XML1 = readFileContent(file);
            logger.info("meta1: {}", DRC_XML1);
        }
        if (DRC_XML2 == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META2).getPath();
            DRC_XML2 = readFileContent(file);
            logger.info("meta2: {}", DRC_XML2);
        }
        if (DRC_XML2_OLD == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META2OLD).getPath();
            DRC_XML2_OLD = readFileContent(file);
            logger.info("meta2: {}", DRC_XML2_OLD);
        }
        if (DRC_XML2_1 == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META2_1).getPath();
            DRC_XML2_1 = readFileContent(file);
            logger.info("meta2_1: {}", DRC_XML2_1);
        }
        if (DRC_XML2_2 == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_FILE_META2_2).getPath();
            DRC_XML2_2 = readFileContent(file);
            logger.info("meta2_2: {}", DRC_XML2_2);
        }
        if (META_COMPLICATED == null) {
            String file = ClassUtils.getDefaultClassLoader().getResource(XML_COMPLICATED).getPath();
            META_COMPLICATED = readFileContent(file);
            logger.info("meta complicated: {}", META_COMPLICATED);
        }
        if (testDb == null) {
            // fxdrcmetadb for test
            testDb = getDb(12345);
        }
        if (writeSqlOperatorWrapper == null) {
            writeSqlOperatorWrapper = new WriteSqlOperatorWrapper(new DefaultEndPoint(MYSQL_IP, META_DB_PORT, MYSQL_USER, null));
            try {
                writeSqlOperatorWrapper.initialize();
                writeSqlOperatorWrapper.start();
            } catch (Exception e) {
                logger.error("sqlOperatorWrapper initialize: ", e);
            }
        }
        if (srcDb == null) {
            srcDb = getDb(SRC_PORT);
        }
        if (dstDb == null) {
            dstDb = getDb(DST_PORT);
        }
    }

    public static void initTestDb() throws ManagedProcessException {
        if (testDb == null) {
            // fxdrcmetadb for test
            testDb = getDb(12345);
        }
    }


    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }


    @AfterClass
    public static void tearDown() {
        try {
            srcDb.stop();
            dstDb.stop();
            testDb.stop();
            if (null != writeSqlOperatorWrapper) {
                writeSqlOperatorWrapper.stop();
                writeSqlOperatorWrapper.dispose();
            }
            if (null != ciWriteSqlOperatorWrapper) {
                ciWriteSqlOperatorWrapper.stop();
                ciWriteSqlOperatorWrapper.dispose();
            }
        } catch (Exception e) {
            logger.error("tearDown: ", e);
        }
    }

    /**
     * brew update && brew upgrade
     * brew uninstall --ignore-dependencies openssl
     * brew install https://github.com/tebelorg/Tump/releases/download/v1.0.0/openssl.rb
     * <p>
     * <p>
     * ssl need 1.0.0 see https://github.com/kelaberetiv/TagUI/issues/635
     *
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
        db.source("db/init.sql");
        return db;
    }

    public static void createIfAbsentCiWriteSqlOperatorWrapper() {
        if (null == ciWriteSqlOperatorWrapper) {
            ciWriteSqlOperatorWrapper = new WriteSqlOperatorWrapper(ciEndpoint);
            try {
                ciWriteSqlOperatorWrapper.initialize();
                ciWriteSqlOperatorWrapper.start();
            } catch (Exception e) {
                logger.error("sqlOperatorWrapper initialize: ", e);
            }
        }
    }

    public static void insertCiMuchDataDb(int count) {
        String createDatabase = "CREATE DATABASE IF NOT EXISTS `testdb`;";
        String createTable = "CREATE TABLE IF NOT EXISTS `testdb`.`customer` (\n" +
                "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(15) NOT NULL,\n" +
                "  `gender` varchar(15) NOT NULL,\n" +
                "  `datachange_lasttime` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),\n" +
                "  `UID` varchar(20) DEFAULT NULL COMMENT 'userid',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;";
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT `testdb`.`customer`(`name`, `gender`) values('testname', 'testgender')");
        for (int i = 0; i < count - 1; i++) {
            sb.append(",('testname', 'testgender')");
        }
        sb.append(";");
//        String insertTable = "INSERT `testdb`.`customer`(`name`, `gender`) values('testname', 'testgender');";
        String insertTable = sb.toString();
        List<String> sqls = new LinkedList<>();
        sqls.add(createDatabase);
        sqls.add(createTable);
        sqls.add(insertTable);

        createIfAbsentCiWriteSqlOperatorWrapper();
        for (String sql : sqls) {
            GeneralSingleExecution execution = new GeneralSingleExecution(sql);
            try {
                ciWriteSqlOperatorWrapper.write(execution);
            } catch (SQLException throwables) {
                logger.error("Failed execute : {}", sql, throwables);
            }
        }
        try {
            ciWriteSqlOperatorWrapper.stop();
            ciWriteSqlOperatorWrapper.dispose();
        } catch (Exception e) {
            logger.error("Clear ci conn pool resource, ", e);
        }
    }

    public static void dropAllCiDb() {
        createIfAbsentCiWriteSqlOperatorWrapper();
        String DROP_DB = "drop database %s";
        List<MySqlUtils.TableSchemaName> tableSchemaNames = MySqlUtils.getTables(ciEndpoint, GET_ALL_TABLES, true);
        List<String> tables = tableSchemaNames.stream().map(MySqlUtils.TableSchemaName::toString).collect(Collectors.toList());
        Set<String> dbs = new HashSet<>();
        tables.forEach(table -> {
            String[] split = table.split("\\.");
            String db = split[0];
            dbs.add(db);
        });
        dbs.forEach(db -> {
            String sql = String.format(DROP_DB, db);
            GeneralSingleExecution execution = new GeneralSingleExecution(sql);
            try {
                ciWriteSqlOperatorWrapper.write(execution);
            } catch (SQLException throwables) {
                logger.error("Failed drop db : {}", db, throwables);
            }
        });
    }

    public static <T> boolean weakEquals(Collection<? extends T> c1, Collection<? extends T> c2) {
        if (c1.size() != c2.size()) return false;
        Set<T> set1 = new HashSet<>(c1);
        Set<T> set2 = new HashSet<>(c2);
        if (set1.size() != set2.size()) return false;
        for (T t : set1) {
            if (!set2.contains(t)) return false;
        }
        return true;
    }


}
