package org.onehippo.forge.servlet.decorators.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DecoratorConfigurationTest {

    @Test
    void invalidSentinel_isNotValid() {
        assertFalse(DecoratorConfiguration.INVALID.isValid());
    }

    @Test
    void invalidSentinel_hasNullHostHeader() {
        assertNull(DecoratorConfiguration.INVALID.getHostHeader());
    }

    @Test
    void invalidSentinel_isNotEnabled() {
        assertFalse(DecoratorConfiguration.INVALID.isEnabled());
    }

    @Test
    void constClass_hasExpectedConstants() {
        assertNotNull(DecoratorConst.CONFIG_ENABLED);
        assertNotNull(DecoratorConst.HEADER_X_FORWARDED_HOST);
        assertEquals("X-Forwarded-Host", DecoratorConst.HEADER_X_FORWARDED_HOST);
        assertTrue(DecoratorConst.CACHE_EXPIRES_IN_DAYS > 0);
        assertTrue(DecoratorConst.CACHE_MAX_SIZE > 0);
    }
}
