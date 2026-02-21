package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.NetworkUtil;
import com.daedong.zipmap.util.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final PostService postService;
    private final ReviewService reviewService;
    private final ReplyService replyService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signUp")
    public String signUp() {
        return "/users/sign-up-form";
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
        return "/users/login-form";
    }

    @GetMapping("/users/find/id")
    public String findId() {
        return "/users/find-id-form";
    }

    @PostMapping("/users/find/id")
    public String findId(String name, String email, RedirectAttributes rttr) {
        try {
            User user = userService.findByNameAndEmail(name, email);
            rttr.addFlashAttribute("success", "찾으시는 아이디는 " + user.getLoginId() + " 입니다.");
            return "redirect:/login";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/find/id";
        }
    }

    @GetMapping("/users/find/password")
    public String findPassword() {
        return "/users/find-password-form";
    }

    @PostMapping("/users/find/password")
    public String findPassword(@RequestParam String loginId, String name, String email, RedirectAttributes rttr, HttpServletRequest request) {
        String clientIp = NetworkUtil.getClientIp(request);
        try {
            userService.passwordReset(loginId, name, email, clientIp);
            rttr.addFlashAttribute("success", "비밀번호 재설정 메일을 발송했습니다.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/find/password";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "redirect:/users/find/password";
        }
    }

    @GetMapping("/users/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        Token tokenData = userService.selectValidToken(token);
        if (tokenData == null) {
            model.addAttribute("error", "유효하지 않은 링크입니다.");
            return "redirect:/";
        }

        model.addAttribute("token", token);
        return "/users/reset-password-form";
    }

    @PostMapping("/users/reset-password")
    public String resetPassword(@RequestParam String token, String newPassword, RedirectAttributes rttr, HttpServletRequest request) {
        String usedIp = NetworkUtil.getClientIp(request);
        try {
            userService.confirmReset(token, newPassword, usedIp);
            rttr.addFlashAttribute("success", "비밀번호가 재설정되었습니다.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }

    @GetMapping("/users/mypage")
    public String mypage(Model model, @AuthenticationPrincipal User user) {
        try {
            model.addAttribute("user", user);
        } catch (Exception e) {
            return "redirect:/";
        }
        return "/users/mypage";
    }

    @PostMapping("/users/mypage")
    // 혼자서 해보려다가 AI 도움 받았어요. 공부 조금더 필요합니다!!!
    public String mypage(User user,
                         @RequestParam(required = false) String new_password,
                         @RequestParam(required = false) String password_confirm,
                         RedirectAttributes rttr) {
        try {
            User findUser = userService.findByLoginId(user.getLoginId());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                if (!passwordEncoder.matches(user.getPassword(), findUser.getPassword())) {
                    rttr.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                    return "redirect:/users/mypage";
                }
            }

            if (new_password != null && !new_password.isEmpty()) {
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    rttr.addFlashAttribute("error", "비밀번호 변경을 위해서는 현재 비밀번호 입력이 필요합니다.");
                    return "redirect:/users/mypage";
                }

                if (!new_password.equals(password_confirm)) {
                    rttr.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                    return "redirect:/users/mypage";
                }

                user.setPassword(passwordEncoder.encode(new_password));
            } else {
                user.setPassword(null);
            }

            user.setId(findUser.getId());
            userService.update(user);

            rttr.addFlashAttribute("success", "회원 정보 수정이 완료되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            rttr.addFlashAttribute("error", "회원 정보 수정 중 오류가 발생했습니다.");
            return "redirect:/users/mypage";
        }
    }

    @GetMapping("/users/unregister")
    public String unregister(Model model, @AuthenticationPrincipal User user) {
        try {
            model.addAttribute("user", user);
        } catch (Exception e) {
            return "redirect:/";
        }
        return "/users/unregister";
    }

    @PostMapping("/users/unregister")
    public String unregister(User user, RedirectAttributes rttr, HttpSession session) {
        try {
            userService.unregister(user);
            rttr.addFlashAttribute("success", "회원 탈퇴가 완료되었습니다.");
            session.invalidate();
            return "redirect:/";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/unregister";
        }
    }

    @GetMapping("/users/articles")
    public String myReviews(@AuthenticationPrincipal User user,
                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            @RequestParam(required = false, defaultValue = "reviews") String type,
                            Model model) {
        model.addAttribute("type", type);

        if ("reviews".equals(type)) {
            Page<ReviewDTO> reviewList = reviewService.getMyReviews(user.getId(), pageable);
            model.addAttribute("reviews", reviewList);
            model.addAttribute("posts", Page.empty(pageable));
        } else if ("posts".equals(type)) {
            Page<PostDTO> postList = postService.getMyPosts(user.getId(), pageable);
            model.addAttribute("posts", postList);
            model.addAttribute("reviews", Page.empty(pageable));
        }

        return "/users/articles";
    }

    @GetMapping("/users/comments")
    public String comments(@AuthenticationPrincipal User user,
                           @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(required = false, defaultValue = "reviews") String type,
                           Model model) {

        model.addAttribute("type", type);

        if ("reviews".equals(type)) {
            Page<ReplyDTO> reviewReplyList = replyService.findByTargetTypeAndUserId("review", user.getId(), pageable);
            model.addAttribute("reviewReplies", reviewReplyList);
            model.addAttribute("postReplies", Page.empty(pageable));
        } else {
            Page<ReplyDTO> postReplies = replyService.findByTargetTypeAndUserId("post", user.getId(), pageable);
            model.addAttribute("postReplies", postReplies);
            model.addAttribute("reviewReplyList", Page.empty(pageable));
        }

        return "/users/comments";
    }
}
