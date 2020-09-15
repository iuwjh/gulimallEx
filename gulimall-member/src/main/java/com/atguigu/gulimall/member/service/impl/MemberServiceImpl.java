package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.controller.vo.MemberRegistVo;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PasswordFormatException;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.remote.SocialUserService;
import com.atguigu.gulimall.member.remote.vo.SocialUserVo;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.SocialUserAccessVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Map;


@Service("memberService")
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    private final MemberDao memberDao;

    private final MemberLevelDao memberLevelDao;

    private final SocialUserService socialUserService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
            new Query<MemberEntity>().getPage(params),
            new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) throws PhoneExistException, UsernameExistException {
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        if (!checkPasswordFormat(vo.getPassword())) {
            throw new PasswordFormatException();
        }
        if (!memberDao.checkPhoneUnique(vo.getPhone())) {
            throw new PhoneExistException();
        }
        if (!memberDao.checkUsernameUnique(vo.getUserName())) {
            throw new UsernameExistException();
        }

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());

        memberEntity.setNickname(vo.getUserName());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPass = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encodedPass);

        memberDao.insert(memberEntity);
    }

    private boolean checkPasswordFormat(String password) {
        return password.length() >= 6;
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();

        MemberEntity entity = memberDao.findByLoginAcct(loginAcct);
        if (entity == null) {
            return null;
        } else {
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            boolean passMatch = passwordEncoder.matches(password, passwordDb);
            if (passMatch) {
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity loginSocial(SocialUserAccessVo socialUserAccessVo) {
        String uid = socialUserAccessVo.getUid();
        MemberEntity memberEntity = memberDao.findBySocialUid(uid);
        if (memberEntity != null) {
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUserAccessVo.getAccessToken());
            update.setExpiresIn(socialUserAccessVo.getExpiresIn());

            memberDao.updateById(update);

            memberEntity.setAccessToken(socialUserAccessVo.getAccessToken());
            memberEntity.setExpiresIn(socialUserAccessVo.getExpiresIn());

            return memberEntity;
        } else {
            MemberEntity regist = new MemberEntity();

            try {
                SocialUserVo userVo = socialUserService.weiboRegist(socialUserAccessVo);
                if (userVo == null) {
                    return null;
                }
                regist.setNickname(userVo.getNickname());
                regist.setGender("m".equals(userVo.getGender()) ? 1 : 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }

            regist.setSocialUid(socialUserAccessVo.getUid());
            regist.setAccessToken(socialUserAccessVo.getAccessToken());
            regist.setExpiresIn(socialUserAccessVo.getExpiresIn());

            memberDao.insert(regist);
            return regist;
        }
    }
}
