package com.example.automatictransmissionpartsinventory.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 権限（ロール）エンティティクラス
 * Spring Securityによる認証・認可機能で使用
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "users") // 循環参照を防ぐ
@ToString(exclude = "users") // 循環参照を防ぐ
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "権限名は必須です")
    @Size(max = 50, message = "権限名は50文字以下で入力してください")
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;
    
    @Size(max = 255, message = "説明は255文字以下で入力してください")
    @Column(length = 255)
    private String description;
    
    // ユーザーとの多対多関係（逆参照）
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
    
    // タイムスタンプ
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * エンティティ保存前の自動実行メソッド
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * 管理者権限かどうかをチェック
     * @return 管理者権限の場合true
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(roleName);
    }
    
    /**
     * 一般ユーザー権限かどうかをチェック
     * @return 一般ユーザー権限の場合true
     */
    public boolean isUser() {
        return "ROLE_USER".equals(roleName);
    }
    
    /**
     * 権限名を表示用に変換
     * @return 表示用権限名
     */
    public String getDisplayName() {
        switch (roleName) {
            case "ROLE_ADMIN":
                return "管理者";
            case "ROLE_USER":
                return "一般ユーザー";
            default:
                return roleName;
        }
    }
}