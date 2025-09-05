package com.example.automatictransmissionpartsinventory.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.automatictransmissionpartsinventory.entity.User;
import com.example.automatictransmissionpartsinventory.repository.UserRepository;

/**
 * Spring Security認証用のUserDetailsService実装
 * データベースからユーザー情報を取得してSpring Securityに提供
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * ユーザー名を使ってユーザー詳細情報を取得
     * Spring Securityによって認証時に自動的に呼び出される
     * 
     * @param username ログイン画面で入力されたユーザー名
     * @return UserDetails Spring Security用のユーザー詳細情報
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // データベースからユーザーを検索
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "ユーザーが見つかりません: " + username));
        
        // ユーザーの権限情報を取得してGrantedAuthorityに変換
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toSet());
        
        // Spring SecurityのUserDetailsを返す
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.getAccountNonExpired())
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(!user.getCredentialsNonExpired())
                .disabled(!user.getEnabled())
                .build();
    }
}