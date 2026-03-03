package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.*;
import com.daedong.zipmap.util.NetworkUtil;
import com.daedong.zipmap.util.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final ReviewService reviewService;
    private final ReplyService replyService;
    private final ReactionService reactionService;
    private final PasswordEncoder passwordEncoder;
    private final AlarmService alarmService;


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

    @GetMapping("/check-id")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkId(@RequestParam String loginId) {
        boolean isDuplicate = userService.isLoginIdDuplicate(loginId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
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
    public String mypage(Model model, @AuthenticationPrincipal UserPrincipalDetails user) {
        try {
            model.addAttribute("user", user.getUser());

            // 이 사용자의 알림 목록 가져와서 화면에 보내기
            List<AlarmDTO> alarmList = alarmService.getAlarmList(user.getUser().getId());
            model.addAttribute("alarmList", alarmList);
        } catch (Exception e) {
            return "redirect:/";
        }
        return "/users/mypage";
    }

    @PostMapping("/users/mypage")
    public String mypage(@AuthenticationPrincipal UserPrincipalDetails user,
                         @RequestParam(required = false) String current_password,
                         @RequestParam(required = false) String new_password,
                         @RequestParam(required = false) String password_confirm,
                         RedirectAttributes rttr) {
        try {
            User findUser = userService.findByLoginId(user.getUsername());

            if (new_password != null && !new_password.isEmpty()) {
                if(current_password == null || current_password.isEmpty()){
                    rttr.addFlashAttribute("error", "현재 비밀번호를 입력해주세요.");
                    return "redirect:/users/mypage";
                }
                if (!passwordEncoder.matches(current_password, findUser.getPassword())) {
                    rttr.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                    return "redirect:/users/mypage";
                }

                if (password_confirm == null || !new_password.equals(password_confirm)) {
                    rttr.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                    return "redirect:/users/mypage";
                }
                findUser.setPassword(passwordEncoder.encode(new_password));

            }

            userService.update(findUser);

            rttr.addFlashAttribute("success", "회원 정보 수정이 완료되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            rttr.addFlashAttribute("error", "회원 정보 수정 중 오류가 발생했습니다.");
            return "redirect:/users/mypage";
        }
    }

    @GetMapping("/users/unregister")
    public String unregister(Model model, @AuthenticationPrincipal UserPrincipalDetails user) {
        try {
            model.addAttribute("user", user.getUser());
        } catch (Exception e) {
            return "redirect:/";
        }
        return "/users/unregister";
    }

    @PostMapping("/users/unregister")
    public String unregister(@AuthenticationPrincipal UserPrincipalDetails user, RedirectAttributes rttr, HttpSession session) {
        try {
            userService.unregister(user.getUser());
            rttr.addFlashAttribute("success", "회원 탈퇴가 완료되었습니다.");
            session.invalidate();
            return "redirect:/";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/unregister";
        }
    }

    @GetMapping("/users/articles")
    public String myReviews(@AuthenticationPrincipal UserPrincipalDetails user,
                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            @RequestParam(required = false, defaultValue = "reviews") String type,
                            Model model) {
        model.addAttribute("type", type);

        if ("reviews".equals(type)) {
            Page<ReviewDTO> reviewList = reviewService.getMyReviews(user.getUser().getId(), pageable);
            model.addAttribute("reviews", reviewList);
            model.addAttribute("posts", Page.empty(pageable));
        } else if ("posts".equals(type)) {
            Page<PostDTO> postList = postService.getMyPosts(user.getUser().getId(), pageable);
            model.addAttribute("posts", postList);
            model.addAttribute("reviews", Page.empty(pageable));
        }

        return "/users/articles";
    }

    @GetMapping("/users/comments")
    public String comments(@AuthenticationPrincipal UserPrincipalDetails user,
                           @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(required = false, defaultValue = "reviews") String type,
                           Model model) {

        model.addAttribute("type", type);

        if ("reviews".equals(type)) {
            Page<ReplyDTO> reviewReplyList = replyService.findByTargetTypeAndUserId("review", user.getUser().getId(), pageable);
            model.addAttribute("reviewReplies", reviewReplyList);
            model.addAttribute("postReplies", Page.empty(pageable));
        } else {
            Page<ReplyDTO> postReplies = replyService.findByTargetTypeAndUserId("post", user.getUser().getId(), pageable);
            model.addAttribute("postReplies", postReplies);
            model.addAttribute("reviewReplyList", Page.empty(pageable));
        }

        return "/users/comments";
    }

    @GetMapping("/users/liked")
    public String liked(@AuthenticationPrincipal UserPrincipalDetails user,
                        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                        @RequestParam(required = false, defaultValue = "reviews") String type,
                        Model model) {
        model.addAttribute("type", type);

        if ("reviews".equals(type)) {
            Page<ReviewDTO> reviewList = reactionService.getLikedReviews(user.getUser().getId(), pageable);
            model.addAttribute("reviews", reviewList);
            model.addAttribute("posts", Page.empty(pageable));
        } else if ("posts".equals(type)) {
            Page<PostDTO> postList = reactionService.getLikedPosts(user.getUser().getId(), pageable);
            model.addAttribute("posts", postList);
            model.addAttribute("reviews", Page.empty(pageable));
        }

        return "/users/liked";
    }

//    @GetMapping("/trigger-500-error")
//    public String triggerError() {
//        // 이 지점에서 의도적으로 RuntimeException을 발생시킵니다.
//        throw new RuntimeException("의도적으로 발생시킨 500 에러입니다.");
//    }

// ... 상단 생략 ...

    @GetMapping("/articles")
    public String getMyArticles(@AuthenticationPrincipal UserPrincipalDetails userDetails,
                                @RequestParam(defaultValue = "reviews") String type,
                                Pageable pageable, Model model) {

        // 1. 유저 ID 추출
        Long userId = userDetails.getUser().getId();
        System.out.println("현재 로그인 유저 ID: " + userId); // 디버깅용

        // 2. 데이터 조회 로직 (여기에 넣으세요!)
        if ("reviews".equals(type)) {
            // 🚩 이 부분입니다! findByUserId 대신 getMyReviews를 호출하세요.
            Page<ReviewDTO> reviews = reviewService.getMyReviews(userId, pageable);
            model.addAttribute("reviews", reviews);
        }

        // 만약 게시글(posts) 로직도 있다면 else if로 이어질 것입니다.
        else if ("posts".equals(type)) {
            // 게시글 관련 로직...
        }

        model.addAttribute("type", type);
        return "users/articles"; // 마이페이지 내 활동내역 뷰 이름
    }
}
