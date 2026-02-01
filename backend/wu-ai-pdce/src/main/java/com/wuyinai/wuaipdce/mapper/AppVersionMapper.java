package com.wuyinai.wuaipdce.mapper;

import com.mybatisflex.core.BaseMapper;
import com.wuyinai.wuaipdce.model.entity.AppVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppVersionMapper extends BaseMapper<AppVersion> {

    Integer getNextVersionNumber(@Param("appId") Long appId);

    List<AppVersion> listVersionsByAppId(@Param("appId") Long appId);

    AppVersion getCurrentVersionByAppId(@Param("appId") Long appId);

    void setAllVersionsNotCurrent(@Param("appId") Long appId);
}
