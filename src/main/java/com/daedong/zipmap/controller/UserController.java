package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.MailService;
import com.daedong.zipmap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signUp")
    public String signUp() {
        return "/users/signUpForm";
    }

    @PostMapping("/signUp")
    public String signUp(User user, RedirectAttributes rttr) {
        try {
            userService.signUp(user);
            rttr.addFlashAttribute("success", "회원가입이 완료되었습니다.");
            user.setRole("ROLE_WRITER");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "/users/loginForm";
    }

    @PostMapping("/login")
    public String login(User user, RedirectAttributes rttr) {
        try {

            User findUser = userService.findByLoginId(user.getLoginId());
            boolean isMatch = passwordEncoder.matches(user.getPassword(), findUser.getPassword());
            if (isMatch) {
                return "redirect:/";
            } else {
                rttr.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/login";
            }
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/login";
    }

    @GetMapping("/users/find/id")
    public String findId() {
        return "/users/find_id_form";
    }

    @PostMapping("/users/find/id")
    public String findId(String name, String email, RedirectAttributes rttr) {
        try {
            User user = userService.findId(name, email);
            rttr.addFlashAttribute("message", "찾으시는 아이디는 " + user.getLoginId() + " 입니다.");
            return "redirect:/login";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/find/id";
        }
    }

    @GetMapping("/users/find/password")
    public String findPassword() {
        return "/users/find_password_form";
    }

    @PostMapping("/users/find/password")
    public String findPassword(@RequestParam String loginId, String name, String email, RedirectAttributes rttr) {
        try {
            userService.passwordReset(loginId, name, email);
            rttr.addFlashAttribute("message", "비밀번호 재설정 메일을 발송했습니다.");
            return "redirect:/login";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/find/password";
        }
    }

    @GetMapping("/users/mypage")
    public String mypage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
        return "/users/mypage";
    }
}
