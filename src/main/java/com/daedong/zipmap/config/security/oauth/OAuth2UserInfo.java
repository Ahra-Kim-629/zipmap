package com.daedong.zipmap.config.security.oauth;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getProviderEmail();
    String getProviderName();

}
