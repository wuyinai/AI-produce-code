package com.wuyinai.wuaipdce.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wuyinai.wuaipdce.common.DeleteRequest;
import com.wuyinai.wuaipdce.model.dto.app.AppAddRequest;
import com.wuyinai.wuaipdce.model.dto.app.AppAdminUpdateRequest;
import com.wuyinai.wuaipdce.model.dto.app.AppQueryRequest;
import com.wuyinai.wuaipdce.model.dto.app.AppUpdateRequest;
import com.wuyinai.wuaipdce.model.entity.App;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;


/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface AppService extends IService<App> {
    /**
     * 添加上脱敏用户信息
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);
    /**
     * 查询条件封装
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
    /**
     * 获取AppVO列表
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 增加应用
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 更新应用
     */
    void updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 删除应用
     */
    boolean deleteApp(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 根据 id 获取应用详情
     */
    AppVO getAppVOById(Long id);

    /**
     * 分页获取当前用户创建的应用列表
     */
    Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页获取精选应用列表
     *
     */
    Page<AppVO> listSelectedAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 管理员删除应用
     */
    boolean deleteAppByAdmin(DeleteRequest deleteRequest);
    /**
     * 管理员更新应用
     */
    void updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest);
    /**
     * 管理员分页获取应用列表
     */
    Page<AppVO> listAppVOByPageAdmin(AppQueryRequest appQueryRequest);

    /**
     * 管理员根据 id 获取应用详情
     */
    AppVO getAppVOByIdByAdmin(long id);

    /**
     * 调用门面类方法
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署服务
     */
    String deployApp(Long appId,User loginUser);
}
