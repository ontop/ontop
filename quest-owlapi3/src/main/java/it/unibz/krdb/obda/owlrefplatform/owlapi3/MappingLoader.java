package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.r2rml.R2RMLReader;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MappingLoader {

    public MappingLoader() {}

    public OBDAModel loadFromOBDAFile(String obdafile) throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);
        return obdaModel;
    }

    public OBDAModel loadRFrom2RMLFile(String r2rmlFile, String jdbcUrl, String username, String password, String driverClass) throws Exception {
        OBDADataFactory f = OBDADataFactoryImpl.getInstance();

        URI obdaURI = new File(r2rmlFile).toURI();

        String sourceUrl = obdaURI.toString();
        OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcUrl,
                username, password, driverClass);

        R2RMLReader reader = new R2RMLReader(r2rmlFile);

        return reader.readModel(dataSource);
    }




}
