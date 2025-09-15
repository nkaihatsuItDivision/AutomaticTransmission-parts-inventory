package com.example.automatictransmissionpartsinventory.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.automatictransmissionpartsinventory.entity.Role;
import com.example.automatictransmissionpartsinventory.entity.User;
import com.example.automatictransmissionpartsinventory.repository.AutomaticPartRepository;
import com.example.automatictransmissionpartsinventory.repository.RoleRepository;
import com.example.automatictransmissionpartsinventory.repository.UserRepository;

/**
 * 管理者専用機能制御コントローラー
 * AT部品在庫管理システム - Phase 2完成版
 * 
 * 機能:
 * - 管理者ダッシュボード表示
 * - ユーザー管理機能（CRUD完全対応）
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
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            List<User> users = userRepository.findAll();
            
            // 権限別ユーザー情報の取得
            List<User> adminUsers = userRepository.findByRoleName("ROLE_ADMIN");
            List<User> regularUsers = userRepository.findByRoleName("ROLE_USER");
            
            // モデルにデータを追加
            model.addAttribute("users", users);
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

    /**
     * 新規ユーザー登録画面表示
     * URL: /admin/users/new
     * 機能: 新規ユーザー登録フォームを表示
     */
    @GetMapping("/users/new")
    public String showUserRegistrationForm(Model model) {
        try {
            // 空のUserオブジェクトをフォームバインディング用に追加
            model.addAttribute("user", new User());
            
            // 選択可能な権限（ロール）一覧を取得
            List<Role> availableRoles = roleRepository.findAll();
            model.addAttribute("availableRoles", availableRoles);
            
            model.addAttribute("pageTitle", "新規ユーザー登録");
            
            return "admin/user-form";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "登録フォームの表示に失敗しました");
            return "admin/users";
        }
    }

    /**
     * 新規ユーザー登録処理
     * URL: POST /admin/users
     * 機能: 新規ユーザーをデータベースに保存
     */
    @PostMapping("/users")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                              BindingResult bindingResult,
                              @RequestParam("roleId") Long roleId,
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        try {
            // バリデーションエラーチェック
            if (bindingResult.hasErrors()) {
                // エラーがある場合は登録フォームに戻る
                List<Role> availableRoles = roleRepository.findAll();
                model.addAttribute("availableRoles", availableRoles);
                model.addAttribute("pageTitle", "新規ユーザー登録");
                return "admin/user-form";
            }
            
            // ユーザー名重複チェック
            if (userRepository.existsByUsername(user.getUsername())) {
                model.addAttribute("errorMessage", "このユーザー名は既に使用されています");
                List<Role> availableRoles = roleRepository.findAll();
                model.addAttribute("availableRoles", availableRoles);
                model.addAttribute("pageTitle", "新規ユーザー登録");
                return "admin/user-form";
            }
            
            // メールアドレス重複チェック
            if (userRepository.existsByEmail(user.getEmail())) {
                model.addAttribute("errorMessage", "このメールアドレスは既に使用されています");
                List<Role> availableRoles = roleRepository.findAll();
                model.addAttribute("availableRoles", availableRoles);
                model.addAttribute("pageTitle", "新規ユーザー登録");
                return "admin/user-form";
            }
            
            // パスワードをBCryptで暗号化
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // ユーザーをデータベースに保存
            User savedUser = userRepository.save(user);
            
            // 選択された権限をユーザーに関連付け
            Role selectedRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("選択された権限が見つかりません"));
            savedUser.getRoles().add(selectedRole);
            userRepository.save(savedUser);
            
            // 成功メッセージと共にユーザー管理画面にリダイレクト
            redirectAttributes.addFlashAttribute("successMessage", 
                "ユーザー「" + user.getUsername() + "」を正常に登録しました");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            // エラーハンドリング
            model.addAttribute("errorMessage", "ユーザー登録に失敗しました: " + e.getMessage());
            List<Role> availableRoles = roleRepository.findAll();
            model.addAttribute("availableRoles", availableRoles);
            model.addAttribute("pageTitle", "新規ユーザー登録");
            return "admin/user-form";
        }
    }

    /**
     * 既存ユーザー編集画面表示
     * URL: GET /admin/users/{id}/edit
     * 機能: 指定ユーザーの編集フォームを表示
     */
    @GetMapping("/users/{id}/edit")
    public String showUserEditForm(@PathVariable("id") Long id, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        try {
            // 指定IDのユーザー取得
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません (ID: " + id + ")"));
            
            // モデルに必要なデータ設定
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            
            // 現在の権限情報を取得
            String currentRole = user.getRoles().isEmpty() ? "" : 
                               user.getRoles().iterator().next().getRoleName();
            model.addAttribute("currentRole", currentRole);
            model.addAttribute("pageTitle", "ユーザー編集");
            
            return "admin/user-edit-form";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "ユーザー情報の取得に失敗しました: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    /**
     * 既存ユーザー情報更新処理（パスワード変更対応完全版）
     * URL: POST /admin/users/{id}
     * 機能: ユーザー情報の更新（基本情報 + オプションのパスワード変更）
     */
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable("id") Long id,
                            @RequestParam("username") String username,
                            @RequestParam("email") String email,
                            @RequestParam("fullName") String fullName,
                            @RequestParam("roleName") String roleName,
                            @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
                            @RequestParam(value = "password", required = false) String password,
                            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                            RedirectAttributes redirectAttributes) {
        try {
            // 既存ユーザー取得
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません (ID: " + id + ")"));
            
            // ユーザー名の重複チェック（自分以外）
            if (userRepository.existsByUsernameAndIdNot(username, id)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "このユーザー名は他のユーザーによって既に使用されています: " + username);
                return "redirect:/admin/users/" + id + "/edit";
            }
            
            // メールアドレスの重複チェック（自分以外）
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "このメールアドレスは他のユーザーによって既に使用されています: " + email);
                return "redirect:/admin/users/" + id + "/edit";
            }
            
            // パスワード変更処理
            if (password != null && !password.trim().isEmpty()) {
                // パスワード確認チェック
                if (confirmPassword == null || !password.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "パスワードと確認パスワードが一致しません");
                    return "redirect:/admin/users/" + id + "/edit";
                }
                
                // パスワード長さチェック
                if (password.length() < 6) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "パスワードは6文字以上で入力してください");
                    return "redirect:/admin/users/" + id + "/edit";
                }
                
                // パスワードをBCryptで暗号化して更新
                user.setPassword(passwordEncoder.encode(password));
            }
            
            // 基本情報更新
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setEnabled(enabled);
            
            // 権限更新
            Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("権限が見つかりません: " + roleName));
            
            user.getRoles().clear();
            user.getRoles().add(role);
            
            // データベース保存
            userRepository.save(user);
            
            // 成功メッセージ
            String successMsg = "ユーザー「" + user.getFullName() + "」の情報を更新しました";
            if (password != null && !password.trim().isEmpty()) {
                successMsg += "（パスワードも変更されました）";
            }
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "更新に失敗しました: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }
    
    /**
     * ユーザー削除処理
     * URL: POST /admin/users/{id}/delete
     * 機能: 指定ユーザーの削除（ハードデリート）
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, 
                            RedirectAttributes redirectAttributes) {
        try {
            // 削除対象ユーザー取得
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません (ID: " + id + ")"));
            
            // 管理者の削除制限チェック（最後の管理者削除防止）
            if (user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()))) {
                long adminCount = userRepository.countByRoleName("ROLE_ADMIN");
                if (adminCount <= 1) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "最後の管理者ユーザーは削除できません");
                    return "redirect:/admin/users";
                }
            }
            
            // ユーザー削除（ハードデリート）
            userRepository.delete(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "ユーザー「" + user.getFullName() + "」を削除しました");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "ユーザーの削除に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}