package com.trading.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    @SuppressWarnings("null")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            TenantContext.setCurrentTenant("master");
        }
        return true;
    }

    @Override
    @SuppressWarnings("null")
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
