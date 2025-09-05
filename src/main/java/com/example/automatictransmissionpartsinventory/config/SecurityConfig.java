package com.example.automatictransmissionpartsinventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定クラス
 * AT部品在庫管理システム - Phase 8.2-2 完全対応版
 * 
 * セキュリティ設定:
 * - BCryptPasswordEncoder による パスワード暗号化
 * - ロールベースアクセス制御（RBAC）
 * - メソッドレベルセキュリティ（@PreAuthorize有効化）
 * - フォームログイン認証
 * - ログアウト機能
 * 
 * アクセス制御:
 * - /admin/** → ROLE_ADMIN のみ
 * - /parts/export/** → ROLE_ADMIN のみ
 * - /parts/import/** → ROLE_ADMIN のみ
 * - その他 → 認証済みユーザー
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize を有効化（Phase 8.2-2 追加）
public class SecurityConfig {

    /**
     * パスワードエンコーダーのBean定義
     * BCryptアルゴリズムによる安全なパスワードハッシュ化
     * 
     * @return BCryptPasswordEncoder インスタンス
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP セキュリティ設定
     * URL別アクセス制御・認証・ログアウト設定
     * 
     * @param http HttpSecurity インスタンス
     * @return SecurityFilterChain
     * @throws Exception 設定エラー
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // URL別アクセス制御設定
            .authorizeHttpRequests(authz -> authz
                // 静的リソースは認証不要
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // 管理者専用機能 - ROLE_ADMIN のみアクセス可能（Phase 8.2-2 強化）
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // CSV機能 - ROLE_ADMIN のみアクセス可能（Phase 8.2-1 で実装済み）
                .requestMatchers("/parts/export/**").hasRole("ADMIN")
                .requestMatchers("/parts/import/**").hasRole("ADMIN")
                
                // 管理者機能のAPI（将来の拡張用）
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 部品管理機能 - 認証済みユーザーならアクセス可能
                .requestMatchers("/parts/**").authenticated()
                
                // その他すべてのリクエスト - 認証が必要
                .anyRequest().authenticated()
            )
            
            // フォームログイン設定
            .formLogin(form -> form
                // .loginPage("/login") // カスタムログインページは未実装のためコメントアウト
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/parts", true) // ログイン成功後のリダイレクト先
                .failureUrl("/login?error=true") // ログイン失敗時のリダイレクト先
                .usernameParameter("username") // ユーザー名パラメータ名
                .passwordParameter("password") // パスワードパラメータ名
                .permitAll() // ログインページは誰でもアクセス可能
            )
            
            // ログアウト設定
            .logout(logout -> logout
                .logoutUrl("/logout") // ログアウトURL
                .logoutSuccessUrl("/login?logout=true") // ログアウト成功後のリダイレクト先
                .invalidateHttpSession(true) // セッション無効化
                .deleteCookies("JSESSIONID") // セッションCookie削除
                .clearAuthentication(true) // 認証情報クリア
                .permitAll() // ログアウトは誰でも実行可能
            )
            
            // セッション管理設定
            .sessionManagement(session -> session
                .maximumSessions(1) // 同時セッション数制限
                .maxSessionsPreventsLogin(false) // 新しいログインが古いセッションを無効化
            )
            
            // CSRF設定（開発時は無効化、本番では有効化推奨）
            .csrf(csrf -> csrf.disable())
            
            // セキュリティヘッダー設定
            .headers(headers -> headers
                .frameOptions().deny() // Clickjacking対策
                .contentTypeOptions().and() // MIME sniffing対策
                .httpStrictTransportSecurity().and() // HTTPS強制（本番環境）
            );

        return http.build();
    }
}

    /**
     * 権限設定の詳細説明
     * 
     * ROLE_ADMIN:
     * - 全機能へのアクセス権限
     * - 管理者ダッシュボード（/admin）
     * - ユーザー管理（/admin/users）
     * - CSV インポート・エクスポート
     * - システム設定（/admin/settings）
     * 
     * ROLE_USER:
     * - 基本的な部品管理機能のみ
     * - 部品一覧・詳細表示
     * - 部品検索
     * - 管理者機能への アクセス拒否（403 Forbidden）
     * 
     * @PreAuthorize アノテーション:
     * - AdminController で使用
     * - @EnableMethodSecurity で有効化
     * - メソッドレベルでの詳細なアクセス制御
     */