package com.dev.vocalab.home;

import com.dev.vocalab.users.details.AuthenticationUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
//        HTTP SESSION
        AuthenticationUtil.addUserSessionToModel(model);
        return "home";
    }
}
