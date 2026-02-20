package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Token;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final PostService postService;
    private final ReviewService reviewService;
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
        String clientIp = getClientIp(request);
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
        String usedIp = getClientIp(request);
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
    public String mypage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());
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
    public String unregister(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByLoginId(userDetails.getUsername());
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

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }


//    @GetMapping("/users/articles")
//    public String articles(@AuthenticationPrincipal UserDetails userDetails,
//                           @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
//                           @RequestParam(required = false, defaultValue = "reviews") String type,
//                           Model model) {
//        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
//
//        model.addAttribute("type", type);
//
//        if ("reviews".equals(type)) {
//            Page<ReviewDTO> reviews = reviewService.findMyReviews(user.getId(), pageable);
//            model.addAttribute("reviews", reviews);
//            model.addAttribute("posts", Page.empty(pageable));
//        } else {
//            Page<Post> posts = postService.findMyPosts(user.getId(), pageable);
//            model.addAttribute("posts", posts);
//            model.addAttribute("reviews", Page.empty(pageable));
//        }
//
//        return "/users/articles";
//    }

    @GetMapping("/users/comments")
    public String comments(@AuthenticationPrincipal UserDetails userDetails,
                           @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(required = false, defaultValue = "reviews") String type,
                           Model model) {

        User user = userService.findByLoginId(userDetails.getUsername());

        model.addAttribute("type", type);

//        if ("reviews".equals(type)) {
//            Page<ReviewReply> replies = reviewService.findMyReplies(user.getId(), pageable);
//            model.addAttribute("replies", replies);
//            model.addAttribute("postReplies", Page.empty(pageable));
//        } else {
//            Page<PostReply> replies = postService.findMyReplies(user.getId(), pageable);
//            model.addAttribute("postReplies", replies);
//            model.addAttribute("replies", Page.empty(pageable));
//        }

        return "/users/comments";
    }
}
