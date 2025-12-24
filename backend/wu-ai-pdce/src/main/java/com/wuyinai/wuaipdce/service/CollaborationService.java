package com.wuyinai.wuaipdce.service;

import com.mybatisflex.core.service.IService;
import com.wuyinai.wuaipdce.model.entity.ChatHistory;
import com.wuyinai.wuaipdce.model.entity.CollaborationMember;
import com.wuyinai.wuaipdce.model.entity.CollaborationRecord;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.CollaborationMemberVO;

import java.util.List;

/**
 * 协作服务 服务层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface CollaborationService extends IService<CollaborationMember> {

    /**
     * 开始协作，创建协作记录
     * @param appId 应用ID
     * @param creatorId 创建者ID
     * @return 协作记录ID
     */
    Long startCollaboration(Long appId, Long creatorId);

    /**
     * 添加协作者
     * @param collaborationId 协作记录ID
     * @param userId 要添加的用户ID
     */
    void addCollaborator(Long collaborationId, Long userId);

    /**
     * 移除协作者
     * @param collaborationId 协作记录ID
     * @param userId 要移除的用户ID
     */
    void removeCollaborator(Long collaborationId, Long userId);

    /**
     * 退出协作，清除所有协作者并更新协作记录状态
     * @param collaborationId 协作记录ID
     */
    void exitCollaboration(Long collaborationId);

    /**
     * 获取协作记录
     * @param collaborationId 协作记录ID
     * @return 协作记录
     */
    CollaborationRecord getCollaborationRecord(Long collaborationId);

    /**
     * 根据应用ID获取协作记录
     * @param appId 应用ID
     * @return 协作记录
     */
    CollaborationRecord getCollaborationRecordByAppId(Long appId);

    /**
     * 获取协作成员列表
     * @param collaborationId 协作记录ID
     * @return 协作成员列表
     */
    List<CollaborationMemberVO> getCollaborationMembers(Long collaborationId);

    /**
     * 获取用户参与的协作记录列表
     * @param userId 用户ID
     * @return 协作记录列表
     */
    List<CollaborationRecord> getCollaborationsByUserId(Long userId);

    /**
     * 检查用户是否是协作成员
     * @param collaborationId 协作记录ID
     * @param userId 用户ID
     * @return 是否是协作成员
     */
    boolean isCollaborator(Long collaborationId, Long userId);

    /**
     * 检查用户是否是协作创建者
     * @param collaborationId 协作记录ID
     * @param userId 用户ID
     * @return 是否是协作创建者
     */
    boolean isCollaborationCreator(Long collaborationId, Long userId);

    /**
     * 获取在线好友列表
     * @param userId 当前用户ID
     * @return 在线好友列表
     */
    List<User> getOnlineFriends(Long userId);

}