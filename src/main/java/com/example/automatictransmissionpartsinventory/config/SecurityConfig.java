package com.example.automatictransmissionpartsinventory.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定クラス
 * Phase 8.2: ユーザー認証機能の実装
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * パスワードエンコーダーの設定
     * BCryptを使用してパスワードを安全に暗号化
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        // テスト用：平文パスワード
//        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
//    }
    
    /**
     * データベース認証プロバイダーの設定
     * UserDetailsServiceとPasswordEncoderを連携
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * 認証マネージャーの設定
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * セキュリティフィルターチェーンの設定
     * 認証・認可ルールとログイン・ログアウト設定
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 認証プロバイダーの設定
            .authenticationProvider(authenticationProvider())
            
            // CSRF保護の設定（開発段階では無効化、本番では有効推奨）
            .csrf(csrf -> csrf.disable())
            
            // 認可設定：どのURLにアクセス許可するか
            .authorizeHttpRequests(authz -> authz
                // 静的リソース（CSS、JS、画像）は認証不要
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // ログイン・ログアウト関連は認証不要
                .requestMatchers("/login", "/logout").permitAll()
                
                // 管理者専用機能（今後実装予定）
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // CSV機能は管理者のみ
                .requestMatchers("/parts/export/**", "/parts/import/**").hasRole("ADMIN")
                
                // その他全てのURLは認証必要
                .anyRequest().authenticated()
            )
            
            // ログイン設定
            .formLogin(form -> form
                //.loginPage("/login")  // カスタムログイン画面（Step 4で作成予定）
                .defaultSuccessUrl("/parts", true)  // ログイン成功後のリダイレクト先
                .failureUrl("/login?error=true")    // ログイン失敗時のリダイレクト先
                .usernameParameter("username")      // ユーザー名パラメータ名
                .passwordParameter("password")      // パスワードパラメータ名
                .permitAll()
            )
            
            // ログアウト設定
            .logout(logout -> logout
                .logoutUrl("/logout")              // ログアウトURL
                .logoutSuccessUrl("/login?logout") // ログアウト成功後のリダイレクト先
                .invalidateHttpSession(true)      // セッション無効化
                .deleteCookies("JSESSIONID")       // セッションCookie削除
                .permitAll()
            )
            
            // セッション管理
            .sessionManagement(session -> session
                .maximumSessions(1)          // 同時ログイン数制限
                .maxSessionsPreventsLogin(false)  // 既存セッション無効化
            );
            
        return http.build();
    }
}