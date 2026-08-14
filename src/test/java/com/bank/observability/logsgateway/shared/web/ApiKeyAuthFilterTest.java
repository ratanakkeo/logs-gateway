package com.bank.observability.logsgateway.shared.web;

import com.bank.observability.logsgateway.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ApiKeyAuthFilterTest {

    @Test
    void rejectsMissingApiKeyWith401WithoutReadingBody() throws Exception {
        SecurityProperties properties = new SecurityProperties();
        properties.setApiKeys(List.of("secret-key"));
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/logs/ingest");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsConfiguredApiKey() throws Exception {
        SecurityProperties properties = new SecurityProperties();
        properties.setApiKeys(List.of("secret-key"));
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/logs/ingest");
        request.addHeader(ApiKeyAuthFilter.API_KEY_HEADER, "secret-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
