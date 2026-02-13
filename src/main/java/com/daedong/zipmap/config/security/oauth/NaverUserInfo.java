package com.daedong.zipmap.config.security.oauth;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo{

    private Map<String, Object> attributes; //getAttributes()를 의미
    private Map<String, Object> response;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProviderId() {
        return (String) response.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderEmail() {
        return (String) response.get("email");
    }

    @Override
    public String getProviderName() {
        return (String) response.get("name");
    }

    public String getMobile(){
        return (String) response.get("mobile");
    }

    public String getGender(){
        return (String) response.get("gender");
    }
}
