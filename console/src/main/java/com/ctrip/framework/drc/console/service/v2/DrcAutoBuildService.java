package com.ctrip.framework.drc.console.service.v2;

import com.ctrip.framework.drc.console.param.v2.DrcAutoBuildParam;
import com.ctrip.framework.drc.console.param.v2.DrcAutoBuildReq;
import com.ctrip.framework.drc.console.service.v2.external.dba.response.DbClusterInfoDto;
import com.ctrip.framework.drc.console.vo.check.TableCheckVo;
import com.ctrip.framework.drc.console.vo.display.v2.MhaPreCheckVo;
import com.ctrip.framework.drc.console.vo.display.v2.MhaReplicationPreviewDto;

import java.util.List;


public interface DrcAutoBuildService {
    
    void mhaInitBeforeBuild(DrcAutoBuildReq req) throws Exception;
    
    List<MhaReplicationPreviewDto> preCheckMhaReplication(DrcAutoBuildReq req);
    List<TableCheckVo> preCheckMysqlTables(DrcAutoBuildReq req);

    List<DrcAutoBuildParam> getDrcBuildParam(DrcAutoBuildReq req);

    List<String> getRegionOptions(DrcAutoBuildReq req);
    List<String> getCommonColumn(DrcAutoBuildReq req);

    List<MhaReplicationPreviewDto> getMhaReplicationPreviewDtos(DrcAutoBuildReq req, List<DbClusterInfoDto> databaseClusterInfoList);

    void autoBuildDrc(DrcAutoBuildReq req) ;

    void autoBuildDrcFromApplication(DrcAutoBuildReq req) throws Exception;

    List<String> getAllRegions() throws Exception;

    MhaPreCheckVo preCheckMysqlConfig(List<String> mhaList) throws Exception;
}
