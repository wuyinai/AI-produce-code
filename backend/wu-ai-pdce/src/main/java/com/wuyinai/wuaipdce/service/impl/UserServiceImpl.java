package com.wuyinai.wuaipdce.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.wuyinai.wuaipdce.common.DeleteRequest;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.mapper.UserMapper;
import com.wuyinai.wuaipdce.model.dto.user.UserAddRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserQueryRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserUpdateRequest;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.enums.UserRoleEnum;
import com.wuyinai.wuaipdce.model.vo.LoginUserVO;
import com.wuyinai.wuaipdce.model.vo.UserVO;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wuyinai.wuaipdce.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //判断参数是否为空
        if (ObjUtil.isAllEmpty(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //判断账号长度
        if (userAccount.length() < 6 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "账号长度在6~16位之间");
        }
        //判断密码长度
        if (userPassword.length() < 6 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码长度在6~16位之间");
        }
        //判断密码和确认密码是否一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码和确认密码不一致");
        }
        //账号查重
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "账号已存在");
        }
        //加密
        String password = getEncryptPassword(userPassword);
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(password);
        user.setUserName("无名氏");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        //判断插入成功
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        //返回用户id
        return user.getId();
    }

    /**
     * 获取加密密码
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        //盐值，混淆密码
        final String SALT = "yinai";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 进行用户脱敏
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 返回脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //判断参数是否为空
        if (ObjUtil.isAllEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //账号长度
        if (userAccount.length() < 6 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "账号错误");
        }
        //密码长度
        if (userPassword.length() < 6 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码错误");
        }
        System.out.println(request);
        //加密
        String password = getEncryptPassword(userPassword);
        //判断用户密码在数据库中是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount).eq("userPassword", password);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在或密码错误");
        }
        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //返回脱敏后的数据
        return this.getLoginUserVO(user);
    }
    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) user;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 将用户信息（脱敏）
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if(user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 将用户列表信息（脱敏）
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        //第一种方案
//        if (userList == null) {
//            return new ArrayList<>();
//        }
//        List<UserVO> userVOList = new ArrayList<>();
//        for (User user : userList) {
//            UserVO userVO = new UserVO();
//            BeanUtils.copyProperties(user, userVO);
//            userVOList.add(userVO);
//        }
//        return userVOList;
//    }
        //第二种方案
        if(CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        //使用 stream() 将用户列表转为流。
        //通过 map(this::getUserVO) 调用 getUserVO 方法对每个 User 进行转换。
        //使用 collect(Collectors.toList()) 将转换后的结果收集为列表返回。
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();//排序的字段
        String sortOrder = userQueryRequest.getSortOrder();//排序的顺序
        //通过 MyBatis-Plus 的 QueryWrapper 链式调用添加查询条件：
        return QueryWrapper.create()
                .eq("id",id)//eq添加等于条件
                .eq("userRole",userRole)
                .like("userName",userName)//like添加模糊匹配条件
                .like("userAccount",userAccount)
                .like("userProfile",userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));//排序
    }

    /**
     * 创建用户 （仅管理员）
     * @param userAddRequest
     * @return user Id
     */
    @Override
    public long addUser(UserAddRequest userAddRequest) {
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        //设置默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

     /**
      * 根据id获取用户（仅管理员）
      * @param id
      * @return
      */
    @Override
    public User getUserById(Long id) {
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    /**
     * 根据id获取用户VO（仅管理员）
     * @param id
     * @return
     */
    @Override
    public UserVO getUserVOById(Long id) {
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return getUserVO(user);
    }

    /**
     * 删除用户（仅管理员）
     * @param deleteRequest
     * @return  boolean
     */
    @Override
    public boolean deleteUser(DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(deleteRequest.getId());
    }

    /**
     * 更新用户（仅管理员）
     * @param userUpdateRequest
     * @return  boolean
     */
    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 获取用户列表（仅管理员）
     * @param userQueryRequest
     * @return  List<UserVO>
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = page(Page.of(pageNum, pageSize), getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize,userPage.getTotalRow());
        List<UserVO> userVOList = getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }


}
