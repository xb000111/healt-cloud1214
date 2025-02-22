package com.healt.cloud.checkup.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author GuYue
 * @date 2022-02-21 9:52
 * @description: TODO
 */
@Configuration
@MapperScan("com.healt.cloud.checkup.mapper")
public class CheckupServiceConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.ORACLE);
        paginationInnerInterceptor.setOptimizeJoin(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(500L);
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
