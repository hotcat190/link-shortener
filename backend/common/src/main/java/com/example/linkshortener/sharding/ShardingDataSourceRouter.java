package com.example.linkshortener.sharding;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Custom DataSource router that determines the target database
 * based on the sharding key stored in ShardingContext.
 */
public class ShardingDataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return ShardingContext.determineTargetShard();
    }
}
