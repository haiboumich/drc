package com.ctrip.framework.drc.console.service.log;

import com.ctrip.framework.drc.console.enums.log.CflBlacklistType;
import com.ctrip.framework.drc.console.param.log.*;
import com.ctrip.framework.drc.console.vo.log.*;
import com.ctrip.framework.drc.core.server.common.filter.table.aviator.AviatorRegexFilter;
import com.ctrip.framework.drc.fetcher.conflict.ConflictTransactionLog;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by dengquanliang
 * 2023/9/26 15:09
 */
public interface ConflictLogService {

    List<ConflictTrxLogView> getConflictTrxLogView(ConflictTrxLogQueryParam param) throws Exception;

    List<ConflictRowsLogView> getConflictRowsLogView(ConflictRowsLogQueryParam param) throws Exception;

    int getRowsLogCount(ConflictRowsLogQueryParam param) throws Exception;

    int getTrxLogCount(ConflictTrxLogQueryParam param) throws Exception;

    List<ConflictRowsLogView> getConflictRowsLogView(List<Long> conflictRowLogIds) throws Exception;

    ConflictTrxLogDetailView getConflictTrxLogDetailView(Long conflictTrxLogId) throws Exception;

    ConflictCurrentRecordView getConflictCurrentRecordView(Long conflictTrxLogId, int columnSize) throws Exception;

    ConflictCurrentRecordView getConflictRowRecordView(Long conflictRowLogId, int columnSize) throws Exception;

    ConflictRowsRecordCompareView compareRowRecords(List<Long> conflictRowLogIds) throws Exception;

    void createConflictLog(List<ConflictTransactionLog> trxLogs) throws Exception;

    long deleteTrxLogs(long beginTime, long endTime) throws Exception;

    Map<String, Integer> deleteTrxLogsByTime(long beginTime, long endTime) throws Exception;

    Pair<String, String> checkSameMhaReplication(List<Long> conflictRowLogIds) throws Exception;

    ConflictTrxLogDetailView getRowLogDetailView(List<Long> conflictRowLogIds) throws Exception;

    ConflictCurrentRecordView getConflictRowRecordView(List<Long> conflictRowLogIds) throws Exception;

    List<ConflictAutoHandleView> createHandleSql(ConflictAutoHandleParam param) throws Exception;

    // addDbBlacklist ,refresh expire time when exist 
    void addDbBlacklist(String dbFilter, CflBlacklistType type, Long expirationTime) throws Exception;

    void updateDbBlacklist(ConflictDbBlacklistDto dto) throws Exception;

    void deleteBlacklist(String dbFilter) throws Exception;

    void deleteBlacklistForTouchJob(String dbFilter) throws Exception;

    List<ConflictDbBlacklistView> getConflictDbBlacklistView(ConflictDbBlacklistQueryParam param) throws Exception;

    boolean isInBlackListWithCache(String db, String table);

    List<ConflictRowRecordCompareEqualView> compareRowRecordsEqual(List<Long> conflictRowLogIds) throws Exception;

    ConflictRowsLogCountView getRowsLogCountView(long beginHandleTime, long endHandlerTime) throws Exception;

    List<AviatorRegexFilter> queryBlackList() throws SQLException;

}
