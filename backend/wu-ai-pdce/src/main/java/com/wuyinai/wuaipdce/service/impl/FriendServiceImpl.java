package com.wuyinai.wuaipdce.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wuyinai.wuaipdce.mapper.FriendMapper;
import com.wuyinai.wuaipdce.model.dto.friend.FriendRequestDTO;
import com.wuyinai.wuaipdce.model.entity.Friend;
import com.wuyinai.wuaipdce.model.entity.FriendRequest;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.FriendRequestVO;
import com.wuyinai.wuaipdce.model.vo.FriendVO;
import com.wuyinai.wuaipdce.service.FriendRequestService;
import com.wuyinai.wuaipdce.service.FriendService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友关系 服务层实现。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    private FriendRequestService friendRequestService;
    @Resource
    private UserService userService;


    @Override
    public boolean sendFriendRequest(Long userId, FriendRequestDTO requestDTO) {
        Long toUserId = requestDTO.getToUserId();

        // 不能添加自己为好友
        if (userId.equals(toUserId)) {
            return false;
        }

        // 检查是否已经是好友
        QueryWrapper friendQuery = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("friend_id = ?", toUserId)
                .and("status = ?", "friend")
                .and("is_delete = ?", 0);
        if (this.getObj(friendQuery) != null) {
            return false;
        }

        // 检查是否已经发送过请求
        QueryWrapper requestQuery = QueryWrapper.create()
                .where("from_user_id = ?", userId)
                .and("to_user_id = ?", toUserId)
                .and("status = ?", "pending")
                .and("is_delete = ?", 0);
        if (friendRequestService.getObj(requestQuery) != null) {
            return false;
        }

        // 创建好友请求
        FriendRequest friendRequest = FriendRequest.builder()
                .fromUserId(userId)
                .toUserId(toUserId)
                .message(requestDTO.getMessage())
                .status("pending")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        return friendRequestService.save(friendRequest);
    }

    @Override
    public boolean acceptFriendRequest(Long userId, Long requestId) {
        // 获取好友请求
        FriendRequest friendRequest = friendRequestService.getById(requestId);
        if (friendRequest == null || friendRequest.getIsDelete() == 1) {
            return false;
        }

        // 检查请求是否属于当前用户
        if (!friendRequest.getToUserId().equals(userId)) {
            return false;
        }

        // 检查请求状态是否为pending
        if (!"pending".equals(friendRequest.getStatus())) {
            return false;
        }

        // 更新请求状态为accepted
        friendRequest.setStatus("accepted");
        friendRequest.setUpdateTime(LocalDateTime.now());
        if (!friendRequestService.updateById(friendRequest)) {
            return false;
        }

        // 创建双向好友关系
        Long fromUserId = friendRequest.getFromUserId();

        // 创建当前用户到发送者的好友关系
        Friend friend1 = Friend.builder()
                .userId(userId)
                .friendId(fromUserId)
                .status("friend")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        // 创建发送者到当前用户的好友关系
        Friend friend2 = Friend.builder()
                .userId(fromUserId)
                .friendId(userId)
                .status("friend")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        return this.save(friend1) && this.save(friend2);
    }

    @Override
    public boolean rejectFriendRequest(Long userId, Long requestId) {
        // 获取好友请求
        FriendRequest friendRequest = friendRequestService.getById(requestId);
        if (friendRequest == null || friendRequest.getIsDelete() == 1) {
            return false;
        }

        // 检查请求是否属于当前用户
        if (!friendRequest.getToUserId().equals(userId)) {
            return false;
        }

        // 检查请求状态是否为pending
        if (!"pending".equals(friendRequest.getStatus())) {
            return false;
        }

        // 更新请求状态为rejected
        friendRequest.setStatus("rejected");
        friendRequest.setUpdateTime(LocalDateTime.now());

        return friendRequestService.updateById(friendRequest);
    }

    @Override
    public List<FriendVO> getFriendList(Long userId) {
        // 查询当前用户的所有好友
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("friend")
                .join("user").on("friend.friend_id = user.id")
                .where("friend.user_id = ?", userId)
                .and("friend.status = ?", "friend")
                .and("friend.is_delete = ?", 0)
                .orderBy("friend.update_time desc");

        List<User> friendUsers = userService.list(queryWrapper);

        // 转换为FriendVO列表
        return friendUsers.stream().map(user -> {
            FriendVO friendVO = new FriendVO();
            BeanUtil.copyProperties(user, friendVO);
            friendVO.setStatus("friend");
            return friendVO;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        // 删除当前用户到好友的关系
        QueryWrapper queryWrapper1 = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("friend_id = ?", friendId)
                .and("is_delete = ?", 0);

        // 删除好友到当前用户的关系
        QueryWrapper queryWrapper2 = QueryWrapper.create()
                .where("user_id = ?", friendId)
                .and("friend_id = ?", userId)
                .and("is_delete = ?", 0);

        return this.remove(queryWrapper1) && this.remove(queryWrapper2);
    }

    @Override
    public FriendVO getFriendDetail(Long userId, Long friendId) {
        // 检查是否是好友
        QueryWrapper friendQuery = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("friend_id = ?", friendId)
                .and("status = ?", "friend")
                .and("is_delete = ?", 0);

        Friend friend = this.getOne(friendQuery);
        if (friend == null) {
            return null;
        }

        // 获取好友用户信息
        User user = userService.getUserById(friendId);
        if (user == null) {
            return null;
        }

        // 转换为FriendVO
        FriendVO friendVO = new FriendVO();
        BeanUtil.copyProperties(user, friendVO);
        friendVO.setStatus(friend.getStatus());
        friendVO.setCreateTime(friend.getCreateTime());

        return friendVO;
    }

    @Override
    public List<FriendRequestVO> getReceivedFriendRequests(Long userId) {
        // 1. 查询当前用户收到的所有待处理请求
        QueryWrapper requestQueryWrapper = QueryWrapper.create()
                .select()
                .from("friend_request")
                .where("to_user_id = ?", userId)
                .and("status = ?", "pending")
                .and("is_delete = ?", 0)
                .orderBy("create_time desc");

        List<FriendRequest> friendRequests = friendRequestService.list(requestQueryWrapper);
        if (friendRequests.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集所有发送者ID
        List<Long> fromUserIds = friendRequests.stream()
                .map(FriendRequest::getFromUserId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询所有发送者信息
        QueryWrapper userQueryWrapper = QueryWrapper.create()
                .in(User::getId, fromUserIds)
                .and("isDelete = ?",0);
        List<User> fromUsers = userService.list(userQueryWrapper);

        // 4. 将用户信息转换为Map，方便快速查找
        Map<Long, User> userMap = fromUsers.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 5. 转换为FriendRequestVO列表
        List<FriendRequestVO> requestVOList = new ArrayList<>();
        for (FriendRequest friendRequest : friendRequests) {
            User user = userMap.get(friendRequest.getFromUserId());
            if (user != null) {
                FriendRequestVO requestVO = new FriendRequestVO();
                requestVO.setId(friendRequest.getId());
                requestVO.setFromUserId(user.getId());
                requestVO.setFromUserName(user.getUserName());
                requestVO.setFromUserAvatar(user.getUserAvatar());
                requestVO.setFromUserAccount(user.getUserAccount());
                requestVO.setToUserId(friendRequest.getToUserId());
                requestVO.setMessage(friendRequest.getMessage());
                requestVO.setStatus(friendRequest.getStatus());
                requestVO.setCreateTime(friendRequest.getCreateTime());

                requestVOList.add(requestVO);
            }
        }

        return requestVOList;
    }

    @Override
    public List<FriendRequestVO> getSentFriendRequests(Long userId) {
        // 1. 查询当前用户发送的所有请求
        QueryWrapper requestQueryWrapper = QueryWrapper.create()
                .select()
                .from("friend_request")
                .where("from_user_id = ?", userId)
                .and("is_delete = ?", 0)
                .orderBy("create_time desc");

        List<FriendRequest> friendRequests = friendRequestService.list(requestQueryWrapper);
        if (friendRequests.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集所有接收者ID
        List<Long> toUserIds = friendRequests.stream()
                .map(FriendRequest::getToUserId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询所有接收者信息
        QueryWrapper userQueryWrapper = QueryWrapper.create()
                .select()
                .from("user")
                .where("id in (?)", toUserIds);
        List<User> toUsers = userService.list(userQueryWrapper);

        // 4. 将用户信息转换为Map，方便快速查找
        Map<Long, User> userMap = toUsers.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 5. 转换为FriendRequestVO列表
        List<FriendRequestVO> requestVOList = new ArrayList<>();
        for (FriendRequest friendRequest : friendRequests) {
            User user = userMap.get(friendRequest.getToUserId());
            if (user != null) {
                FriendRequestVO requestVO = new FriendRequestVO();
                requestVO.setId(friendRequest.getId());
                requestVO.setFromUserId(friendRequest.getFromUserId());
                requestVO.setFromUserName(user.getUserAccount()); // 这里使用接收者的账号
                requestVO.setFromUserAvatar(user.getUserAvatar());
                requestVO.setFromUserAccount(user.getUserAccount());
                requestVO.setToUserId(user.getId());
                requestVO.setMessage(friendRequest.getMessage());
                requestVO.setStatus(friendRequest.getStatus());
                requestVO.setCreateTime(friendRequest.getCreateTime());

                requestVOList.add(requestVO);
            }
        }

        return requestVOList;
    }

    @Override
    public List<FriendVO> searchUsers(String keyword, Long userId) {
        // 根据关键词搜索用户
        QueryWrapper userQuery = QueryWrapper.create()
                .where("userAccount like ? or userName like ?", "%" + keyword + "%", "%" + keyword + "%")
                .and("id != ?", userId)
                .and("isDelete = ?", 0);

        List<User> users = userService.list(userQuery);

        // 排除已经是好友的用户
        QueryWrapper friendQuery = QueryWrapper.create()
                .select("friend_id")
                .from("friend")
                .where("user_id = ?", userId)
                .and("status = ?", "friend")
                .and("is_delete = ?", 0);

        List<Friend> friends = this.list(friendQuery);
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());

        // 转换为FriendVO列表
        return users.stream()
                .filter(user -> !friendIds.contains(user.getId()))
                .map(user -> {
                    FriendVO friendVO = new FriendVO();
                    BeanUtil.copyProperties(user, friendVO);
                    return friendVO;
                })
                .collect(Collectors.toList());
    }
}
