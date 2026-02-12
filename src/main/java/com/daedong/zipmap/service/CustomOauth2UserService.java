package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
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
        } else if (provider.equals("google")) {
            providerId = oAuth2User.getAttribute("sub");
            loginId = provider + "_" + providerId;
            name = oAuth2User.getAttribute("name");
            email = oAuth2User.getAttribute("email");

        }

        User userEntity = userMapper.findByLoginId(loginId);

        if (userEntity == null) {
            userEntity = new User();
            userEntity.setLoginId(loginId);
            userEntity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            userEntity.setName(name);
            userEntity.setEmail(email);
            userEntity.setPhone(mobile != null ? mobile : "000-0000-0000");
            userEntity.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : 'M');
            userEntity.setRole("WRITER");
            userEntity.setAddress("주소 미입력");

            userMapper.save(userEntity);
        }

        return new UserPrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}
