package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // naver
        String providerId = "";
        String loginId = "";
        String name = "";
        String email = "";
        String mobile = "";
        String gender = "";

        if (provider.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            providerId = (String) response.get("id");
            loginId = provider + "_" + providerId;
            name = (String) response.get("name");
            email = (String) response.get("email");
            mobile = (String) response.get("mobile");
            gender = (String) response.get("gender");
        }

        User userEntity = userMapper.findByLoginId(loginId);

        if (userEntity == null) {
            userEntity = new User();
            userEntity.setLoginId(loginId);
            userEntity.setPassword(UUID.randomUUID().toString()); // 임의의 비밀번호
            userEntity.setName(name);
            userEntity.setEmail(email);
            userEntity.setPhone(mobile);
            if (gender != null && !gender.isEmpty()) {
                userEntity.setGender(gender.charAt(0));
            }
            userEntity.setRole("WRITER");
            userEntity.setAddress("주소 미입력"); // 필수 필드라면 기본값 설정

            userMapper.save(userEntity);
        }

        // 필요하다면 여기서 기존 회원의 정보를 업데이트하는 로직을 추가할 수 있습니다.

        return new UserPrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}
