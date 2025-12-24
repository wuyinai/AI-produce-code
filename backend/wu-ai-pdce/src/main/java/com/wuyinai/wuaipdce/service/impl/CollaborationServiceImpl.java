package com.wuyinai.wuaipdce.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wuyinai.wuaipdce.mapper.*;
import com.wuyinai.wuaipdce.model.entity.*;
import com.wuyinai.wuaipdce.model.vo.CollaborationMemberVO;
import com.wuyinai.wuaipdce.service.CollaborationRecordService;
import com.wuyinai.wuaipdce.service.CollaborationService;
import com.wuyinai.wuaipdce.service.FriendService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 协作服务 服务层实现。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Service
public class CollaborationServiceImpl extends ServiceImpl<CollaborationMemberMapper, CollaborationMember> implements CollaborationService {

    @Resource
    private CollaborationRecordService collaborationRecordService;

    @Resource
    private UserService userService;

    @Resource
    private FriendService friendService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startCollaboration(Long appId, Long creatorId) {
        // 检查是否已经存在有效的协作记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("collaboration_record")
                .where("appId = ?", appId)
                .and("status = ?", "valid")
                .and("isDelete = ?", 0);
        CollaborationRecord existingRecord = collaborationRecordService.getOne(queryWrapper);
        if (existingRecord != null) {
            return existingRecord.getId();
        }

        // 创建新的协作记录
        CollaborationRecord collaborationRecord = CollaborationRecord.builder()
                .appId(appId)
                .creatorId(creatorId)
                .status("valid")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();
        collaborationRecordService.save(collaborationRecord);

        // 将创建者添加为协作者
        CollaborationMember collaborationMember = CollaborationMember.builder()
                .collaborationId(collaborationRecord.getId())
                .userId(creatorId)
                .joinTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();
        this.save(collaborationMember);

        return collaborationRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCollaborator(Long collaborationId, Long userId) {
        // 检查协作记录是否存在且有效
        CollaborationRecord collaborationRecord = collaborationRecordService.getById(collaborationId);
        if (collaborationRecord == null || !"valid".equals(collaborationRecord.getStatus()) || collaborationRecord.getIsDelete() == 1) {
            throw new IllegalArgumentException("无效的协作记录");
        }

        // 检查用户是否已经是协作者
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("collaboration_member")
                .where("collaborationId = ?", collaborationId)
                .and("userId = ?", userId)
                .and("isDelete = ?", 0);
        CollaborationMember existingMember = this.getOne(queryWrapper);
        if (existingMember != null) {
            return; // 用户已经是协作者，不需要重复添加
        }

        // 添加协作者
        CollaborationMember collaborationMember = CollaborationMember.builder()
                .collaborationId(collaborationId)
                .userId(userId)
                .joinTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();
        this.save(collaborationMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCollaborator(Long collaborationId, Long userId) {
        // 检查协作记录是否存在且有效
        CollaborationRecord collaborationRecord = collaborationRecordService.getById(collaborationId);
        if (collaborationRecord == null || !"valid".equals(collaborationRecord.getStatus()) || collaborationRecord.getIsDelete() == 1) {
            throw new IllegalArgumentException("无效的协作记录");
        }

        // 移除协作者
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("collaborationId = ?", collaborationId)
                .and("userId = ?", userId);
        this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitCollaboration(Long collaborationId) {
        // 检查协作记录是否存在且有效
        CollaborationRecord collaborationRecord = collaborationRecordService.getById(collaborationId);
        if (collaborationRecord == null || collaborationRecord.getIsDelete() == 1) {
            throw new IllegalArgumentException("无效的协作记录");
        }

        // 更新协作记录状态为无效
        collaborationRecord.setStatus("invalid");
        collaborationRecord.setUpdateTime(LocalDateTime.now());
        collaborationRecordService.updateById(collaborationRecord);

        // 移除所有协作者
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("collaborationId = ?", collaborationId);
        this.remove(queryWrapper);
    }

    @Override
    public CollaborationRecord getCollaborationRecord(Long collaborationId) {
        return collaborationRecordService.getById(collaborationId);
    }

    @Override
    public CollaborationRecord getCollaborationRecordByAppId(Long appId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("collaboration_record")
                .where("appId = ?", appId)
                .and("isDelete = ?", 0);
        return collaborationRecordService.getOne(queryWrapper);
    }

    @Override
    public List<CollaborationMemberVO> getCollaborationMembers(Long collaborationId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("collaboration_member")
                .where("collaborationId = ?", collaborationId)
                .and("isDelete = ?", 0);
        List<CollaborationMember> collaborationMembers = this.list(queryWrapper);
        
        // 转换为VO列表，包含用户名
        return collaborationMembers.stream()
                .map(member -> {
                    CollaborationMemberVO vo = new CollaborationMemberVO();
                    vo.setId(member.getId());
                    vo.setCollaborationId(member.getCollaborationId());
                    vo.setUserId(member.getUserId());
                    vo.setJoinTime(member.getJoinTime());
                    vo.setCreateTime(member.getCreateTime());
                    
                    // 获取用户名
                    User user = userService.getById(member.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getUserName());
                    }
                    
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<CollaborationRecord> getCollaborationsByUserId(Long userId) {
        // 先获取用户作为协作者的所有协作记录ID
        QueryWrapper memberQuery = QueryWrapper.create()
                .select("collaborationId")
                .from("collaboration_member")
                .where("userId = ?", userId)
                .and("isDelete = ?", 0);
        List<Long> collaborationIds = this.listAs(memberQuery, Long.class);

        if (collaborationIds.isEmpty()) {
            return List.of();
        }

        List<CollaborationRecord> onlineFriends = List.of();
        if (!collaborationIds.isEmpty()) {
            onlineFriends = collaborationIds.stream()
                    .map(collaborationId -> collaborationRecordService.getById(collaborationId))
                    .filter(collaborationRecord -> collaborationRecord != null && collaborationRecord.getIsDelete() != 1)
                    .toList();
        }
        return onlineFriends;
    }

    @Override
    public boolean isCollaborator(Long collaborationId, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from("collaboration_member")
                .where("collaborationId = ?", collaborationId)
                .and("userId = ?", userId)
                .and("isDelete = ?", 0);
        CollaborationMember collaborationMember = this.getOne(queryWrapper);
        return collaborationMember != null;
    }

    @Override
    public boolean isCollaborationCreator(Long collaborationId, Long userId) {
        CollaborationRecord collaborationRecord = collaborationRecordService.getById(collaborationId);
        if (collaborationRecord == null || collaborationRecord.getIsDelete() == 1) {
            return false;
        }
        return collaborationRecord.getCreatorId().equals(userId);
    }

    @Override
    public List<User> getOnlineFriends(Long userId) {
        // 先获取用户的所有好友ID
        QueryWrapper friendQuery = QueryWrapper.create()
                .select("friend_id")
                .from("friend")
                .where("user_id = ?", userId)
                .and("status = ?", "friend")
                .and("is_delete = ?", 0);
        List<Long> friendIds = friendService.listAs(friendQuery, Long.class);

        if (friendIds.isEmpty()) {
            return List.of();
        }

        // 再获取在线的好友列表
        List<User> onlineFriends = List.of();
        if (!friendIds.isEmpty()) {
            onlineFriends = friendIds.stream()
                    .map(friendId -> userService.getById(friendId))
                    .filter(user -> user != null && "online".equals(user.getOnlineStatus()) && user.getIsDelete() != 1)
                    .toList();
        }
        return onlineFriends;
    }

}