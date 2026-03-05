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
    public String login(@RequestParam(value = "prevPage", required = false) String prevPage,
                        HttpServletRequest request, Model model) {
        // 세션이 없을 수도 있으므로 false 로 호출
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 2. Handler에서 세션에 저장한 에러 메시지를 꺼냄
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                // 3. HTML(Thymeleaf)에서 찾고 있는 키값인 'error'로 Model에 담아 전달
                model.addAttribute("error", errorMessage);

                // 4. 메시지를 한 번 보여준 후 세션에서 제거 (새로고침 시 계속 뜨는 현상 방지)
                session.removeAttribute("errorMessage");
            }
        }

        // 값 저장을 위해 세션 강제 호출 (없으면 생성)
        session = request.getSession(true);

        // 시큐리티가 강제로 로그인창으로 보낸 경우
        if (session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
            // prevPage를 비워둠
            session.removeAttribute("prevPage");
        } else {
            // 좋아요 싫어요 버튼을 눌러서 로그인창으로 넘어온 경우
            if (prevPage != null && !prevPage.isEmpty()) {
                // 세션에 prevPage 라는 이름으로 직전 페이지를 저장 (OAuth2 인증 후에도 유지됨)
                session.setAttribute("prevPage", prevPage);
            } else {
                // 헤더의 일반 로그인 버튼을 눌러 파라미터 없이 접근한 경우
                // 브라우저가 제공하는 '이전 페이지 주소(Referer)'를 꺼내서 확인
                String referer = request.getHeader("Referer");

                // 이전 페이지가 존재하고, 로그인 실패로 인한 새로고침이 아니며, 회원가입/비밀번호 찾기 페이지가 아닐 때만 저장
                if (referer != null && !referer.contains("/login") && !referer.contains("/signUp") && !referer.contains("/users/find")) {
                    session.setAttribute("prevPage", referer);
                }
            }
        }

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
                if (current_password == null || current_password.isEmpty()) {
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

}
