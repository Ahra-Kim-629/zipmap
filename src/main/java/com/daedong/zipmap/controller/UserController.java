package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            if (findUser.getPassword().equals(user.getPassword())) {
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
            User user = userService.findPassword(name, email);
            rttr.addFlashAttribute("message", "찾으시는 비밀번호는 " + user.getPassword() + " 입니다.");
            return "redirect:/login";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/find/password";
        }
    }

    @GetMapping("/users/mypage")
    public String mypage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
//        if (userDetails != null) {
        // Usually UserService would have a findByLoginId method
        // For now, we assume the user data is available or fetched
        // Adding a placeholder for the logic requested by the comment
//             model.addAttribute("user", userService.findByLoginId(userDetails.getUsername()));
//        }

        try {
            model.addAttribute("user", userService.findById(2));
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
        return "/users/mypage";
    }
}
