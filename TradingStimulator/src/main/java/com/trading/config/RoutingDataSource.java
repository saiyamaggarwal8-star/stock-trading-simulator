package com.trading.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import java.util.Map;
import java.util.HashMap;

public class RoutingDataSource extends AbstractRoutingDataSource {
    private final Map<Object, Object> targetDataSources = new HashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        this.targetDataSources.putAll(targetDataSources);
    }

    public void addDataSource(String tenantId, Object dataSource) {
        this.targetDataSources.put(tenantId, dataSource);
        super.setTargetDataSources(new HashMap<>(this.targetDataSources));
        super.afterPropertiesSet();
    }

    public boolean hasDataSource(String tenantId) {
        return this.targetDataSources.containsKey(tenantId);
    }
}
