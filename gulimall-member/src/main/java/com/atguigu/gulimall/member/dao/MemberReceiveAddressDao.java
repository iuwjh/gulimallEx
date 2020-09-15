package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员收货地址
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
@Mapper
public interface MemberReceiveAddressDao extends BaseMapper<MemberReceiveAddressEntity> {

    default List<MemberReceiveAddressEntity> listByMemberId(long memberId) {
        return selectList(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));
    }

}
