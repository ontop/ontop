package it.unibz.inf.ontop.teiid.embedded;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.Nullable;

import org.teiid.core.util.ApplicationInfo;
import org.teiid.jdbc.JDBCPlugin;

public final class TeiidEmbeddedDriver implements Driver {

    @Override
    public boolean acceptsURL(final String jdbcUrl) {
        return TeiidEmbeddedUtils.acceptsJdbcUrl(jdbcUrl);
    }

    @Override
    public Connection connect(final String jdbcUrl, @Nullable final Properties info)
            throws SQLException {

        if (!TeiidEmbeddedUtils.acceptsJdbcUrl(jdbcUrl)) {
            return null;
        }

        try {
            return TeiidEmbeddedDataSource.load(jdbcUrl, info).getConnection();
        } catch (final Throwable ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String jdbcUrl, @Nullable Properties info) {

        info = TeiidEmbeddedUtils.parseJdbcUrl(jdbcUrl, info);

        final String[] jdbcProperties = new String[] { TeiidEmbeddedUtils.JDBC_VDB_NAME,
                TeiidEmbeddedUtils.JDBC_VDB_PATH };

        final DriverPropertyInfo[] dpis = new DriverPropertyInfo[jdbcProperties.length];
        for (int i = 0; i < jdbcProperties.length; ++i) {
            final String p = jdbcProperties[i];
            dpis[i] = new DriverPropertyInfo(p, info.getProperty(p));
            dpis[i].required = p.equals(TeiidEmbeddedUtils.JDBC_VDB_NAME);
            dpis[i].description = JDBCPlugin.Util.getString(p + "_desc");
            if (JDBCPlugin.Util.keyExists(p + "_choices")) {
                dpis[i].choices = JDBCPlugin.Util.getString(p + "_choices").split(",");
            }
        }
        return dpis;
    }

    @Override
    public int getMajorVersion() {
        return ApplicationInfo.getInstance().getMajorReleaseVersion();
    }

    @Override
    public int getMinorVersion() {
        return ApplicationInfo.getInstance().getMinorReleaseVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger(getClass().getPackage().getName());
    }

}
