package it.unibz.inf.ontop.protege.core.impl;


import com.google.common.base.Preconditions;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDADataSourceFactory;

import java.net.URI;
import java.util.UUID;

public class OBDADataSourceFactoryImpl implements OBDADataSourceFactory {

    private static OBDADataSourceFactory INSTANCE = null;

    private OBDADataSourceFactoryImpl() {
    }

    public static OBDADataSourceFactory getInstance() {
        if (INSTANCE == null)
            INSTANCE = new OBDADataSourceFactoryImpl();
        return INSTANCE;
    }

    @Override
    public OBDADataSource getJDBCDataSource(String jdbcurl, String username,
                                            String password, String driverclass) {
        URI id = URI.create(UUID.randomUUID().toString());
        return getJDBCDataSource(id.toString(), jdbcurl, username, password, driverclass);
    }

    @Override
    public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
                                            String username, String password, String driverclass) {
        Preconditions.checkNotNull(sourceuri, "sourceuri is null");
        Preconditions.checkNotNull(jdbcurl, "jdbcurl is null");
        Preconditions.checkNotNull(password, "password is null");
        Preconditions.checkNotNull(username, "username is null");
        Preconditions.checkNotNull(driverclass, "driverclass is null");

        DataSourceImpl source = new DataSourceImpl(URI.create(sourceuri));
        source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, jdbcurl);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driverclass);
        return source;
    }

}
