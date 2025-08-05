package com.wuyinai.wuaipdce.model.dto.user;

import com.wuyinai.wuaipdce.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * 查询用户封装
 */
@EqualsAndHashCode(callSuper = true)
//生成equals和hashCode方法
//callSuper = true：在生成方法时调用父类的equals和hashCode方法，确保继承关系中的比较完整性
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
