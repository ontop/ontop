package org.semanticweb.ontop.cli;


import com.google.common.base.Preconditions;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.r2rml.R2RMLReader;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

public abstract class OntopReasoningCommandBase implements OntopCommand{


    @Option(type = OptionType.COMMAND, name = {"--disable-reasoning"},
            description = "disable OWL reasoning. Default: false")
    public boolean disableReasoning = false;
    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontologyFile",
            description = "OWL ontology file")
    protected String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mappingFile",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)", required = true)
    protected String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"},
            title = "output", description = "output file (default) or directory (for --separate-files")
    protected String outputFile;

    @Option(type = OptionType.COMMAND, name = {"-u", "--username"}, title = "jdbcUserName",
            description = "user name for the jdbc connection (only for R2RML mapping)")
    protected String jdbcUserName;

    @Option(type = OptionType.COMMAND, name = {"-p", "--password"}, title = "jdbcPassword",
            description = "password for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcPassword;

    @Option(type = OptionType.COMMAND, name = {"-l", "--url"}, title = "jdbcUrl",
            description = "jdbcUrl for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcUrl;

    @Option(type = OptionType.COMMAND, name = {"-c", "--driver-class"}, title = "jdbcUrl",
            description = "class name of the jdbc Driver (only for R2RML mapping)")
    protected String jdbcDriverClass;

    protected static OWLOntologyFormat getOntologyFormat(String format) throws Exception {
		OWLOntologyFormat ontoFormat;

		if(format == null){
			ontoFormat = new RDFXMLOntologyFormat();
		}
		else {
		switch (format) {
			case "rdfxml":
				ontoFormat = new RDFXMLOntologyFormat();
				break;
			case "owlxml":
				ontoFormat = new OWLXMLOntologyFormat();
				break;
			case "turtle":
				ontoFormat = new TurtleOntologyFormat();
				break;
			default:
				throw new Exception("Unknown format: " + format);
			}
		}
		return ontoFormat;
	}

    protected static OWLOntology extractDeclarations(OWLOntologyManager manager, OWLOntology ontology) throws OWLOntologyCreationException {

        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        System.err.println("Ontology " + ontologyIRI);

        Set<OWLDeclarationAxiom> declarationAxioms = ontology.getAxioms(AxiomType.DECLARATION);

        manager.removeOntology(ontology);

        OWLOntology newOntology = manager.createOntology(ontologyIRI);

        manager.addAxioms(newOntology, declarationAxioms);

        return newOntology;
    }

    protected OBDAModel loadMappingFile(String mappingFile) throws InvalidPredicateDeclarationException, IOException, InvalidMappingException {
        OBDAModel obdaModel;
        if(mappingFile.endsWith(".obda")){
            obdaModel = loadOBDA(mappingFile);
        } else {
            obdaModel = loadR2RML(mappingFile, jdbcUrl, jdbcUserName, jdbcPassword, jdbcDriverClass);
        }
        return obdaModel;
    }

    private OBDAModel loadOBDA(String obdaFile) throws InvalidMappingException, IOException, InvalidPredicateDeclarationException {
        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);
        return obdaModel;
    }

    private OBDAModel loadR2RML(String r2rmlFile, String jdbcUrl, String username, String password, String driverClass) {

        Preconditions.checkNotNull(jdbcUrl, "jdbcUrl is null");
        Preconditions.checkNotNull(password, "password is null");
        Preconditions.checkNotNull(username, "username is null");
        Preconditions.checkNotNull(driverClass, "driverClass is null");

        OBDADataFactory f = OBDADataFactoryImpl.getInstance();

        URI obdaURI = new File(r2rmlFile).toURI();

        String sourceUrl = obdaURI.toString();
        OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcUrl,
                username, password, driverClass);

        R2RMLReader reader = new R2RMLReader(r2rmlFile);

        return reader.readModel(dataSource);
    }
}
