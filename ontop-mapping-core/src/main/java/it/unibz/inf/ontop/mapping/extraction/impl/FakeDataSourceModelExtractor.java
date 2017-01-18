package it.unibz.inf.ontop.mapping.extraction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModelExtractor;
import it.unibz.inf.ontop.model.DBMetadata;

import java.io.IOException;

public class FakeDataSourceModelExtractor implements DataSourceModelExtractor {

    @Inject
    private FakeDataSourceModelExtractor(){
    }

    @Override
    public DataSourceModel extract() throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This FakeDatasourceModelExtractor is fake and thus does not extract");
    }

    @Override
    public DataSourceModel extract(DBMetadata dbMetadata) throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException("This FakeDatasourceModelExtractor is fake and thus does not extract");
    }
}
