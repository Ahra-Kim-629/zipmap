package com.daedong.zipmap.global.security.oauth;

import com.daedong.zipmap.global.common.enums.Status;
import com.daedong.zipmap.domain.member.entity.Member;
import com.daedong.zipmap.global.security.auth.UserPrincipalDetails;
import com.daedong.zipmap.domain.member.enums.MemberRole;
import com.daedong.zipmap.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if (provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            throw new RuntimeException("지원하지 않는 로그인입니다.");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String loginId = provider + "_" + providerId;
        String email = oAuth2UserInfo.getProviderEmail();
        String name = oAuth2UserInfo.getProviderName();
        String gender = "";

        Member memberEntity = memberMapper.findByLoginId(loginId);

        if (memberEntity == null) {
            memberEntity = new Member();
            memberEntity.setLoginId(loginId);
            memberEntity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            memberEntity.setName((name != null && !name.isEmpty()) ? name : provider + "_" + loginId);
            memberEntity.setEmail((email != null && !email.isEmpty()) ? email : provider + "_" + loginId + "@" + provider + ".com");
            memberEntity.setGender(
                    (gender != null && !gender.isEmpty())
                            ? gender.charAt(0)
                            : 'M'
            );
            // 2-24 수정
            // userEntity.setRole("WRITER");
            // userEntity.setAccountStatus("ACTIVE");

            memberEntity.setRole(MemberRole.USER);         //  Enum으로 변경
            memberEntity.setAccountStatus(Status.ACTIVE);  //  Enum으로 변경
            memberMapper.save(memberEntity);
        }

        return new UserPrincipalDetails(memberEntity, oAuth2User.getAttributes());
    }

}
