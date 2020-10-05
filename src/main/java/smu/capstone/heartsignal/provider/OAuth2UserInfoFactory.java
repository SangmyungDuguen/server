package smu.capstone.heartsignal.provider;

import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfo;
import smu.capstone.heartsignal.domain.user.User;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return new OAuth2UserInfo(attributes);
    }
}
