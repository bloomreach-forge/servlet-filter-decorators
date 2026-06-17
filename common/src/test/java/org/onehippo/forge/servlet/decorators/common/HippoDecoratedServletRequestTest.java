package org.onehippo.forge.servlet.decorators.common;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HippoDecoratedServletRequestTest {

    @Mock private HttpServletRequest request;

    private static DecoratorConfiguration validConfig(String contextPath) {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Forwarded-Host")
                .hosts(Map.of(".*", contextPath))
                .build();
        return configs.iterator().next();
    }

    // --- getContextPath ---

    @Test
    void getContextPath_whenEnabled_returnsConfigContextPath() {
        DecoratorConfiguration cfg = validConfig("/cms");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertEquals("/cms", wrapped.getContextPath());
    }

    @Test
    void getContextPath_whenServeOriginal_returnsOriginalContextPath() {
        when(request.getContextPath()).thenReturn("/original");
        DecoratorConfiguration cfg = validConfig("/cms");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);
        wrapped.setServeOriginal(true);

        assertEquals("/original", wrapped.getContextPath());
    }

    @Test
    void getContextPath_whenInvalidConfig_returnsOriginalContextPath() {
        when(request.getContextPath()).thenReturn("/original");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, DecoratorConfiguration.INVALID);

        assertEquals("/original", wrapped.getContextPath());
    }

    @Test
    void getContextPath_whenDisabledConfig_returnsOriginalContextPath() {
        when(request.getContextPath()).thenReturn("/original");
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(false)
                .hostHeader("X-Host")
                .hosts(Map.of(".*", "/cms"))
                .build();
        DecoratorConfiguration cfg = configs.iterator().next();
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertEquals("/original", wrapped.getContextPath());
    }

    // --- getRequestURI ---

    @Test
    void getRequestURI_whenEnabled_stripsOldAndPrependsNewContext() {
        when(request.getRequestURI()).thenReturn("/old/content/page.html");
        when(request.getContextPath()).thenReturn("/old");
        DecoratorConfiguration cfg = validConfig("/new");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertEquals("/new/content/page.html", wrapped.getRequestURI());
    }

    @Test
    void getRequestURI_whenSameContext_returnsOriginalUri() {
        when(request.getRequestURI()).thenReturn("/cms/content/page.html");
        when(request.getContextPath()).thenReturn("/cms");
        DecoratorConfiguration cfg = validConfig("/cms");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertEquals("/cms/content/page.html", wrapped.getRequestURI());
    }

    @Test
    void getRequestURI_whenDisabled_returnsOriginalUri() {
        when(request.getRequestURI()).thenReturn("/original/page.html");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, DecoratorConfiguration.INVALID);

        assertEquals("/original/page.html", wrapped.getRequestURI());
    }

    @Test
    void getRequestURI_whenNullUri_returnsNull() {
        when(request.getRequestURI()).thenReturn(null);
        // getContextPath() is never reached when URI is null — don't stub it
        DecoratorConfiguration cfg = validConfig("/new");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertNull(wrapped.getRequestURI());
    }

    @Test
    void getRequestURI_whenNewContextIsSlash_returnsStrippedUri() {
        when(request.getRequestURI()).thenReturn("/cms/page.html");
        when(request.getContextPath()).thenReturn("/cms");
        DecoratorConfiguration cfg = validConfig("/");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        assertEquals("/page.html", wrapped.getRequestURI());
    }

    @Test
    void getRequestURI_whenOldContextIsRoot_noStripping() {
        when(request.getRequestURI()).thenReturn("/page.html");
        when(request.getContextPath()).thenReturn("/");
        DecoratorConfiguration cfg = validConfig("/cms");
        HippoDecoratedServletRequest wrapped = new HippoDecoratedServletRequest(request, cfg);

        // old context "/" length = 1, no strip → newContext + "/page.html"
        assertEquals("/cms/page.html", wrapped.getRequestURI());
    }
}
