package com.example.automatictransmissionpartsinventory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("元のパスワード: " + rawPassword);
        System.out.println("BCryptハッシュ: " + encodedPassword);
        System.out.println("検証結果: " + encoder.matches(rawPassword, encodedPassword));
        
        // 2つ目のハッシュも生成（毎回異なるハッシュが生成される）
        String encodedPassword2 = encoder.encode(rawPassword);
        System.out.println("別のハッシュ: " + encodedPassword2);
        System.out.println("検証結果2: " + encoder.matches(rawPassword, encodedPassword2));
    }
}