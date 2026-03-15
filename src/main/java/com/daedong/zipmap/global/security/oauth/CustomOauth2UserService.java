package com.daedong.zipmap.global.security.oauth;

import com.daedong.zipmap.global.common.enums.Status;
import com.daedong.zipmap.domain.member.entity.User;
import com.daedong.zipmap.global.security.auth.UserPrincipalDetails;
import com.daedong.zipmap.domain.member.enums.UserRole;
import com.daedong.zipmap.domain.member.mapper.UserMapper;
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
    private final UserMapper userMapper;
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

        User userEntity = userMapper.findByLoginId(loginId);

        if (userEntity == null) {
            userEntity = new User();
            userEntity.setLoginId(loginId);
            userEntity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            userEntity.setName((name != null && !name.isEmpty()) ? name : provider + "_" + loginId);
            userEntity.setEmail((email != null && !email.isEmpty()) ? email : provider + "_" + loginId + "@" + provider + ".com");
            userEntity.setGender(
                    (gender != null && !gender.isEmpty())
                            ? gender.charAt(0)
                            : 'M'
            );
            // 2-24 수정
            // userEntity.setRole("WRITER");
            // userEntity.setAccountStatus("ACTIVE");

            userEntity.setRole(UserRole.USER);         //  Enum으로 변경
            userEntity.setAccountStatus(Status.ACTIVE);  //  Enum으로 변경
            userMapper.save(userEntity);
        }

        return new UserPrincipalDetails(userEntity, oAuth2User.getAttributes());
    }

}
