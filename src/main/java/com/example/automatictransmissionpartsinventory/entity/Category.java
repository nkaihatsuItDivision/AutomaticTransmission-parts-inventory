package com.example.automatictransmissionpartsinventory.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * カテゴリエンティティクラス
 * AT部品の階層構造カテゴリを管理
 * 
 * @author Phase 8.3 Development Team
 * @version 1.0
 * @since 2025-09-05
 */
@Entity
@Table(name = "categories")
@Data
@EqualsAndHashCode(exclude = {"children", "parent", "automotiveParts"})
@ToString(exclude = {"children", "parent", "automotiveParts"})
public class Category {
    
    /**
     * カテゴリID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * カテゴリ名（一意制約）
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * カテゴリ説明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 表示順序
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    /**
     * 有効フラグ
     */
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    /**
     * 作成日時
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新日時
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ========================================
    // 階層構造関連（自己参照）
    // ========================================
    
    /**
     * 親カテゴリ（多対一）
     * 大分類の場合はnull
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    /**
     * 子カテゴリリスト（一対多）
     * 小分類の場合は空リスト
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<Category> children = new ArrayList<>();
    
    // ========================================
    // 部品との関連
    // ========================================
    
    /**
     * このカテゴリに属する部品リスト（一対多）
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<AutomativePart> automotiveParts = new ArrayList<>();
    
    // ========================================
    // JPA ライフサイクルコールバック
    // ========================================
    
    /**
     * エンティティ永続化前の処理
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // デフォルト値設定
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
    
    /**
     * エンティティ更新前の処理
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========================================
    // ビジネスロジック用メソッド
    // ========================================
    
    /**
     * 大分類かどうかを判定
     * @return 大分類の場合true
     */
    public boolean isParentCategory() {
        return this.parent == null;
    }
    
    /**
     * 小分類かどうかを判定
     * @return 小分類の場合true
     */
    public boolean isChildCategory() {
        return this.parent != null;
    }
    
    /**
     * 子カテゴリを持っているかどうかを判定
     * @return 子カテゴリがある場合true
     */
    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }
    
    /**
     * このカテゴリに属する部品があるかどうかを判定
     * @return 部品がある場合true
     */
    public boolean hasParts() {
        return this.automotiveParts != null && !this.automotiveParts.isEmpty();
    }
    
    /**
     * このカテゴリに属する部品数を取得
     * @return 部品数
     */
    public int getPartsCount() {
        return this.automotiveParts != null ? this.automotiveParts.size() : 0;
    }
    
    /**
     * 階層レベルを取得（大分類=0, 小分類=1）
     * @return 階層レベル
     */
    public int getLevel() {
        return this.parent == null ? 0 : 1;
    }
    
    /**
     * フルカテゴリパスを取得（例: "トランスミッション系部品 > ギア類"）
     * @return フルカテゴリパス
     */
    public String getFullPath() {
        if (this.parent == null) {
            return this.name;
        } else {
            return this.parent.getName() + " > " + this.name;
        }
    }
    
    /**
     * 表示用カテゴリ名を取得（階層表示用）
     * @return 表示用カテゴリ名
     */
    public String getDisplayName() {
        if (this.parent == null) {
            return "【大分類】" + this.name;
        } else {
            return "  └ " + this.name;
        }
    }
    
    /**
     * カテゴリが削除可能かどうかを判定
     * 削除不可条件：子カテゴリがある、または部品が割り当てられている
     * @return 削除可能な場合true
     */
    public boolean isDeletable() {
        return !hasChildren() && !hasParts();
    }
    
    /**
     * カテゴリの完全な階層情報を含む文字列表現
     * @return 階層情報を含む文字列
     */
    public String toHierarchyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Category{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", level=").append(getLevel());
        sb.append(", fullPath='").append(getFullPath()).append('\'');
        sb.append(", displayOrder=").append(displayOrder);
        sb.append(", partsCount=").append(getPartsCount());
        sb.append(", hasChildren=").append(hasChildren());
        sb.append(", isActive=").append(isActive);
        sb.append('}');
        return sb.toString();
    }
}