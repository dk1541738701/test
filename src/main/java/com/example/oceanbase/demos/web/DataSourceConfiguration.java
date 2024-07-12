//package com.example.oceanbase.demos.web;
//
//import com.alibaba.druid.pool.DruidDataSource;
//import com.taobao.txc.datasource.cobar.TxcDataSource;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class DataSourceConfiguration {
//
//    @Bean
//    @ConfigurationProperties(prefix = "my.datasource.a")
//    public DruidDataSource dataSourceA() {
//        System.out.println("初始化数据源A......");
//        DruidDataSource druidDataSource = new DruidDataSource();
//        return druidDataSource;
//    }
//
//    @Bean
//    public TxcDataSource dataSourceProxyA(DruidDataSource dataSourceA) {
//        // 用 TxcDataSource 包装代理我的数据源 A
//        System.out.println("将数据源包装为GTS数据源.....");
//        return new TxcDataSource(dataSourceA);
//    }
//
//
//
//
//
//}
