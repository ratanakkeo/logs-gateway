package com.bank.observability.logsgateway.shared.web;

import com.bank.observability.logsgateway.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RateLimitFilterTest {

    @Test
    void returns429WithRetryAfterWhenCapacityExceeded() throws Exception {
        SecurityProperties properties = new SecurityProperties();
        properties.getRateLimit().setCapacity(1);
        properties.getRateLimit().setRefillTokens(1);
        properties.getRateLimit().setRefillDuration(Duration.ofMinutes(1));
        RateLimitFilter filter = new RateLimitFilter(properties);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest first = new MockHttpServletRequest("POST", "/v1/logs/ingest");
        first.addHeader(ApiKeyAuthFilter.API_KEY_HEADER, "k1");
        filter.doFilter(first, new MockHttpServletResponse(), chain);

        MockHttpServletRequest second = new MockHttpServletRequest("POST", "/v1/logs/ingest");
        second.addHeader(ApiKeyAuthFilter.API_KEY_HEADER, "k1");
        MockHttpServletResponse limited = new MockHttpServletResponse();
        filter.doFilter(second, limited, chain);

        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(limited.getHeader("Retry-After")).isNotBlank();
    }
}
