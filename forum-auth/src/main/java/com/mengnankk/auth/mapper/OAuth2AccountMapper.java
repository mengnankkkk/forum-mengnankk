package com.mengnankk.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengnankk.auth.entity.OAuth2Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * OAuth2账号数据访问层
 */
@Mapper
public interface OAuth2AccountMapper extends BaseMapper<OAuth2Account> {

    /**
     * 根据用户ID和提供者查询OAuth2账号
     */
    @Select("SELECT * FROM oauth2_accounts WHERE user_id = #{userId} AND provider = #{provider}")
    OAuth2Account findByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 根据提供者和提供者用户ID查询OAuth2账号
     */
    @Select("SELECT * FROM oauth2_accounts WHERE provider = #{provider} AND provider_user_id = #{providerUserId}")
    OAuth2Account findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);

    /**
     * 根据用户ID查询所有OAuth2账号
     */
    @Select("SELECT * FROM oauth2_accounts WHERE user_id = #{userId} ORDER BY created_time DESC")
    List<OAuth2Account> findByUserId(@Param("userId") Long userId);
}
