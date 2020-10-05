package smu.capstone.heartsignal.domain.oAuth2UserInfo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OAuth2UserInfoRepository extends ReactiveMongoRepository<OAuth2UserInfo, String> {
}
