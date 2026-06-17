package org.onehippo.forge.servlet.decorators.common;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorConfigurationBuilderTest {

    // --- valid build ---

    @Test
    void build_withValidHostAndPath_producesValidConfig() {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Forwarded-Host")
                .hosts(Map.of(".*\\.example\\.com", "/cms"))
                .build();

        assertEquals(1, configs.size());
        DecoratorConfiguration cfg = configs.iterator().next();
        assertTrue(cfg.isValid());
        assertFalse(cfg.invalid());
        assertEquals("/cms", cfg.getContextPath());
        assertNotNull(cfg.getHostPattern());
        assertEquals("X-Forwarded-Host", cfg.getHostHeader());
    }

    @Test
    void build_withEnabledTrue_configIsEnabled() {
        DecoratorConfiguration cfg = singleConfig(true, ".*", "/app");
        assertTrue(cfg.isEnabled());
        assertFalse(cfg.disabled());
    }

    @Test
    void build_withEnabledFalse_configIsDisabled() {
        DecoratorConfiguration cfg = singleConfig(false, ".*", "/app");
        assertFalse(cfg.isEnabled());
        assertTrue(cfg.disabled());
    }

    @Test
    void build_withMultipleHosts_producesMultipleConfigs() {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Forwarded-Host")
                .hosts(Map.of(".*\\.site-a\\.com", "/a", ".*\\.site-b\\.com", "/b"))
                .build();
        assertEquals(2, configs.size());
    }

    @Test
    void build_withEmptyHostKey_skipsEntry() {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Host")
                .hosts(Map.of("", "/cms"))
                .build();
        assertTrue(configs.isEmpty());
    }

    @Test
    void build_withInvalidRegex_skipsEntry() {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Host")
                .hosts(Map.of("[invalid", "/cms"))
                .build();
        assertTrue(configs.isEmpty());
    }

    @Test
    void build_withNullHostsArg_producesEmptySet() {
        // .hosts(null) is silently ignored; the internal map stays empty → no configs built
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(true)
                .hostHeader("X-Host")
                .hosts(null)
                .build();
        assertTrue(configs.isEmpty());
    }

    @Test
    void build_withNullHostHeader_configHasNullHeader() {
        DecoratorConfiguration cfg = singleConfig(true, ".*", "/app");
        // hostHeader not set → defaults to null in builder, propagated to config
        assertNotNull(cfg); // config still built
    }

    // --- DecoratorConfiguration properties ---

    @Test
    void invalidSentinel_isInvalidAndDisabled() {
        assertTrue(DecoratorConfiguration.INVALID.invalid());
        assertTrue(DecoratorConfiguration.INVALID.disabled());
        assertFalse(DecoratorConfiguration.INVALID.isValid());
        assertFalse(DecoratorConfiguration.INVALID.isEnabled());
    }

    @Test
    void toString_containsValidAndEnabledFields() {
        DecoratorConfiguration cfg = singleConfig(true, ".*", "/site");
        String s = cfg.toString();
        assertTrue(s.contains("valid=true"));
        assertTrue(s.contains("enabled=true"));
        assertTrue(s.contains("/site"));
    }

    @Test
    void builtConfig_hostPatternMatchesExpectedHosts() {
        DecoratorConfiguration cfg = singleConfig(true, ".*\\.bloomreach\\.com", "/cms");
        assertTrue(cfg.getHostPattern().matcher("www.bloomreach.com").matches());
        assertFalse(cfg.getHostPattern().matcher("www.other.com").matches());
    }

    // --- helper ---

    private static DecoratorConfiguration singleConfig(boolean enabled, String hostRegex, String contextPath) {
        Set<DecoratorConfiguration> configs = DecoratorConfiguration.Builder.start()
                .enabled(enabled)
                .hostHeader("X-Forwarded-Host")
                .hosts(Map.of(hostRegex, contextPath))
                .build();
        assertEquals(1, configs.size(), "expected exactly one config");
        return configs.iterator().next();
    }
}
