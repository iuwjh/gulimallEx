package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    default boolean checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        return count == 0;
    }


    default boolean checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        return count == 0;
    }

    default MemberEntity findByLoginAcct(String loginAcct) {
        return selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAcct)
            .or().eq("mobile", loginAcct));
    }

    default MemberEntity findBySocialUid(String uid) {
        return selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
    }
}
