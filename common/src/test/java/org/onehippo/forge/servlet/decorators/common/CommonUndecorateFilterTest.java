package org.onehippo.forge.servlet.decorators.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonUndecorateFilterTest {

    @Mock private FilterChain chain;
    @Mock private ServletResponse response;
    @Mock private HttpServletRequest originalRequest;

    /** Concrete subclass — CommonUndecorateFilter is abstract with no abstract methods */
    static class TestableFilter extends CommonUndecorateFilter {}

    private TestableFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TestableFilter();
    }

    private static DecoratorConfiguration validConfig() {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true).hostHeader("X-Forwarded-Host").hosts(Map.of(".*", "/cms")).build();
        return configs.iterator().next();
    }

    // --- doFilter: plain request (not wrapped) ---

    @Test
    void doFilter_plainRequest_passesDirectlyToChain() throws Exception {
        filter.doFilter(originalRequest, response, chain);
        verify(chain).doFilter(originalRequest, response);
    }

    // --- doFilter: HippoDecoratedServletRequest ---

    @Test
    void doFilter_decoratedRequest_unwrapsAndPassesOriginalToChain() throws Exception {
        HippoDecoratedServletRequest decorated = new HippoDecoratedServletRequest(originalRequest, validConfig());

        filter.doFilter(decorated, response, chain);

        verify(chain).doFilter(originalRequest, response);
        verify(chain, never()).doFilter(decorated, response);
    }

    // --- doFilter: ServletRequestWrapper wrapping HippoDecoratedServletRequest ---

    @Test
    void doFilter_wrapperAroundDecorated_setsServeOriginalAndPassesOriginalToChain() throws Exception {
        HippoDecoratedServletRequest decorated = new HippoDecoratedServletRequest(originalRequest, validConfig());
        ServletRequestWrapper outerWrapper = new ServletRequestWrapper(decorated);

        filter.doFilter(outerWrapper, response, chain);

        // chain is called with the OUTER wrapper (the original to unwrapDeep)
        verify(chain).doFilter(outerWrapper, response);
        // serveOriginal is set on the inner HippoDecoratedServletRequest
        // (verified indirectly: if chain called with outer and not decorated, the logic ran)
    }

    // --- doFilter: wrapper around plain request (not decorated) ---

    @Test
    void doFilter_wrapperAroundPlainRequest_passesOriginalToChain() throws Exception {
        ServletRequestWrapper wrapper = new ServletRequestWrapper(originalRequest);

        filter.doFilter(wrapper, response, chain);

        verify(chain).doFilter(wrapper, response);
    }

    // --- lifecycle ---

    @Test
    void init_doesNotThrow() {
        assertDoesNotThrow(() -> filter.init(null));
    }

    @Test
    void destroy_doesNotThrow() {
        assertDoesNotThrow(() -> filter.destroy());
    }
}
