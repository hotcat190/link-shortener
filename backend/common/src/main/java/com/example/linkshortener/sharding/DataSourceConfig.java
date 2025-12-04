package com.example.linkshortener.sharding;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for sharded DataSources.
 * Creates two separate DataSources and wraps them in a routing DataSource.
 */
@Configuration
public class DataSourceConfig {

    @Value("${datasource.shard1.url}")
    private String shard1Url;

    @Value("${datasource.shard1.username}")
    private String shard1Username;

    @Value("${datasource.shard1.password}")
    private String shard1Password;

    @Value("${datasource.shard2.url}")
    private String shard2Url;

    @Value("${datasource.shard2.username}")
    private String shard2Username;

    @Value("${datasource.shard2.password}")
    private String shard2Password;

    @Bean
    public DataSource shard1DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(shard1Url);
        dataSource.setUsername(shard1Username);
        dataSource.setPassword(shard1Password);
        dataSource.setPoolName("shard1-pool");
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }

    @Bean
    public DataSource shard2DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(shard2Url);
        dataSource.setUsername(shard2Username);
        dataSource.setPassword(shard2Password);
        dataSource.setPoolName("shard2-pool");
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }

    @Bean
    @Primary
    public DataSource routingDataSource() {
        ShardingDataSourceRouter router = new ShardingDataSourceRouter();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(ClientDatabase.SHARD_1, shard1DataSource());
        targetDataSources.put(ClientDatabase.SHARD_2, shard2DataSource());

        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(shard1DataSource());
        router.afterPropertiesSet();

        return router;
    }
}
