package it.unibz.inf.ontop.owlrefplatform.owlapi3;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.r2rml.R2RMLReader;

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
