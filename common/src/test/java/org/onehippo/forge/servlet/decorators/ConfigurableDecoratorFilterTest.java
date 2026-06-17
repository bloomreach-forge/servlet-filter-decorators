package org.onehippo.forge.servlet.decorators;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onehippo.forge.servlet.decorators.common.DecoratorConfiguration;
import org.onehippo.forge.servlet.decorators.common.DecoratorConfigurationLoader;
import org.onehippo.forge.servlet.decorators.common.HippoDecoratedServletRequest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurableDecoratorFilterTest {

    @Mock private HttpServletRequest request;
    @Mock private ServletResponse response;
    @Mock private FilterChain chain;
    @Mock private FilterConfig filterConfig;
    @Mock private DecoratorConfigurationLoader configLoader;

    private TestableFilter filter;

    static class TestableFilter extends ConfigurableDecoratorFilter {
        @Override
        protected void initializeConfigManager() {
            // stays uninitialised unless test sets initialized = true manually
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        filter = new TestableFilter();
        filter.init(filterConfig);  // initialises the Guava cache
    }

    // --- getHost ---

    @Test
    void getHost_withForwardedHostHeader_returnsHeaderValue() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn("www.example.com");
        assertEquals("www.example.com", filter.getHost(request));
    }

    @Test
    void getHost_withoutForwardedHostHeader_returnsRemoteHost() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getRemoteHost()).thenReturn("192.168.1.1");
        assertEquals("192.168.1.1", filter.getHost(request));
    }

    @Test
    void getHost_withEmptyForwardedHostHeader_returnsRemoteHost() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn("");
        when(request.getRemoteHost()).thenReturn("10.0.0.1");
        assertEquals("10.0.0.1", filter.getHost(request));
    }

    // --- config: not initialized ---

    @Test
    void config_whenNotInitialized_returnsInvalid() {
        DecoratorConfiguration cfg = filter.config(request, response);
        assertTrue(cfg.invalid());
    }

    // --- config: initialized, needs reload ---

    @Test
    void config_whenInitializedAndNeedsReload_invalidatesCacheAndContinues() {
        filter.configLoader = configLoader;
        filter.initialized = true;
        when(configLoader.needReloading()).thenReturn(true);
        when(configLoader.load()).thenReturn(Set.of());
        when(request.getHeader("X-Forwarded-Host")).thenReturn("host.com");

        DecoratorConfiguration cfg = filter.config(request, response);

        verify(configLoader).load(); // reloaded due to needReloading
        assertTrue(cfg.invalid()); // no matching config for "host.com"
    }

    // --- config: initialized, host matches ---

    @Test
    void config_withMatchingHost_returnsValidConfig() {
        filter.configLoader = configLoader;
        filter.initialized = true;

        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true).hostHeader("X-Forwarded-Host").hosts(Map.of(".*\\.example\\.com", "/cms")).build();

        when(configLoader.needReloading()).thenReturn(false);
        when(configLoader.load()).thenReturn(configs);
        when(request.getHeader("X-Forwarded-Host")).thenReturn("www.example.com");

        DecoratorConfiguration cfg = filter.config(request, response);
        assertTrue(cfg.isValid());
        assertEquals("/cms", cfg.getContextPath());
    }

    // --- doFilter: invalid/disabled config passes through ---

    @Test
    void doFilter_whenNotInitialized_passesRequestThrough() throws Exception {
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenValidConfig_wrapsRequestInDecorator() throws Exception {
        filter.configLoader = configLoader;
        filter.initialized = true;

        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true).hostHeader("X-Forwarded-Host").hosts(Map.of(".*", "/cms")).build();

        when(configLoader.needReloading()).thenReturn(false);
        when(configLoader.load()).thenReturn(configs);
        when(request.getHeader("X-Forwarded-Host")).thenReturn("www.example.com");

        filter.doFilter(request, response, chain);

        // Chain should receive a HippoDecoratedServletRequest, not the raw request
        verify(chain).doFilter(argThat(r -> r instanceof HippoDecoratedServletRequest), eq(response));
    }

    // --- destroy ---

    @Test
    void destroy_doesNotThrow() {
        filter.configLoader = configLoader;
        filter.initialized = true;
        assertDoesNotThrow(() -> filter.destroy());
    }
}
