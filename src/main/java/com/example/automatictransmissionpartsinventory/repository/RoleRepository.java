package com.example.automatictransmissionpartsinventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.automatictransmissionpartsinventory.entity.Role;

/**
 * 権限（ロール）情報に関するデータアクセス層
 * Spring Securityによる認証・認可機能で使用
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 権限名で権限を検索
     * @param roleName 権限名（例: "ROLE_ADMIN", "ROLE_USER"）
     * @return 該当する権限（存在しない場合はOptional.empty()）
     */
    Optional<Role> findByRoleName(String roleName);
    
    /**
     * 権限名の重複チェック
     * @param roleName チェックする権限名
     * @return 既に存在する場合true
     */
    boolean existsByRoleName(String roleName);
}