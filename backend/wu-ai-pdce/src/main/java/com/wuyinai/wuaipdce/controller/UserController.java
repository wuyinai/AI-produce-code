package com.wuyinai.wuaipdce.controller;

import com.mybatisflex.core.paginate.Page;
import com.wuyinai.wuaipdce.common.BaseResponse;
import com.wuyinai.wuaipdce.common.ResultUtils;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.model.dto.user.UserLoginRequest;
import com.wuyinai.wuaipdce.model.dto.user.UserRegisterRequest;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.LoginUserVO;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册
     *
     * @param userRegisterRequest 注册参数
     */
    @PostMapping("Register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        //工具类 ThrowUtils 检查参数是否为空
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 登录
     *
     *@param userLoginRequest 登录参数
     */
    @PostMapping("Login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request)  {
        //工具类 ThrowUtils 检查参数是否为空
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO userVO = userService.userLogin(userAccount, userPassword,request);
        return ResultUtils.success(userVO);
    }
    /**
     * 获取当前登录用户
     *
     * @param request 请求参数
     */
    @GetMapping("list")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(loginUser);
    }

    /**
     * 退出登录
     *
     * @param request 退出登录参数
     */
    @PostMapping("logout")
    public BaseResponse<?> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
}