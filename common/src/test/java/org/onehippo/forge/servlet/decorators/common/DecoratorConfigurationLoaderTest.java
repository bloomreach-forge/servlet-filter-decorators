package org.onehippo.forge.servlet.decorators.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecoratorConfigurationLoaderTest {

    @Mock private Repository repository;
    @Mock private Credentials credentials;
    @Mock private Session session;
    @Mock private Node configNode;
    @Mock private NodeIterator nodeIterator;
    @Mock private Event event;

    /** Minimal concrete subclass — class is abstract */
    static class MinimalLoader extends DecoratorConfigurationLoader {}

    private MinimalLoader loader() {
        return new MinimalLoader();
    }

    @Test
    void needReloading_defaultsToTrue() {
        assertTrue(loader().needReloading());
    }

    @Test
    void invalidate_setsNeedRefreshTrue() {
        MinimalLoader l = loader();
        l.needRefresh = false;
        l.invalidate(event);
        assertTrue(l.needReloading());
    }

    @Test
    void setAndGetConfigurationLocation_roundTrips() {
        MinimalLoader l = loader();
        l.setConfigurationLocation("/hippo:config/decorators");
        assertEquals("/hippo:config/decorators", l.getConfigurationLocation());
    }

    @Test
    void setAndGetRepository_roundTrips() {
        MinimalLoader l = loader();
        l.setRepository(repository);
        assertSame(repository, l.getRepository());
    }

    @Test
    void setAndGetCredentials_roundTrips() {
        MinimalLoader l = loader();
        l.setCredentials(credentials);
        assertSame(credentials, l.getCredentials());
    }

    @Test
    void getLastLoadDate_isNotNull() {
        assertNotNull(loader().getLastLoadDate());
    }

    @Test
    void load_whenNoRefreshNeeded_returnsImmediately() {
        MinimalLoader l = loader();
        l.needRefresh = false;
        assertNotNull(l.load());
        verifyNoInteractions(repository);
    }

    @Test
    void load_whenSessionLoginThrows_returnsEmptyData() throws RepositoryException {
        MinimalLoader l = loader();
        l.setRepository(repository);
        l.setCredentials(credentials);
        l.setConfigurationLocation("/config");
        when(repository.login(credentials)).thenThrow(new RepositoryException("no session"));

        assertNotNull(l.load());
    }

    @Test
    void load_withEmptyConfigNode_succeeds() throws Exception {
        MinimalLoader l = loader();
        l.setRepository(repository);
        l.setCredentials(credentials);
        l.setConfigurationLocation("/config");
        when(repository.login(credentials)).thenReturn(session);
        when(session.getNode("/config")).thenReturn(configNode);
        when(configNode.getNodes()).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(false);

        assertNotNull(l.load());
        assertFalse(l.needReloading()); // flagged as loaded
        verify(session).logout();
    }

    @Test
    void load_whenGetNodeThrows_returnsExistingData() throws Exception {
        MinimalLoader l = loader();
        l.setRepository(repository);
        l.setCredentials(credentials);
        l.setConfigurationLocation("/config");
        when(repository.login(credentials)).thenReturn(session);
        when(session.getNode("/config")).thenThrow(new RepositoryException("not found"));

        assertNotNull(l.load());
        verify(session).logout();
    }
}
