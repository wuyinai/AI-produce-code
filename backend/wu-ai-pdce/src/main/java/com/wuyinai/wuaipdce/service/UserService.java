package com.wuyinai.wuaipdce.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wuyinai.wuaipdce.common.DeleteRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserAddRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserQueryRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserUpdateRequest;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.LoginUserVO;
import com.wuyinai.wuaipdce.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的登录用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return 脱敏后的用户数据·
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 从request来获取用户信息
     * @param request
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     * @param request
     *
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的userVO
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的userVO列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 创建用户 （仅管理员）
     * @param userAddRequest
     * @return user Id
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 根据id获取用户（仅管理员）
     * @param id
     * @return
     */
    User getUserById(Long id);

    /**
     * 根据id获取用户信息（脱敏）
     * @param id
     * @return UserVO
     */
    UserVO getUserVOById(Long id);

    /**
     * 删除用户（仅管理员）
     * @param deleteRequest
     * @return  boolean
     */
    boolean deleteUser(DeleteRequest deleteRequest);

    /**
     * 更新用户（仅管理员）
     * @param userUpdateRequest
     * @return boolean
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 分页获取用户封装列表（仅管理员）
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    /**
     * 获取当前用户应用被精选和聊天次数
     * @param request
     * @return
     */
    Object getAppChatNumber(HttpServletRequest request);
}

