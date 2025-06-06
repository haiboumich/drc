package com.ctrip.framework.drc.manager.ha.meta.impl;

import com.ctrip.framework.drc.core.config.DynamicConfig;
import com.ctrip.framework.drc.core.entity.Applier;
import com.ctrip.framework.drc.core.entity.DbCluster;
import com.ctrip.framework.drc.core.entity.Dc;
import com.ctrip.framework.drc.core.entity.IRoute;
import com.ctrip.framework.drc.core.entity.Route;
import com.ctrip.framework.drc.core.meta.comparator.DcRouteComparator;
import com.ctrip.framework.drc.core.meta.comparator.MetaComparator;
import com.ctrip.framework.drc.core.server.config.RegistryKey;
import com.ctrip.framework.drc.core.server.utils.ThreadUtils;
import com.ctrip.framework.drc.manager.config.SourceProvider;
import com.ctrip.framework.drc.manager.ha.config.ClusterManagerConfig;
import com.ctrip.framework.drc.manager.ha.meta.DcCache;
import com.ctrip.framework.drc.manager.ha.meta.DcManager;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ApplierComparator;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ClusterComparator;
import com.ctrip.framework.drc.manager.ha.meta.comparator.DcComparator;
import com.ctrip.framework.drc.manager.ha.meta.comparator.ReplicatorComparator;
import com.ctrip.xpipe.api.lifecycle.Ordered;
import com.ctrip.xpipe.api.monitor.EventMonitor;
import com.ctrip.xpipe.observer.AbstractLifecycleObservable;
import com.ctrip.xpipe.utils.StringUtil;
import com.ctrip.xpipe.utils.VisibleForTesting;
import com.ctrip.xpipe.utils.XpipeThreadFactory;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.ResourceAccessException;

/**
 * @Author limingdong
 * @create 2020/4/21
 */
public class DefaultDcCache extends AbstractLifecycleObservable implements DcCache, Runnable {

    public static final String META_MODIFY_JUST_NOW_TEMPLATE = "current dc meta modifyTime {} larger than meta loadTime {}";

    public static String MEMORY_META_SERVER_DAO_KEY = "memory_meta_server_dao_file";

    public static double META_REMOVE_THRESHOLD = 0.5;

    public static final String META_CHANGE_TYPE = "MetaChange";

    private SourceProvider sourceProvider;

    private ClusterManagerConfig config;

    private String currentDc;

    private ScheduledExecutorService scheduled;

    private ScheduledFuture<?> future;

    private AtomicReference<DcManager> dcMetaManager = new AtomicReference<DcManager>(null);

    private AtomicLong metaModifyTime = new AtomicLong(System.currentTimeMillis());

    private long lastRefreshTime;

    public DefaultDcCache(ClusterManagerConfig config, SourceProvider sourceProvider, String idc) {
        this.config = config;
        this.sourceProvider = sourceProvider;
        this.currentDc = idc;
        scheduled = Executors.newScheduledThreadPool(1, XpipeThreadFactory.create(ThreadUtils.getThreadName("Meta-Refresher", currentDc)));
        logger.info("[doInitialize][dc]{}", currentDc);
    }

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        this.dcMetaManager.set(loadMetaManager());
    }

    protected DcManager loadMetaManager() {

        DcManager dcMetaManager = null;
        if (sourceProvider != null) {
            try {
                logger.info("[loadMetaManager][load from console]");
                Dc dcMeta = sourceProvider.getDc(currentDc);
                dcMetaManager = DefaultDcManager.buildFromDcMeta(dcMeta);
            } catch (ResourceAccessException e) {
                logger.error("[loadMetaManager][consoleService]" + e.getMessage());
            } catch (Exception e) {
                logger.error("[loadMetaManager][consoleService]", e);
            }
        }

        if (dcMetaManager == null) {
            String fileName = System.getProperty(MEMORY_META_SERVER_DAO_KEY, "memory_meta_server_dao_file.xml");
            logger.info("[loadMetaManager][load from file]{}", fileName);
            dcMetaManager = DefaultDcManager.buildFromFile(currentDc, fileName);
        }

        logger.info("[loadMetaManager]{}", dcMetaManager);

        if (dcMetaManager == null) {
            throw new IllegalArgumentException("[loadMetaManager][fail]");
        }
        return dcMetaManager;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        future = scheduled.scheduleAtFixedRate(this, 0, config.getClusterRefreshMilli(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void refresh(String clusterId) {
        if (getCluster(clusterId) != null) {
            logger.info("refresh for: {}", clusterId);
            scheduled.schedule(() -> {
                refresh();
                lastRefreshTime = System.currentTimeMillis();
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void doStop() throws Exception {

        future.cancel(true);
        super.doStop();
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        long refreshGapTime = (currentTime - lastRefreshTime) / 1000;
        if (refreshGapTime > config.getClusterRefreshMilli() / 1000) {
            refresh();
        } else {
            logger.info("[refresh] skip for gap time : {}s, less than {}s", refreshGapTime, config.getClusterRefreshMilli() / 1000);
        }
    }

    private void refresh() {
        try {
            if (sourceProvider != null) {
                long metaLoadTime = System.currentTimeMillis();
                Dc future = sourceProvider.getDc(currentDc);
                Dc current = dcMetaManager.get().getDc();

                changeDcMeta(current, future, metaLoadTime);
                checkRouteChange(current, future);
            }
        } catch (Throwable th) {
            logger.error("[run]" + th.getMessage());
        }
    }

    @VisibleForTesting
    protected void changeDcMeta(Dc current, Dc future, final long metaLoadTime) {

        if (metaLoadTime <= metaModifyTime.get()) {
            logger.warn("[run][skip change dc meta]" + META_MODIFY_JUST_NOW_TEMPLATE, metaModifyTime.get(), metaLoadTime);
            return;
        }

        if(null == future) {
            logger.warn("[run][null new config]");
            return;
        }

        DcComparator dcMetaComparator = new DcComparator(current, future);
        dcMetaComparator.compare();

        logger.info("changed here: {}", dcMetaComparator);

        
        if(dcMetaComparator.getRemoved().size()/(current.getDbClusters().size()*1.0) > META_REMOVE_THRESHOLD) {
            logger.warn("[run][remove too many dbclusters]{}, {}, {}", META_REMOVE_THRESHOLD, dcMetaComparator.getRemoved().size(), dcMetaComparator);
            EventMonitor.DEFAULT.logAlertEvent("remove too many:" + dcMetaComparator.getRemoved().size());
            return;
        }

        DcManager newDcMetaManager = DefaultDcManager.buildFromDcMeta(future);
        boolean dcMetaUpdated = false;
        synchronized (this) {
            if (metaLoadTime > metaModifyTime.get()) {
                dcMetaManager.set(newDcMetaManager);
                dcMetaUpdated = true;
            }
        }

        if (!dcMetaUpdated) {
            logger.info("[run][skip change dc meta]" + META_MODIFY_JUST_NOW_TEMPLATE, metaModifyTime, metaLoadTime);
            return;
        }

        logger.info("[run][change dc meta]");
        if (dcMetaComparator.totalChangedCount() > 0) {
            logger.info("[run][change]{}", dcMetaComparator);
            EventMonitor.DEFAULT.logEvent(META_CHANGE_TYPE, String.format("[add:%s, del:%s, mod:%s]",
                    StringUtil.join(",", (clusterMeta) -> clusterMeta.getId(), dcMetaComparator.getAdded()),
                    StringUtil.join(",", (clusterMeta) -> clusterMeta.getId(), dcMetaComparator.getRemoved()),
                    StringUtil.join(",", (comparator) -> comparator.idDesc(), dcMetaComparator.getMofified()))
            );
            notifyObservers(dcMetaComparator);
        }
    }

    private int drClusterNums(DcComparator comparator) {
        int result = 0;
        for(MetaComparator metaComparator : comparator.getMofified()) {
            ClusterComparator clusterMetaComparator = (ClusterComparator) metaComparator;
            ReplicatorComparator replicatorComparator = clusterMetaComparator.getReplicatorComparator();
            int replicatorChangeCount = replicatorComparator.totalChangedCount();
            result += replicatorChangeCount;
            logger.info("[ReplicatorComparator] change count {}", replicatorChangeCount);

            ApplierComparator applierComparator = clusterMetaComparator.getApplierComparator();
            int applierChangeCount = applierComparator.totalChangedCount();
            result += applierChangeCount;
            logger.info("[ApplierComparator] change count {}", applierChangeCount);
        }
        logger.info("[DR Switched][cluster num] {}", result);
        return result;
    }

    @VisibleForTesting
    protected void checkRouteChange(Dc current, Dc future) {
        DcRouteComparator comparator = new DcRouteComparator(current, future, IRoute.TAG_META);
        comparator.compare();

        if(!comparator.getRemoved().isEmpty()
                || !comparator.getMofified().isEmpty()) {
            notifyObservers(comparator);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Set<String> getClusters() {
        return dcMetaManager.get().getClusters();
    }

    @Override
    public DbCluster getCluster(String clusterId) {
        return dcMetaManager.get().getCluster(clusterId);
    }

    @Override
    public Route randomRoute(String clusterId, String dstDc) {
        return dcMetaManager.get().randomRoute(clusterId, dstDc);
    }
    
    @Override
    public Route randomRoute(String clusterId,String dstDc,Integer orgId) {
        return dcMetaManager.get().randomRoute(clusterId, dstDc,orgId);
    }

    @Override
    public void clusterAdded(String dbCluster) {

        EventMonitor.DEFAULT.logEvent(META_CHANGE_TYPE, String.format("add:%s", dbCluster));

        clusterModified(dbCluster);
    }

    @Override
    public void clusterModified(String clusterId) {
        EventMonitor.DEFAULT.logEvent(META_CHANGE_TYPE, String.format("mod:%s", clusterId));

        refresh(clusterId);
    }

    @Override
    public void clusterDeleted(String registryKey) {

        EventMonitor.DEFAULT.logEvent(META_CHANGE_TYPE, String.format("del:%s", registryKey));

        refresh(registryKey);
    }

    @Override
    public Map<String, String> getBackupDcs(String clusterId) {
        Map<String, String> res = Maps.newHashMap();
        DbCluster dbCluster = getCluster(clusterId);
        List<Applier> applierList = dbCluster.getAppliers();
        for (Applier applier : applierList) {
            String targetMhaName = applier.getTargetMhaName();
            String targetName = applier.getTargetName();
            if (StringUtils.isNotBlank(targetMhaName)) {
                res.put(applier.getTargetIdc(), RegistryKey.from(targetName, targetMhaName));
            }
        }

        return res;
    }

    public String getCurrentDc() {
        return currentDc;
    }
}
