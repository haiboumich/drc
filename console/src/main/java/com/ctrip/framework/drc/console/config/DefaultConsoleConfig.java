package com.ctrip.framework.drc.console.config;

import com.ctrip.framework.drc.console.config.meta.DcInfo;
import com.ctrip.framework.drc.console.utils.Constants;
import com.ctrip.framework.drc.core.config.RegionConfig;
import com.ctrip.xpipe.api.codec.GenericTypeReference;
import com.ctrip.xpipe.api.config.Config;
import com.ctrip.xpipe.codec.JsonCodec;
import com.ctrip.xpipe.config.AbstractConfigBean;
import com.ctrip.xpipe.utils.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author shenhaibo
 * @version 1.0
 * date: 2020-07-02
 */
@Component("defaultConsoleConfig")
public class DefaultConsoleConfig extends AbstractConfigBean {


    private RegionConfig regionConfig = RegionConfig.getInstance();

    public static String KEY_DC_INFOS = "drc.dcinfos";
    public static final String SWITCH_META_ROLL_BACK = "switch.meta.roll.back";
    public static final String SWITCH_CM_REGION_URL = "switch.cm.region.url";
    public static final String SWITCH_ON = "on";
    public static final String SWITCH_OFF = "off";

    public static String DEFAULT_SWITCH_CM_REGION_URL = "off";

    public static String DBA_DC_INFOS = "dba.dcinfos";

    public static String CONSOLE_DC_INFOS = "drc.console.dcinfos";

    public static String VALIDATION_DC_INFOS = "drc.validation.dcinfos";

    public static String CONSOLE_DC_ENDPOINT_INFOS = "drc.console.dcinfos.endpoint";

    public static String BEACON_PREFIX = "beacon.prefix";

    public static String UID_DATA_PREFIX = "uid.data.%s";

    public static String UCS_STRATEGY_PREFIX = "ucs.strategy.%s";

    private String defaultBeaconPrefix = "";

    public static String MHA_DAL_CLUSTER_INFOS = "mha.dalcluster.info";

    public static String DELAY_EXCEPTION_TIME = "delay.exception";
    public static String DELAY_UPDATE_TIMEOUT_TIME = "delay.update.task.timeout.milliseconds";

    public static String DEFAULT_DELAY_EXCEPTION_TIME = "864500000";

    private Map<String, String> dbaDcInfos = Maps.newConcurrentMap();

    private Map<String, String> consoleDcInfos = Maps.newConcurrentMap();

    private Map<String, String> consoleDcEndpointInfos = Maps.newConcurrentMap();

    private String defaultDcInfos = "{}";

    private String defaultDbaDcInfos = "{}";

    private String defaultConsoleDcInfos = "{}";

    private String defaultValidationDcInfos = "{}";

    private String defaultMhaDalClusterInfos = "{}";

    private String DEFAULT_MAPPING_INFOS = "{}";

    private static String PUBLIC_CLOUD_DC = "drc.public.cloud.dc";
    private static String DEFAULT_PUBLIC_CLOUD_DC = "shali";
    private static String PUBLIC_CLOUD_REGION = "drc.public.cloud.region";
    private static String DEFAULT_PUBLIC_CLOUD_REGION = "fra,sin,ali";
    private static String CENTER_REGION = "console.center.region";
    private static String DEFAULT_CENTER_REGION = "sha";
    private static String DRC_CENTER_IDC = "drc.center.idc";

    private static String LOCAL_CONFIG_CLOUD_DC = "local.config.cloud.dc";
    private static String DEFAULT_LOCAL_CONFIG_CLOUD_DC = "sinibuaws,sinibualiyun";
    private static String LOCAL_CONFIG_MONITOR_MHAS = "local.config.monitor.mhas";
    private static String DEFAULT_LOCAL_CONFIG_MONITOR_MHAS = "";
    private static String LOCAL_CONFIG_MHAS_MAP = "local.config.mhas.nameidmap";
    private static String DEFAULT_LOCAL_CONFIG_MHAS_MAP = "{}";
    
    private static String CONFLICT_RECORD_SEARCH_TIME = "conflict.mha.record.search.time";
    private static int DEFAULT_CONFLICT_RECORD_SEARCH_TIME = 120;

    private static final String UPDATE_REPLICATOR_MASTER_SWITCH = "update.replicator.master.switch";
    
    private static String AVAILABLE_PORT_SIZE ="available.port.size";
    private static int DEFAULT_AVAILABLE_PORT_SIZE = 50;
    private static String VPC_MHA = "vpc.mha";

    private static String META_COMPARE_PARALLEL ="meta.compare.parallel";
    private static int DEFAULT_META_COMPARE_PARALLEL = 10;
    private static String COST_TIME_TRACE_SWITCH ="cost.time.trace.switch";
    private static String DRC_DOUBLE_WRITE_SWITCH = "drc.double.write.switch";
    private static String NEW_DRC_CONFIG_SWITCH = "new.drc.config.switch";
    private static String META_COMPARE_SWITCH = "meta.compare.switch";
    private static String META_GENERATOR_V5_SWITCH = "meta.generator.v5.switch";
    private static String META_DB_APPLIER_CONFIG_SWITCH = "meta.db.applier.config.switch";
    private static String META_DB_APPLIER_CONFIG_SWITCH_KEY = META_DB_APPLIER_CONFIG_SWITCH + ".%s";
    private static String META_REALTIME_SWITCH = "meta.realtime";

    private static final String CONFLICT_LOG_RECORD_SWITCH = "conflict.log.record.switch";
    private static String CFL_BLACK_LIST_AUTO_ADD_SWITCH = "cfl.blacklist.new.config.clear.switch"; // auto add must auto clear
    private static String CONFLICT_DB_OWNER_APPROVAL_SWITCH = "conflict.db.owner.approval.switch";
    private static String CONFLICT_LOG_QUERY_TIME_INTERVAL = "conflict.log.query.time.interval";
    private static String TABLE_STRUCTURE_CHECK_SWITCH = "table.structure.check.switch";
    private static String DB_REPLICATION_CONSISTENCY_CHECK_SWITCH = "db.replication.consistency.check.switch";


    private static final String DBA_DC_2_DRC_DC_MAP = "dbadc.drcdc.map";
    private static final String DEFAULT_DBA_DC_2_DRC_DC_MAP = "{}";
    
    private static final String DRC_ACCESS_TOKEN_KEY = "drc.access.token.key";
    private static final String OPERATION_LOG_SWITCH = "operation.log.switch";

    private static final long DEFAULT_REPLICATOR_SIZE = 20;
    private static final String REPLICATOR_MAX_SIZE = "replicator.max.size";

    private static final String DRC_CONFIG_APPLICATION_SWITCH = "drc.config.application.swtich";
    private static final String DEFAULT_READ_ACCOUNT_KMS_TOKEN= "default.read.account.kms.token";
    private static final String DEFAULT_WRITE_ACCOUNT_KMS_TOKEN= "default.write.account.kms.token";
    private static final String DEFAULT_MONITOR_ACCOUNT_KMS_TOKEN= "default.monitor.account.kms.token";
    
    private static final String ACCOUNT_KMS_TOKEN_SWITCH = "account.kms.token.switch";
    private static final String ACCOUNT_KMS_TOKEN_SWITCH_V2 = "account.kms.token.switch.v2";
    private static final String ACCOUNT_KMS_TOKEN_MHA_GRAY = "account.kms.token.mha.gray";
    private static final String ACCOUNT_KMS_TOKEN_MHA_GRAY_V2 = "account.kms.token.mha.gray.v2";
    private static final String KMS_URL_PREFIX = "kms.url.";
    private static final String KMS_ACCESS_TOKEN_PREFIX = "kms.access.token.";

    public static String HERALD_TOKEN_REQUEST_SWITCH = "herald.token.request.switch";
    private static final String ACCOUNT_FROM_METE_SWITCH = "account.from.meta.switch";
    private static final String DBA_API_PWD_CHANGE_URL = "dba.api.pwd.change.url";


    private static final String SGP_MESSENGER_GTID_INIT = "sgp.messenger.gtid.init";
    private static final String SGP_MESSENGER_GTID_INIT_KEY = SGP_MESSENGER_GTID_INIT + ".%s";
    private static final String BATCH_OFFLINE_ALLOW_REGION = "meta.batch.offline.allow.region";
    private static String DEFAULT_BATCH_OFFLINE_ALLOW_REGION = "sin";
    private static final String ACCOUNT_REALTIME_SWITCH = "account.realtime.switch";


    // only for test
    protected DefaultConsoleConfig(Config config) {
        super(config);
    }

    public DefaultConsoleConfig() {
    }

    public String getSgpMessengerGtidInit(String mha) {
        return getProperty(String.format(SGP_MESSENGER_GTID_INIT_KEY, mha), "");
    }

    public String getRegion(){
        return regionConfig.getRegion();
    }

    public Map<String,Set<String>> getRegion2dcsMapping(){
        return regionConfig.getRegion2dcsMapping();
    }

    public List<String> getCenterRegionDcs() {
        String idcStr = getProperty(DRC_CENTER_IDC, "");
        if (StringUtils.isBlank(idcStr)) {
            return new ArrayList<>();
        }
        return Lists.newArrayList(idcStr.split(","));
    }

    public Map<String,String> getDc2regionMap (){
        Map<String, Set<String>> regionsInfo = getRegion2dcsMapping();
        Map<String,String> dc2regionMap = Maps.newHashMap();
        regionsInfo.forEach(
                (region, dcs) -> dcs.forEach(dc -> dc2regionMap.put(dc, region))
        );
        return dc2regionMap;
    }

    public Set<String> getDcsInSameRegion(String dc) {
        Set<String> dcs = Sets.newHashSet();
        Map<String, Set<String>> regionsInfo = getRegion2dcsMapping();
        regionsInfo.forEach(
                (region,dcsInRegion) ->{
                    if (dcsInRegion.contains(dc)) {
                        dcs.addAll(dcsInRegion);
                    }
                }
        );
        return dcs;
    }

    public Set<String> getDcsInLocalRegion() {
        String region = getRegion();
        Map<String, Set<String>> regionsInfo = getRegion2dcsMapping();
        return  regionsInfo.get(region);
    }
    
    public boolean isCenterRegion() {
        return getCenterRegion().equals(getRegion());
    }
    
    public String getCenterRegionUrl() {
        Map<String, String> consoleRegionUrls = getConsoleRegionUrls();
        return consoleRegionUrls.get(getCenterRegion());
    }
    
    public String getCenterRegion() {
        return getProperty(CENTER_REGION,DEFAULT_CENTER_REGION);
    }
    
    public String getRegionForDc(String dcName) {
        Map<String, Set<String>> region2dcsMapping = getRegion2dcsMapping();
        for (Map.Entry<String, Set<String>> entry : region2dcsMapping.entrySet()) {
            if (entry.getValue().contains(dcName.toLowerCase())) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("can not find region with dcName: " + dcName);
    }
    
    public int getConflictMhaRecordSearchTime() {
        return getIntProperty(CONFLICT_RECORD_SEARCH_TIME,DEFAULT_CONFLICT_RECORD_SEARCH_TIME);
    }

    public long getDelayExceptionTime() {
        String timeStr = getProperty(DELAY_EXCEPTION_TIME, DEFAULT_DELAY_EXCEPTION_TIME);
        return Long.parseLong(timeStr);
    }

    public long getDelayExceptionTimeInMilliseconds() {
        String timeStr = getProperty(DELAY_UPDATE_TIMEOUT_TIME, "5000");
        return Long.parseLong(timeStr);
    }

    public Map<String,String> getCMRegionUrls() {
        return regionConfig.getCMRegionUrls();
    }

    public Map<String,String> getConsoleRegionUrls() {
        return regionConfig.getConsoleRegionUrls();
    }

    public Map<String, DcInfo> getDcInfos() {
        String dcInfoStr = getProperty(KEY_DC_INFOS, defaultDcInfos);
        logger.info("[[monitor=delay]] {}={}", KEY_DC_INFOS, dcInfoStr);
        Map<String, DcInfo> dcInfos = JsonCodec.INSTANCE.decode(dcInfoStr, new GenericTypeReference<Map<String, DcInfo>>() {});

        Map<String, DcInfo> result = Maps.newConcurrentMap();
        for(Map.Entry<String, DcInfo> entry : dcInfos.entrySet()){
            result.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        logger.debug("[getDcInofs]{}", result);
        return result;
    }

    public Map<String, String> getDbaDcInfos() {
        if(dbaDcInfos.size() == 0) {
            dbaDcInfos = getDbaDcInfoMapping();
        }
        return dbaDcInfos;
    }

    public Map<String, String> getConsoleDcInfos() {
        if(consoleDcInfos.size() == 0) {
            consoleDcInfos = getConsoleDcInfoMapping();
        }
        return consoleDcInfos;
    }
    

    public Map<String, String> getConsoleDcEndpointInfos() {
        if(consoleDcEndpointInfos.size() == 0) {
            consoleDcEndpointInfos = getConsoleDcEndpointMapping();
        }
        return consoleDcEndpointInfos;
    }

    public void setDefaultDcInfos(String defaultDcInfos) {
        this.defaultDcInfos = defaultDcInfos;
    }

    // for test turn on switch
    @VisibleForTesting
    protected void setSwitchCmRegionUrl(String switchCmRegionUrl) {
        DEFAULT_SWITCH_CM_REGION_URL = switchCmRegionUrl;
    }

    public void setDefaultDbaDcInfos(String defaultDbaDcInfos) {
        this.defaultDbaDcInfos = defaultDbaDcInfos;
    }

    public void setDefaultConsoleDcInfos(String defaultConsoleDcInfos) {
        this.defaultConsoleDcInfos = defaultConsoleDcInfos;
    }

    public void setDefaultBeaconPrefix(String defaultBeaconPrefix) {
        this.defaultBeaconPrefix = defaultBeaconPrefix;
    }

    public String getBeaconPrefix() {
        return getProperty(BEACON_PREFIX, defaultBeaconPrefix);
    }

    public void setDefaultMhaDalClusterInfos(String defaultMhaDalClusterInfos) {
        this.defaultMhaDalClusterInfos = defaultMhaDalClusterInfos;
    }

    public String getCMMetaServerAddress(String dc) {
        if (SWITCH_ON.equalsIgnoreCase(getSwitchCmRegionUrl())) {
            Map<String, String> dc2regionMap = getDc2regionMap();
            String region = dc2regionMap.get(dc);
            Map<String, String> cmRegionUrls = getCMRegionUrls();
            String cmMetaServerAddress = cmRegionUrls.get(region);
            if (StringUtils.isEmpty(cmMetaServerAddress)) {
                logger.warn("[getCMMetaServerAddress] not configured for dc:{},region:{}", dc,region);
            }
            return cmMetaServerAddress;
        } else {
            Map<String, DcInfo> dcInfos = getDcInfos();
            DcInfo dcInfo = dcInfos.get(dc);
            if (dcInfo != null) {
                return dcInfo.getMetaServerAddress();
            }
            logger.warn("[getCMMetaServerAddress] not configured for {}", dc);
            return null;
        }
    }

    private Map<String, String> getDbaDcInfoMapping() {

        String dcInfoStr = getProperty(DBA_DC_INFOS, defaultDbaDcInfos);
        logger.info("DBA dcInfos {}={}", DBA_DC_INFOS, dcInfoStr);
        Map<String, String> dbaDcInfos = JsonCodec.INSTANCE.decode(dcInfoStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> result = Maps.newConcurrentMap();
        for(Map.Entry<String, String> entry : dbaDcInfos.entrySet()){
            result.put(entry.getKey(), entry.getValue().toLowerCase());
        }

        logger.debug("[getDbaDcInofs]{}", result);
        return result;
    }

    public String getValidationDomain(String dc) {
        Map<String, String> validationDcInfos = getValidationDcInfoMapping();
        return validationDcInfos.get(dc);
    }

    private Map<String, String> getValidationDcInfoMapping() {
        String dcInfoStr = getProperty(VALIDATION_DC_INFOS, defaultValidationDcInfos);
        logger.info("validation dcInfos {}={}", VALIDATION_DC_INFOS, dcInfoStr);
        Map<String, String> validationDcInfos = JsonCodec.INSTANCE.decode(dcInfoStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> result = Maps.newConcurrentMap();
        for(Map.Entry<String, String> entry : validationDcInfos.entrySet()){
            result.put(entry.getKey(), entry.getValue().toLowerCase());
        }

        logger.debug("[getValidationDcInfoMapping]{}", result);
        return result;
    }

    private Map<String, String> getConsoleDcInfoMapping() {
        String dcInfoStr = getProperty(CONSOLE_DC_INFOS, defaultConsoleDcInfos);
        logger.info("console dcInfos {}={}", CONSOLE_DC_INFOS, dcInfoStr);
        Map<String, String> consoleDcInfos = JsonCodec.INSTANCE.decode(dcInfoStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> result = Maps.newConcurrentMap();
        for(Map.Entry<String, String> entry : consoleDcInfos.entrySet()){
            result.put(entry.getKey(), entry.getValue().toLowerCase());
        }

        logger.debug("[getConsoleDcInofs]{}", result);
        return result;
    }

    private Map<String, String> getConsoleDcEndpointMapping() {
        String dcEndpointInfoStr = getProperty(CONSOLE_DC_ENDPOINT_INFOS, defaultConsoleDcInfos);
        logger.info("console dcInfos {}={}", CONSOLE_DC_ENDPOINT_INFOS, dcEndpointInfoStr);
        Map<String, String> consoleDcInfos = JsonCodec.INSTANCE.decode(dcEndpointInfoStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> result = Maps.newConcurrentMap();
        for(Map.Entry<String, String> entry : consoleDcInfos.entrySet()){
            result.put(entry.getKey(), entry.getValue().toLowerCase());
        }

        logger.debug("[getConsoleDcInfos]{}", result);
        return result;
    }

    public Map<String, Integer> getUcsStrategyIdMap(String cluster) {
        String key = String.format(UCS_STRATEGY_PREFIX, cluster);
        String ucsStrategyIdMappingStr = getProperty(key, DEFAULT_MAPPING_INFOS);
        logger.info("ucs strategy mapping for {}: {}", cluster, ucsStrategyIdMappingStr);
        Map<String, Integer> ucsStrategyInfos = JsonCodec.INSTANCE.decode(ucsStrategyIdMappingStr, new GenericTypeReference<Map<String, Integer>>() {});

        Map<String, Integer> ucsStrategyIdMap = Maps.newHashMap();
        for(Map.Entry<String, Integer> entry : ucsStrategyInfos.entrySet()){
            ucsStrategyIdMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        logger.debug("[uidNameMap]: {}", ucsStrategyIdMap);
        return ucsStrategyIdMap;
    }

    public Map<String, String> getUidMap(String cluster) {
        String key = String.format(UID_DATA_PREFIX, cluster);
        String uidNameMappingStr = getProperty(key, DEFAULT_MAPPING_INFOS);
        logger.info("uid name mapping for {}: {}", cluster, uidNameMappingStr);
        Map<String, String> uidNameInfos = JsonCodec.INSTANCE.decode(uidNameMappingStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> uidNameMap = Maps.newHashMap();
        for(Map.Entry<String, String> entry : uidNameInfos.entrySet()){
            uidNameMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
        }

        logger.debug("[uidNameMap]: {}", uidNameMap);
        return uidNameMap;
    }

    public Map<String, String> getMhaDalClusterInfoMapping() {
        String mhaDalClusterInfoStr = getProperty(MHA_DAL_CLUSTER_INFOS, defaultMhaDalClusterInfos);
        logger.info("mha dal cluster info {}={}", MHA_DAL_CLUSTER_INFOS, mhaDalClusterInfoStr);
        Map<String, String> mhaDalClusterInfos = JsonCodec.INSTANCE.decode(mhaDalClusterInfoStr, new GenericTypeReference<Map<String, String>>() {});

        Map<String, String> result = Maps.newConcurrentMap();
        for(Map.Entry<String, String> entry : mhaDalClusterInfos.entrySet()){
            result.put(entry.getKey(), entry.getValue().toLowerCase());
        }

        logger.debug("[getMhaDalClusterInfos]{}", result);
        return result;
    }

    public Set<String> getPublicCloudDc() {
        String publicCloudDc = getProperty(PUBLIC_CLOUD_DC, DEFAULT_PUBLIC_CLOUD_DC);
        logger.info("public cloud dc: {}", publicCloudDc);
        return Sets.newHashSet(publicCloudDc.split(","));
    }
    
    public Set<String> getPublicCloudRegion() {
        String publicCloudRegion = getProperty(PUBLIC_CLOUD_REGION,DEFAULT_PUBLIC_CLOUD_REGION);
        logger.info("public cloud region: {}", publicCloudRegion);
        return Sets.newHashSet(publicCloudRegion.toLowerCase().split(","));
    }

    public Set<String> getLocalConfigCloudDc() {
        String localConfigCloudDc = getProperty(LOCAL_CONFIG_CLOUD_DC, DEFAULT_LOCAL_CONFIG_CLOUD_DC);
        logger.info("localConfigCloudDc: {}", localConfigCloudDc);
        return Sets.newHashSet(localConfigCloudDc.split(","));
    }

    public List<String> getLocalDcMhaNamesToBeMonitored() {
        String localDcMhaNamesToBeMonitored = getProperty(LOCAL_CONFIG_MONITOR_MHAS, DEFAULT_LOCAL_CONFIG_MONITOR_MHAS);
        logger.info("localDcMhaNamesToBeMonitored: {}", localDcMhaNamesToBeMonitored);
        if (null == localDcMhaNamesToBeMonitored || StringUtils.isBlank(localDcMhaNamesToBeMonitored)) {
            return null;
        }
        return Lists.newArrayList(localDcMhaNamesToBeMonitored.split(","));
    }

    public Map<String, Long> getLocalConfigMhasMap() {
        HashMap<String, Long> mhasIdNameMap = Maps.newHashMap();
        String mhaNameIdMapString = getProperty(LOCAL_CONFIG_MHAS_MAP, DEFAULT_LOCAL_CONFIG_MHAS_MAP);
        logger.info("mhaNameIdMapString: {}", mhaNameIdMapString);
        Map<String, String> decode = JsonCodec.INSTANCE.decode(mhaNameIdMapString, new GenericTypeReference<Map<String, String>>() {});
        for (Map.Entry<String,String> entry : decode.entrySet()) {
            mhasIdNameMap.put(entry.getKey(),Long.valueOf(entry.getValue()));
        }
        return mhasIdNameMap;

    }

    public Map<String, String> getDbaDc2DrcDcMap() {
        String dbaDc2DrcDcMapString = getProperty(DBA_DC_2_DRC_DC_MAP, DEFAULT_DBA_DC_2_DRC_DC_MAP);
        logger.info("dbaDc2DrcDcMapString: {}", dbaDc2DrcDcMapString);
        return JsonCodec.INSTANCE.decode(dbaDc2DrcDcMapString, new GenericTypeReference<Map<String, String>>() {});
    }

    public List<String> getVpcMhaNames() {
        String vpcMhaStr = getProperty(VPC_MHA);
        if (StringUtils.isBlank(vpcMhaStr)) {
            return new ArrayList<>();
        }
        return Lists.newArrayList(vpcMhaStr.split(","));
    }

    public String getSwitchCmRegionUrl() {
        return getProperty(SWITCH_CM_REGION_URL,DEFAULT_SWITCH_CM_REGION_URL);
    }

    public String getSwitchMetaRollBack() {
        return getProperty(SWITCH_META_ROLL_BACK,SWITCH_OFF);
    }

    public String getUpdateReplicatorSwitch() {
        return getProperty(UPDATE_REPLICATOR_MASTER_SWITCH,SWITCH_OFF);
    }

    public int getAvailablePortSize() {
        return getIntProperty(AVAILABLE_PORT_SIZE,DEFAULT_AVAILABLE_PORT_SIZE);
    }

    public int getMetaCompareParallel() {
        return getIntProperty(META_COMPARE_PARALLEL,DEFAULT_META_COMPARE_PARALLEL);
    }

    public boolean getCostTimeTraceSwitch() {
        return getBooleanProperty(COST_TIME_TRACE_SWITCH,false);
    }

    public String getDrcDoubleWriteSwitch() {
        return getProperty(DRC_DOUBLE_WRITE_SWITCH, SWITCH_OFF);
    }

    public String getNewDrcConfigSwitch() {
        return getProperty(NEW_DRC_CONFIG_SWITCH, SWITCH_OFF);
    }

    public String getMetaCompareSwitch() {
        return getProperty(META_COMPARE_SWITCH, SWITCH_ON);
    }

    public boolean getMetaGeneratorV5Switch() {
        return getBooleanProperty(META_GENERATOR_V5_SWITCH, false);
    }

    public boolean getDbApplierConfigureSwitch(String mha) {
        String value = getProperty(String.format(META_DB_APPLIER_CONFIG_SWITCH_KEY, mha));
        if (StringUtils.isBlank(value)) {
            return getBooleanProperty(META_DB_APPLIER_CONFIG_SWITCH, false);
        }
        return Boolean.parseBoolean(value);
    }

    public String getMetaRealtimeSwitch() {
        return getProperty(META_REALTIME_SWITCH, SWITCH_OFF);
    }

    public boolean getConfgiCheckSwitch() {
        return getBooleanProperty("config.check.switch", true);
    }

    public boolean getConflictDbOwnerApprovalSwitch() {
        return getBooleanProperty(CONFLICT_DB_OWNER_APPROVAL_SWITCH, false);
    }

    public long getConflictLogQueryTimeInterval() {
        return getLongProperty(CONFLICT_LOG_QUERY_TIME_INTERVAL, Constants.ONE_DAY);
    }

    public String getDrcAccessTokenKey() {
        return getProperty(DRC_ACCESS_TOKEN_KEY, "");
    }

    public boolean getOperationLogSwitch() {
        return getBooleanProperty(OPERATION_LOG_SWITCH,false);
    }

    public long getReplicatorMaxSize() {
        return getLongProperty(REPLICATOR_MAX_SIZE, DEFAULT_REPLICATOR_SIZE);
    }
    
    public boolean getCflBlackListAutoAddSwitch() {
        return getBooleanProperty(CFL_BLACK_LIST_AUTO_ADD_SWITCH, false);
    }

    public boolean getTableStructureCheckSwitch() {
        return getBooleanProperty(TABLE_STRUCTURE_CHECK_SWITCH, true);
    }

    public boolean getConflictLogRecordSwitch() {
        return getBooleanProperty(CONFLICT_LOG_RECORD_SWITCH, true);
    }

    public boolean getDrcConfigApplicationSwitch() {
        return getBooleanProperty(DRC_CONFIG_APPLICATION_SWITCH, true);
    }

    public boolean getDbReplicationConsistencyCheckSwitch() {
        return getBooleanProperty(DB_REPLICATION_CONSISTENCY_CHECK_SWITCH, true);
    }
    
    public String getDefaultReadAccountKmsToken() {
        return getProperty(DEFAULT_READ_ACCOUNT_KMS_TOKEN);
    }
    
    public String getDefaultWriteAccountKmsToken() {
        return getProperty(DEFAULT_WRITE_ACCOUNT_KMS_TOKEN);
    }
    
    public String getDefaultMonitorAccountKmsToken() {
        return getProperty(DEFAULT_MONITOR_ACCOUNT_KMS_TOKEN);
    }
    
    public String getKMSAccessToken(String suffix) {
        return getProperty(KMS_ACCESS_TOKEN_PREFIX + suffix,null);
    }
    
    public boolean getAccountKmsTokenSwitch() {
        return getBooleanProperty(ACCOUNT_KMS_TOKEN_SWITCH, false);
    }
    
    public Set<String> getAccountKmsTokenMhaGray() {
        String property = getProperty(ACCOUNT_KMS_TOKEN_MHA_GRAY, "");
        if (StringUtils.isBlank(property)) {
            return Collections.emptySet();
        }
        return Sets.newHashSet(property.split(","));
    }

    public Set<String> getAccountKmsTokenMhaGrayV2() {
        String property = getProperty(ACCOUNT_KMS_TOKEN_MHA_GRAY_V2, "");
        if (StringUtils.isBlank(property)) {
            return Collections.emptySet();
        }
        return Sets.newHashSet(property.split(","));
    }

    public boolean getAccountKmsTokenSwitchV2() {
        return getBooleanProperty(ACCOUNT_KMS_TOKEN_SWITCH_V2, false);
    }

    public List<String> getBatchOfflineRegion() {
        String idcStr = getProperty(BATCH_OFFLINE_ALLOW_REGION, DEFAULT_BATCH_OFFLINE_ALLOW_REGION);
        if (StringUtils.isBlank(idcStr)) {
            return new ArrayList<>();
        }
        return Lists.newArrayList(idcStr.split(","));
    }

    public String getKmsUrl(String envStr) {
        return getProperty(KMS_URL_PREFIX + envStr, "");
    }

    public boolean requestWithHeraldToken() {
        return getBooleanProperty(HERALD_TOKEN_REQUEST_SWITCH, false);
    }

    public String getDbaApiPwdChangeUrl() {
        return getProperty(DBA_API_PWD_CHANGE_URL, "");
    }

    public boolean getAccountFromMetaSwitch() {
        return getBooleanProperty(ACCOUNT_FROM_METE_SWITCH, false);
    }

    public boolean getAccountRealTimeSwitch() {
        return getBooleanProperty(ACCOUNT_REALTIME_SWITCH, false);
    }
}
