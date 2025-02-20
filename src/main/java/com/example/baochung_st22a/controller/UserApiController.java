package com.example.baochung_st22a.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.baochung_st22a.model.UserDtls;
import com.example.baochung_st22a.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 📌 API đăng ký user mới
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDtls user) {
        if (userService.existsEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        userService.saveUser(user);
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    // 📌 API lấy thông tin người dùng hiện tại
    @GetMapping("/profile")
    public ResponseEntity<UserDtls> getUserProfile(Principal principal) {
        UserDtls user = userService.getUserByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }

    // 📌 API cập nhật thông tin cá nhân
    @PutMapping("/profile")
    public ResponseEntity<UserDtls> updateUserProfile(@RequestBody UserDtls user, Principal principal) {
        UserDtls existingUser = userService.getUserByEmail(principal.getName());
        if (existingUser == null) {
            return ResponseEntity.badRequest().build();
        }
        existingUser.setName(user.getName());
        existingUser.setMobileNumber(user.getMobileNumber());
        existingUser.setAddress(user.getAddress());
        existingUser.setCity(user.getCity());
        existingUser.setState(user.getState());
        existingUser.setPincode(user.getPincode());
        return ResponseEntity.ok(userService.updateUser(existingUser));
    }

    // 📌 API cập nhật ảnh đại diện
    @PutMapping("/profile-image")
    public ResponseEntity<String> updateProfileImage(@RequestParam MultipartFile img, Principal principal) {
        UserDtls user = userService.getUserByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại!");
        }
        userService.updateUserProfile(user, img);
        return ResponseEntity.ok("Cập nhật ảnh đại diện thành công!");
    }

    // 📌 API đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Principal principal) {

        UserDtls user = userService.getUserByEmail(principal.getName());
        if (user == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không chính xác!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    // 📌 API đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        if (!userService.existsEmail(email)) {
            return ResponseEntity.badRequest().body("Email không tồn tại!");
        }
        String resetToken = "TOKEN_" + System.currentTimeMillis();
        userService.updateUserResetToken(email, resetToken);
        return ResponseEntity.ok("Mã đặt lại mật khẩu đã được gửi đến email của bạn!");
    }

    // 📌 API xác nhận đặt lại mật khẩu
    @PutMapping("/reset-password-confirm")
    public ResponseEntity<String> confirmResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        UserDtls user = userService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userService.updateUser(user);
        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công!");
    }

}
