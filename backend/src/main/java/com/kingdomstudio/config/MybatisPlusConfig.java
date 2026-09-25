package com.kingdomstudio.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页插件 + 全表更新/删除防护。
 */
@Configuration
public class MybatisPlusConfig {

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 分页插件
		PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
		pagination.setMaxLimit(100L);       // 单页最多 100 条，防止前端传超大 size
		pagination.setOverflow(false);      // 页码越界时返回空列表，而不是回到第一页
		interceptor.addInnerInterceptor(pagination);

		// 防止误写的全表 update/delete
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

		return interceptor;
	}
}
