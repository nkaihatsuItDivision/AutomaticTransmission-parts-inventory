package com.example.automatictransmissionpartsinventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.automatictransmissionpartsinventory.entity.User;

/**
 * ユーザー情報に関するデータアクセス層
 * Spring Securityによる認証・認可機能で使用
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * ユーザー名でユーザーを検索（Spring Security認証で使用）
     * @param username ユーザー名
     * @return 該当するユーザー（存在しない場合はOptional.empty()）
     */
    Optional<User> findByUsername(String username);
    
    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return 該当するユーザー（存在しない場合はOptional.empty()）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * ユーザー名の重複チェック
     * @param username チェックするユーザー名
     * @return 既に存在する場合true
     */
    boolean existsByUsername(String username);
    
    /**
     * メールアドレスの重複チェック
     * @param email チェックするメールアドレス
     * @return 既に存在する場合true
     */
    boolean existsByEmail(String email);
    
    /**
     * 有効なユーザーのみを取得
     * @return 有効なユーザーのリスト
     */
    List<User> findByEnabledTrue();
    
    /**
     * 無効なユーザーのみを取得
     * @return 無効なユーザーのリスト
     */
    List<User> findByEnabledFalse();
    
    
// // 権限別ユーザー数カウント用メソッドを追加
//    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
//    long countByRoleName(@Param("roleName") String roleName);
    /**
     * 
     * 
     * 指定した権限を持つユーザーを検索
     * @param roleName 権限名（例: "ROLE_ADMIN"）
     * @return 該当する権限を持つユーザーのリスト
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * ユーザー名またはフルネームで部分一致検索
     * @param searchTerm 検索文字列
     * @return 該当するユーザーのリスト
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.fullName LIKE %:searchTerm%")
    List<User> findByUsernameContainingOrFullNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * 管理者権限を持つユーザーを取得
     * @return 管理者ユーザーのリスト
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = 'ROLE_ADMIN'")
    List<User> findAdminUsers();
    
    /**
     * 一般ユーザー権限を持つユーザーを取得
     * @return 一般ユーザーのリスト
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = 'ROLE_USER'")
    List<User> findRegularUsers();
    

/**
     * 指定した権限を持つユーザーの数をカウント（Phase 8.2-2 追加）
     * 管理者ダッシュボードの統計表示で使用
     * @param roleName 権限名（例: "ROLE_ADMIN", "ROLE_USER"）
     * @return 該当する権限を持つユーザー数
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
    
    /**
     * 有効な管理者ユーザー数をカウント
     * （最後の管理者削除/無効化防止用）
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.roleName = :roleName AND u.enabled = true")
    long countByRoleNameAndEnabledTrue(@Param("roleName") String roleName);
    
    /**
     * 指定したIDを除いてユーザー名の重複チェック
     * （編集時の重複チェック用）
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);
    
    /**
     * 指定したIDを除いてメールアドレスの重複チェック
     * （編集時の重複チェック用）
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    /**
     * 権限名と有効状態でユーザーを検索
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName AND u.enabled = :enabled")
    List<User> findByRoleNameAndEnabled(@Param("roleName") String roleName, @Param("enabled") boolean enabled);
    
    /**
     * ユーザー名やメールアドレスで部分一致検索
     * （将来的な検索機能用）
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
    List<User> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 権限名でユーザーをページネーション付きで取得
     * （将来的なページング機能用）
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName ORDER BY u.id DESC")
    List<User> findByRoleNameOrderByIdDesc(@Param("roleName") String roleName);
}