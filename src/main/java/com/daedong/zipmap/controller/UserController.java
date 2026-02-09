package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String signUp(User user, RedirectAttributes rttr){
        try {
            userService.signUp(user);
            rttr.addFlashAttribute("success", "회원가입이 완료되었습니다.");
        }catch (Exception e){
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
    public String login(User user, RedirectAttributes rttr){
        try {
            User findUser = userService.findByLoginId(user.getLogin_id());
            if(findUser.getPassword().equals(user.getPassword())){
                return "redirect:/";
            }else{
                rttr.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/login";
            }
        }catch (Exception e){
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/login";
    }




}
