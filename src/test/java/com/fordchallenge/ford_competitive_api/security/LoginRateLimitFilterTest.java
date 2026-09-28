package com.fordchallenge.ford_competitive_api.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimitFilterTest {

    @ParameterizedTest
    @CsvSource(value = {"'', ''", "'', /auth/login", "/api, ''", "/api, /auth/login"})
    void deveLimitarLoginComOuSemContextPathEServletPath(String contextPath, String servletPath)
            throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();
        for (int attempt = 1; attempt <= 11; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", contextPath + "/auth/login");
            request.setContextPath(contextPath);
            request.setServletPath(servletPath);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(401));

            assertThat(response.getStatus()).isEqualTo(attempt <= 10 ? 401 : 429);
            if (attempt == 11) {
                assertThat(response.getHeader("Retry-After")).isNotBlank();
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/auth/login", "PUT, /api/auth/login", "OPTIONS, /api/auth/login",
            "POST, /api/auth/register", "POST, /api/auth/login/", "POST, /api/auth/login/extra",
            "POST, /api/other/auth/login"
    })
    void deveIgnorarOutrosMetodosERotas(String method, String uri) throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();
        for (int attempt = 1; attempt <= 11; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
            request.setContextPath("/api");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(204));

            assertThat(response.getStatus()).isEqualTo(204);
        }
    }
}
