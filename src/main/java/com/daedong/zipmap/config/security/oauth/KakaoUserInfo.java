package com.daedong.zipmap.config.security.oauth;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo{
    private Map<String, Object> attribute;

    public KakaoUserInfo(Map<String, Object> attribute) { this.attribute = attribute; }

    @Override
    public String getProviderId() { return attribute.get("id").toString(); }

    @Override
    public String getProvider() { return "kakao"; }

    @Override
    public String getProviderEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getProviderName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");

        if (kakaoAccount != null) {
            return (String) kakaoAccount.get("name");
        }
        return null;
    }
}
