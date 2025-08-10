package com.mengnankk.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengnankk.auth.entity.OAuth2Account;
import com.mengnankk.auth.mapper.OAuth2AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2账号服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AccountService extends ServiceImpl<OAuth2AccountMapper, OAuth2Account> {

    /**
     * 根据提供者和提供者用户ID查找OAuth2账号
     */
    public OAuth2Account findByProviderAndProviderUserId(String provider, String providerUserId) {
        QueryWrapper<OAuth2Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("provider", provider)
                   .eq("provider_user_id", providerUserId);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据用户ID查找OAuth2账号列表
     */
    public List<OAuth2Account> findByUserId(Long userId) {
        QueryWrapper<OAuth2Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_time");
        return this.list(queryWrapper);
    }

    /**
     * 根据用户ID和提供者查找OAuth2账号
     */
    public OAuth2Account findByUserIdAndProvider(Long userId, String provider) {
        QueryWrapper<OAuth2Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("provider", provider);
        return this.getOne(queryWrapper);
    }

    /**
     * 绑定OAuth2账号到用户
     */
    @Transactional(rollbackFor = Exception.class)
    public OAuth2Account bindToUser(Long userId, OAuth2Account oAuth2Account) {
        // 检查是否已经绑定了相同的OAuth2账号
        OAuth2Account existing = findByProviderAndProviderUserId(
                oAuth2Account.getProvider(), 
                oAuth2Account.getProviderUserId());
        
        if (existing != null) {
            throw new RuntimeException("该OAuth2账号已被其他用户绑定");
        }
        
        // 检查用户是否已经绑定了相同提供者的账号
        OAuth2Account userBinding = findByUserIdAndProvider(userId, oAuth2Account.getProvider());
        if (userBinding != null) {
            throw new RuntimeException("用户已绑定该提供者的账号");
        }
        
        oAuth2Account.setUserId(userId);
        oAuth2Account.setCreatedTime(LocalDateTime.now());
        oAuth2Account.setUpdatedTime(LocalDateTime.now());
        
        this.save(oAuth2Account);
        log.info("OAuth2账号绑定成功: userId={}, provider={}", userId, oAuth2Account.getProvider());
        
        return oAuth2Account;
    }

    /**
     * 解绑OAuth2账号
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindFromUser(Long userId, String provider) {
        OAuth2Account oAuth2Account = findByUserIdAndProvider(userId, provider);
        if (oAuth2Account != null) {
            this.removeById(oAuth2Account.getId());
            log.info("OAuth2账号解绑成功: userId={}, provider={}", userId, provider);
        }
    }

    /**
     * 更新OAuth2账号的Token信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTokenInfo(Long id, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        OAuth2Account oAuth2Account = this.getById(id);
        if (oAuth2Account != null) {
            oAuth2Account.setAccessToken(accessToken);
            oAuth2Account.setRefreshToken(refreshToken);
            oAuth2Account.setExpiresAt(expiresAt);
            oAuth2Account.setUpdatedTime(LocalDateTime.now());
            
            this.updateById(oAuth2Account);
            log.debug("OAuth2账号Token更新成功: id={}", id);
        }
    }

    /**
     * 获取过期的OAuth2账号列表
     */
    public List<OAuth2Account> findExpiredAccounts() {
        QueryWrapper<OAuth2Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("expires_at")
                   .lt("expires_at", LocalDateTime.now());
        return this.list(queryWrapper);
    }

    /**
     * 清理过期的Token信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredTokens() {
        List<OAuth2Account> expiredAccounts = findExpiredAccounts();
        for (OAuth2Account account : expiredAccounts) {
            account.setAccessToken(null);
            account.setRefreshToken(null);
            account.setExpiresAt(null);
            account.setUpdatedTime(LocalDateTime.now());
            this.updateById(account);
        }
        
        if (!expiredAccounts.isEmpty()) {
            log.info("清理过期OAuth2 Token完成: count={}", expiredAccounts.size());
        }
    }
}
