package smu.capstone.heartsignal.controller.viewController;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfo;
import smu.capstone.heartsignal.domain.user.User;
import smu.capstone.heartsignal.domain.user.UserRepository;
import smu.capstone.heartsignal.provider.CurrentUser;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(Model model, @CurrentUser OAuth2UserInfo oAuth2UserInfo) {
        System.out.println(oAuth2UserInfo.getId());
        System.out.println(oAuth2UserInfo.getEmail());

        Mono<User> userMono = userRepository.findById(oAuth2UserInfo.getEmail())
                .flatMap(u1 -> {
                    u1.setName(oAuth2UserInfo.getName());
                    u1.setImage(oAuth2UserInfo.getImage());
                    return userRepository.save(u1);
                });
        model.addAttribute("user", userMono);

        return "home";
    }
}
