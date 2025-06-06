package com.ctrip.framework.drc.console.service.v2.impl;

import static org.mockito.Mockito.when;

import com.ctrip.framework.drc.console.service.v2.CacheMetaService;
import com.ctrip.framework.drc.console.utils.MySqlUtils;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.AccountEndpoint;
import com.ctrip.framework.drc.core.driver.command.netty.endpoint.MySqlEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MysqlServiceV2ImplTest {
    
    @InjectMocks
    MysqlServiceV2Impl mysqlServiceV2;
    @Mock
    CacheMetaService cacheMetaService;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void getExistDrcMonitorTables() {
        List<String> tables = new ArrayList<>();
        tables.add("dly_db1");
        tables.add("tx_db1");
        tables.add("dly_db2");
        tables.add("tx_db3");
        Set<String> existDrcMonitorTables = MySqlUtils.getDbHasDrcMonitorTables(tables);
        Assert.assertTrue(existDrcMonitorTables.contains("db1"));
        Assert.assertFalse(existDrcMonitorTables.contains("db2"));
        Assert.assertFalse(existDrcMonitorTables.contains("db3"));
        Assert.assertFalse(existDrcMonitorTables.contains("db4"));
        Assert.assertEquals(1, existDrcMonitorTables.size());
        Assert.assertEquals(0, MySqlUtils.getDbHasDrcMonitorTables(Lists.newArrayList()).size());
    }

    // todo hdpan  
    @Test
    public void testCheckAccountsPrivileges() {
        when(cacheMetaService.getMasterEndpoint(Mockito.eq("mhaName"))).thenReturn(new MySqlEndpoint("host", 3306, "user", "password",true));
        try (MockedStatic<MySqlUtils> theMock = Mockito.mockStatic(MySqlUtils.class)) {
            theMock.when(() -> MySqlUtils.getAccountPrivilege(Mockito.any(AccountEndpoint.class),Mockito.anyBoolean())).thenAnswer(
                    invocation -> {
                        AccountEndpoint accountEndpoint = invocation.getArgument(0);
                        String privilegeFormatter = "GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO `%s`@`%%`";
                        return String.format(privilegeFormatter, accountEndpoint.getUser());
                    }
            );
            String s = mysqlServiceV2.queryAccountPrivileges("mhaName", "user", "password");
            Assert.assertNotNull(s);
        }
    }
    
}