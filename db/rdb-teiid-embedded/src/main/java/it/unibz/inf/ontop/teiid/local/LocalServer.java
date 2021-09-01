package it.unibz.inf.ontop.teiid.local;

import java.io.Closeable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.core.TeiidException;
import org.teiid.deployers.VDBLifeCycleListener;
import org.teiid.jdbc.CallableStatementImpl;
import org.teiid.jdbc.ConnectionImpl;
import org.teiid.jdbc.LocalProfile;
import org.teiid.jdbc.PreparedStatementImpl;
import org.teiid.jdbc.TeiidPreparedStatement;
import org.teiid.jdbc.TeiidSQLException;
import org.teiid.net.ServerConnection;
import org.teiid.query.sql.lang.Command;
import org.teiid.runtime.EmbeddedRequestOptions;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.transport.ClientServiceRegistry;
import org.teiid.transport.LocalServerConnection;

public final class LocalServer extends EmbeddedServer implements LocalProfile, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServer.class);

    private final Set<EmbeddedConnection> connections;

    @Nullable
    private final Consumer<LocalServer> closeCallback;

    LocalServer(@Nullable final Consumer<LocalServer> closeCallback) {
        this.connections = Collections.synchronizedSet(Sets.newIdentityHashSet());
        this.closeCallback = closeCallback;
    }

    @Override
    public synchronized ConnectionImpl connect(final String url, final Properties info)
            throws TeiidSQLException {
        return connect(url, info, null);
    }

    public synchronized ConnectionImpl connect(final String url, final Properties info,
            @Nullable final Consumer<ConnectionImpl> closeCallback) throws TeiidSQLException {

        // Check that server is not stopped
        Preconditions.checkState(this.dqp != null, "Teiid Embedded Server stopped");

        // Acquire a server connection
        final ServerConnection serverConnection;
        try {
            serverConnection = createServerConnection(info);
        } catch (final TeiidException e) {
            throw TeiidSQLException.create(e);
        }

        // Wrap the server connection with a JDBC connection + Teiid embedded extension,
        // registering a close hook to unregister the connection from the set of pending ones
        final EmbeddedConnection connection = new EmbeddedConnection(serverConnection, info, url,
                c -> {
                    this.connections.remove(c);
                    if (closeCallback != null) {
                        closeCallback.accept(c);
                    }
                });

        // Register the connection among the set of pending connections that need to be closed
        this.connections.add(connection);
        return connection;
    }

    @Override
    public ServerConnection createServerConnection(final Properties info) throws TeiidException {
        return new EmbeddedServerConnection(info);
    }

    @Override
    public synchronized void close() {

        // Abort if already stopped
        if (this.dqp == null) {
            return;
        }

        try {
            // Close connections, ignoring and logging any error
            for (final EmbeddedConnection connection : ImmutableList.copyOf(this.connections)) {
                try {
                    connection.close();
                } catch (final Throwable ex) {
                    LOGGER.warn("Error closing connection", ex);
                }
            }

            // Stop the server
            stop();

        } finally {
            // Mark as closed (if not done yet)
            this.dqp = null;

            // Invoke callback, if any
            if (this.closeCallback != null) {
                this.closeCallback.accept(this);
            }
        }
    }

    /**
     * Connection implementation.
     *
     * Based on org.teiid.runtime.EmbeddedServer#EmbeddedConnectionImpl, with close callback to
     * unregister connection.
     */
    private final class EmbeddedConnection extends ConnectionImpl
            implements org.teiid.runtime.EmbeddedConnection {

        @Nullable
        private Consumer<? super ConnectionImpl> closeCallback;

        private final Object closeLock;

        EmbeddedConnection(final ServerConnection serverConn, final Properties info,
                final String url, @Nullable final Consumer<? super ConnectionImpl> closeCallback) {
            super(serverConn, info, url);
            this.closeCallback = closeCallback;
            this.closeLock = new Object();
        }

        @Override
        public CallableStatement prepareCall(final Command command,
                final EmbeddedRequestOptions options) throws SQLException {

            final CallableStatementImpl csi = this.prepareCall( //
                    command.toString(), //
                    options.getResultSetType(), //
                    ResultSet.CONCUR_READ_ONLY);

            csi.setCommand(command);
            return csi;
        }

        @Override
        public TeiidPreparedStatement prepareStatement(final Command command,
                final EmbeddedRequestOptions options) throws SQLException {

            final PreparedStatementImpl psi = this.prepareStatement( //
                    command.toString(), //
                    options.getResultSetType(), //
                    ResultSet.CONCUR_READ_ONLY);

            psi.setCommand(command);
            return psi;
        }

        @Override
        public synchronized void close() throws SQLException {

            synchronized (this.closeLock) {
                if (!isClosed()) {
                    try {
                        super.close();
                    } finally {
                        final Consumer<? super EmbeddedConnection> callback = this.closeCallback;
                        this.closeCallback = null;
                        if (callback != null) {
                            callback.accept(this);
                        }
                    }
                }
            }
        }

    }

    /**
     * Internal server connection implementation.
     *
     * Based on org.teiid.runtime.EmbeddedServer.embeddedProfile.createServerConnection()
     *
     */
    private final class EmbeddedServerConnection extends LocalServerConnection {

        EmbeddedServerConnection(final Properties info) throws TeiidException {
            super(info, LocalServer.this.useCallingThread);
            getWorkContext().setConnectionProfile(LocalServer.this);
        }

        @Override
        protected ClientServiceRegistry getClientServiceRegistry(final String name) {
            return LocalServer.this.services;
        }

        @Override
        public void addListener(final VDBLifeCycleListener listener) {
            LocalServer.this.repo.addListener(listener);
        }

        @Override
        public void removeListener(final VDBLifeCycleListener listener) {
            LocalServer.this.repo.removeListener(listener);
        }

    }

}