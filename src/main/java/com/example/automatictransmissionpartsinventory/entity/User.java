package com.example.automatictransmissionpartsinventory.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザー情報エンティティクラス
 * Spring Securityによる認証・認可機能で使用
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以下で入力してください")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "パスワードは必須です")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレスを入力してください")
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "氏名は必須です")
    @Size(max = 100, message = "氏名は100文字以下で入力してください")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    // Spring Security用のアカウント状態管理フィールド
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;
    
    // ロール（権限）との多対多関係
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // タイムスタンプ
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * エンティティ保存前の自動実行メソッド
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * エンティティ更新前の自動実行メソッド
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 権限追加メソッド
     * @param role 追加する権限
     */
    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
    
    /**
     * 権限削除メソッド
     * @param role 削除する権限
     */
    public void removeRole(Role role) {
        if (roles != null) {
            roles.remove(role);
        }
    }
    
    /**
     * 指定した権限を持っているかチェック
     * @param roleName 権限名（例: "ROLE_ADMIN"）
     * @return 権限を持っている場合true
     */
    public boolean hasRole(String roleName) {
        return roles != null && roles.stream()
                .anyMatch(role -> roleName.equals(role.getRoleName()));
    }
}