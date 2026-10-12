package com.financetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * Configures pagination enforcement for all list endpoints.
 * Enforces a maximum page size of 25 and a default page size of 20.
 */
@Configuration
public class PaginationConfig implements WebMvcConfigurer {

    /**
     * Registers a customized PageableHandlerMethodArgumentResolver that enforces
     * maximum page size of 25 and default page size of 20.
     *
     * @param resolvers the list of argument resolvers to add to
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        var pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(25);
        pageableResolver.setFallbackPageable(PageRequest.of(0, 20));
        resolvers.add(pageableResolver);
    }
}
