package com.wuyinai.wuaipdce.mapper;

import com.mybatisflex.core.BaseMapper;
import com.wuyinai.wuaipdce.model.dto.user.UserAppChatNumberDTO;
import com.wuyinai.wuaipdce.model.entity.User;

/**
 * 用户 映射层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取用户应用，应用精选，聊天数量
     */
    UserAppChatNumberDTO getAppChatNumber(Long id);
}
