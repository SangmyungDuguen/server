package smu.capstone.heartsignal.service.userService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfo;
import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfoRepository;
import smu.capstone.heartsignal.domain.user.User;
import smu.capstone.heartsignal.domain.user.UserRepository;
import smu.capstone.heartsignal.provider.OAuth2UserInfoFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final OAuth2UserInfoRepository oAuth2UserInfoRepository;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();
        final String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();

        Mono<OAuth2User> oAuth2User = delegate.loadUser(userRequest);

        return oAuth2User.flatMap(e -> {
            LocalDateTime now = LocalDateTime.now();
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(clientRegistrationId, e.getAttributes());
            oAuth2UserInfo.setTime(now);

            return oAuth2UserInfoRepository
                    .findById(oAuth2UserInfo.getEmail())
                    .switchIfEmpty(Mono.defer(() -> oAuth2UserInfoRepository.save(oAuth2UserInfo)));
        });
    }
}
