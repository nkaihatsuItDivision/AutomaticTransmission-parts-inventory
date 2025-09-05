package com.example.automatictransmissionpartsinventory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.automatictransmissionpartsinventory.entity.User;
import com.example.automatictransmissionpartsinventory.repository.AutomaticPartRepository;
import com.example.automatictransmissionpartsinventory.repository.UserRepository;

/**
 * 管理者専用機能制御コントローラー
 * AT部品在庫管理システム - Phase 8.2-2
 * 
 * 機能:
 * - 管理者ダッシュボード表示
 * - ユーザー管理機能
 * - システム統計情報表示
 * 
 * アクセス制御: ROLE_ADMIN のみ
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AutomaticPartRepository automaticPartRepository;

    /**
     * 管理者ダッシュボード表示
     * URL: /admin
     * 機能: システム統計情報とメニューを表示
     */
    @GetMapping
    public String dashboard(Model model) {
        try {
            // システム統計情報の取得
            long totalParts = automaticPartRepository.count();
            long totalUsers = userRepository.count();
            long adminUsers = userRepository.countByRoleName("ROLE_ADMIN");
            long regularUsers = userRepository.countByRoleName("ROLE_USER");
            
            // モデルにデータを追加
            model.addAttribute("totalParts", totalParts);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("adminUsers", adminUsers);
            model.addAttribute("regularUsers", regularUsers);
            model.addAttribute("pageTitle", "管理者ダッシュボード");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            // エラーハンドリング
            model.addAttribute("errorMessage", "ダッシュボードデータの取得に失敗しました");
            return "admin/dashboard";
        }
    }

    /**
     * ユーザー管理画面表示
     * URL: /admin/users
     * 機能: 全ユーザー一覧と権限情報を表示
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        try {
            // 全ユーザー情報の取得
            List<User> allUsers = userRepository.findAll();
            
            // 権限別ユーザー情報の取得
            List<User> adminUsers = userRepository.findByRoleName("ROLE_ADMIN");
            List<User> regularUsers = userRepository.findByRoleName("ROLE_USER");
            
            // モデルにデータを追加
            model.addAttribute("allUsers", allUsers);
            model.addAttribute("adminUsers", adminUsers);
            model.addAttribute("regularUsers", regularUsers);
            model.addAttribute("pageTitle", "ユーザー管理");
            
            return "admin/users";
            
        } catch (Exception e) {
            // エラーハンドリング
            model.addAttribute("errorMessage", "ユーザー情報の取得に失敗しました");
            return "admin/users";
        }
    }

    /**
     * システム設定画面表示（拡張予定）
     * URL: /admin/settings
     * 機能: システム設定・環境設定表示
     */
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("pageTitle", "システム設定");
        return "admin/settings";
    }
}