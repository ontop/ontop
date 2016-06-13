package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public abstract class OntopReasoningCommandBase extends OntopMappingOntologyRelatedCommand {


    @Option(type = OptionType.COMMAND, name = {"--disable-reasoning"},
            description = "disable OWL reasoning. Default: false")
    public boolean disableReasoning = false;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"},
            title = "output", description = "output file (default) or directory (for --separate-files)")
    protected String outputFile;

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

    protected OBDAModel loadMappingFile(String mappingFile) throws InvalidPredicateDeclarationException, IOException,
            InvalidMappingException, DuplicateMappingException, InvalidDataSourceException {

        QuestPreferences preferences = createPreferences(mappingFile);
        return loadModel(mappingFile, preferences);
    }

    protected QuestPreferences createPreferences(String mappingFile) {
        if(mappingFile.endsWith(".obda")){
            return new QuestPreferences();
        }
        else {
            Properties p = new Properties();
            p.setProperty(QuestPreferences.JDBC_URL, jdbcUrl);
            p.setProperty(QuestPreferences.DB_USER, jdbcUserName);
            p.setProperty(QuestPreferences.DB_PASSWORD, jdbcPassword);
            p.setProperty(QuestPreferences.JDBC_DRIVER, jdbcDriverClass);

            return new R2RMLQuestPreferences(p);
        }
    }

    private OBDAModel loadModel(String mappingFile, QuestPreferences preferences)
            throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
        Injector injector = Guice.createInjector(new OBDACoreModule(preferences));

        NativeQueryLanguageComponentFactory factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
        MappingParser mappingParser = factory.create(new File(mappingFile));

        return mappingParser.getOBDAModel();
    }
}
