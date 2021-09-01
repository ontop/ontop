package org.teiid.jdbc.jboss;

import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.jdbc.ConnectionImpl;
import org.teiid.jdbc.ConnectionProfile;
import org.teiid.jdbc.TeiidSQLException;

import it.unibz.inf.ontop.teiid.local.LocalServer;
import it.unibz.inf.ontop.teiid.local.LocalServers;
import it.unibz.inf.ontop.teiid.util.Pool.Lease;

public class ModuleLocalProfile implements ConnectionProfile {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleLocalProfile.class);

    @Override
    public ConnectionImpl connect(final String url, final Properties info)
            throws TeiidSQLException {

        // Check arguments
        Objects.requireNonNull(url);
        Objects.requireNonNull(info);

        // Log beginning of operation (this may take a while if a server is allocated)
        final long ts = System.currentTimeMillis();
        LOGGER.debug("Connection requested to '{}'", url);

        // Retrieve/start a Teiid server for the properties supplied, increasing its ref count
        final Lease<LocalServer> lease = LocalServers.lease(info);

        // Obtain a connection from the server, releasing it when the connection is closed.
        // The server will be stopped and closed if the ref count reaches 0
        final ConnectionImpl connection = lease.get().connect(url, info, c -> lease.close());

        // Log and return the connection
        LOGGER.debug("Connection returned, {} ms", System.currentTimeMillis() - ts);
        return connection;
    }

}
