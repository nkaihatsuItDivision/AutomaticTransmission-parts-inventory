package com.example.automatictransmissionpartsinventory.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "automotive_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomativePart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "部品番号は必須です")
    @Column(name = "part_number", unique = true, nullable = false)
    private String partNumber;
    
    @NotBlank(message = "部品名は必須です")
    @Column(name = "part_name", nullable = false)
    private String partName;
    
    @NotNull(message = "価格は必須です")
    @DecimalMin(value = "0.0", message = "価格は0以上である必要があります")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    /**
     * カテゴリ（多対一）
     * AT部品の分類カテゴリ
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "manufacturer")
    private String manufacturer;
   
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    /**
     * カテゴリが設定されているかどうかを判定
     * @return カテゴリが設定されている場合true
     */
    public boolean hasCategory() {
        return this.category != null;
    }
    
    /**
     * カテゴリ名を取得（カテゴリが未設定の場合は"未分類"を返す）
     * @return カテゴリ名
     */
    public String getCategoryName() {
        return this.category != null ? this.category.getName() : "未分類";
    }
    
    /**
     * フルカテゴリパスを取得（例: "トランスミッション系部品 > ギア類"）
     * @return フルカテゴリパス
     */
    public String getCategoryFullPath() {
        return this.category != null ? this.category.getFullPath() : "未分類";
    }
    
    /**
     * 大分類名を取得
     * @return 大分類名
     */
    public String getParentCategoryName() {
        if (this.category == null) {
            return "未分類";
        }
        
        if (this.category.isParentCategory()) {
            return this.category.getName();
        } else {
            return this.category.getParent() != null ? 
                   this.category.getParent().getName() : "未分類";
        }
    }
    
    /**
     * カテゴリ情報を含む文字列表現
     * @return カテゴリ情報を含む文字列
     */
    public String toStringWithCategory() {
        StringBuilder sb = new StringBuilder();
        sb.append("AutomativePart{");
        sb.append("id=").append(id);
        sb.append(", partNumber='").append(partNumber).append('\'');
        sb.append(", partName='").append(partName).append('\'');
        sb.append(", manufacturer='").append(manufacturer).append('\'');
        sb.append(", price=").append(price);
        sb.append(", category='").append(getCategoryFullPath()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
