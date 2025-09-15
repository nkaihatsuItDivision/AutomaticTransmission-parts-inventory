package com.example.automatictransmissionpartsinventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.automatictransmissionpartsinventory.entity.Role;

/**
 * Role（権限）エンティティのRepositoryインターフェース
 * AT部品在庫管理システム - 認証・権限管理
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 権限名でRoleを検索
     * @param roleName 権限名（例：ROLE_ADMIN, ROLE_USER）
     * @return 見つかったRole（Optional）
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * すべてのRoleを取得（権限選択用）
     * @return 全権限のリスト
     */
    List<Role> findAll();

    /**
     * 権限名の存在チェック
     * @param roleName チェックする権限名
     * @return 存在する場合true
     */
    boolean existsByRoleName(String roleName);
}