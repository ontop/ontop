package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;

import javax.annotation.Nullable;
import java.util.Optional;

@Singleton
public class DatabaseInfoSupplierImpl implements DatabaseInfoSupplier {

    @Inject
    protected DatabaseInfoSupplierImpl() {
    }

    @Nullable
    private String dbVersion;

    @Override
    public Optional<String> getDatabaseVersion() {
        return Optional.ofNullable(dbVersion);
    }

    @Override
    public synchronized void setDatabaseVersion(String version) throws IllegalStateException {
        if (this.dbVersion == null)
            this.dbVersion = version;
        else
            throw new IllegalStateException("The database version can only be set once");
    }
}
