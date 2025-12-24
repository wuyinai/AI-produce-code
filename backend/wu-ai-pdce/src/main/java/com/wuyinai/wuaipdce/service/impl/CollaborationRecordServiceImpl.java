package com.wuyinai.wuaipdce.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wuyinai.wuaipdce.mapper.CollaborationMemberMapper;
import com.wuyinai.wuaipdce.mapper.CollaborationRecordMapper;
import com.wuyinai.wuaipdce.model.entity.CollaborationMember;
import com.wuyinai.wuaipdce.model.entity.CollaborationRecord;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.service.CollaborationRecordService;
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
public class CollaborationRecordServiceImpl extends ServiceImpl<CollaborationRecordMapper, CollaborationRecord> implements CollaborationRecordService {

}