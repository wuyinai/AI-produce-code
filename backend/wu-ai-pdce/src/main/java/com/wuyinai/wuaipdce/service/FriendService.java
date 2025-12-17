package com.wuyinai.wuaipdce.service;

import com.mybatisflex.core.service.IService;
import com.wuyinai.wuaipdce.model.dto.friend.FriendRequestDTO;
import com.wuyinai.wuaipdce.model.entity.Friend;
import com.wuyinai.wuaipdce.model.vo.FriendRequestVO;
import com.wuyinai.wuaipdce.model.vo.FriendVO;

import java.util.List;

/**
 * 好友关系服务
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface FriendService extends IService<Friend> {

    /**
     * 发送好友请求
     *
     * @param userId       当前用户ID
     * @param requestDTO   好友请求DTO
     * @return             是否发送成功
     */
    boolean sendFriendRequest(Long userId, FriendRequestDTO requestDTO);

    /**
     * 接受好友请求
     *
     * @param userId      当前用户ID
     * @param requestId   请求ID
     * @return            是否接受成功
     */
    boolean acceptFriendRequest(Long userId, Long requestId);

    /**
     * 拒绝好友请求
     *
     * @param userId      当前用户ID
     * @param requestId   请求ID
     * @return            是否拒绝成功
     */
    boolean rejectFriendRequest(Long userId, Long requestId);

    /**
     * 获取好友列表
     *
     * @param userId   当前用户ID
     * @return         好友列表
     */
    List<FriendVO> getFriendList(Long userId);

    /**
     * 删除好友
     *
     * @param userId     当前用户ID
     * @param friendId   好友ID
     * @return           是否删除成功
     */
    boolean deleteFriend(Long userId, Long friendId);

    /**
     * 获取好友详情
     *
     * @param userId     当前用户ID
     * @param friendId   好友ID
     * @return           好友详情
     */
    FriendVO getFriendDetail(Long userId, Long friendId);

    /**
     * 获取收到的好友请求列表
     *
     * @param userId   当前用户ID
     * @return         好友请求列表
     */
    List<FriendRequestVO> getReceivedFriendRequests(Long userId);

    /**
     * 获取发送的好友请求列表
     *
     * @param userId   当前用户ID
     * @return         好友请求列表
     */
    List<FriendRequestVO> getSentFriendRequests(Long userId);

    /**
     * 搜索用户
     *
     * @param keyword   搜索关键词
     * @param userId    当前用户ID
     * @return          用户列表
     */
    List<FriendVO> searchUsers(String keyword, Long userId);
}
