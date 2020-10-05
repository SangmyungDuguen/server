package smu.capstone.heartsignal.domain.oAuth2UserInfo;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Document
@Getter
@Setter
public class OAuth2UserInfo implements OidcUser, Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    private ObjectId id;
    private String name;
    private String email;
    private String image;
    protected String authProvider;
    private Map<String, Object> attributes;
    private LocalDateTime time;
    private Boolean old;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        setAttribute();
    }

    protected void setAttribute() {
        this.name = (String) attributes.get("name");
        this.email = (String) attributes.get("email");
        this.image = (String) attributes.get("image");
        this.image = (String) attributes.get("picture");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + getAuthProvider()));
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }

//    public OAuth2UserInfo update(final OAuth2UserInfoDto.UpdateDto updateDto) {
//        if (updateDto.getNickName() != null) {
//            this.nickName = updateDto.getNickName();
//        }
//        if (updateDto.getImage() != null) {
//            if (updateDto.getImage().startsWith("http")) {
//                this.image = updateDto.getImage();
//            } else {
//                this.image = "";
//            }
//        }
//        return this;
//    }
}
