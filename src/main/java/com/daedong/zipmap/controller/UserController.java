package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.UserService;
import jakarta.servlet.http.HttpSession;
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
            model.addAttribute("user", userService.findByLoginId(userDetails.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
        return "/users/mypage";
    }

    @PostMapping("/users/mypage")
    public String mypage(User user,
                         @RequestParam String password,
                         @RequestParam String new_password,
                         @RequestParam String password_confirm,
                         RedirectAttributes rttr) {

            try {
                User findUser = userService.findByLoginId(user.getLoginId());
                boolean isMatch = passwordEncoder.matches(user.getPassword(), findUser.getPassword());
                if (isMatch) {
//                    userService.update(user);
                    System.out.println("성공");

                    rttr.addFlashAttribute("message", "회원 정보 수정이 완료되었습니다.");
                    return "redirect:/";
                } else {
                    System.out.println("실패");
                    rttr.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/";
            }

        return "redirect:/users/mypage";
    }


    @GetMapping("/users/unregister")
    public String unregister(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByLoginId(userDetails.getUsername());
            model.addAttribute("user", user);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
        return "/users/unregister";
    }

    @PostMapping("/users/unregister")
    public String unregister(User user, RedirectAttributes rttr, HttpSession session) {
        try {
            userService.unregister(user);
            rttr.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            session.invalidate();
            return "redirect:/";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/unregister";
        }
    }




}
