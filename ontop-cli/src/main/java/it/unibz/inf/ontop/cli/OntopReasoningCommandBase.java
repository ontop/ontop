package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.google.common.base.Preconditions;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidPredicateDeclarationException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.r2rml.R2RMLReader;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
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
    //@BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String outputFile;

    @Option(type = OptionType.COMMAND, name = {"--enable-annotations"},
            description = "enable annotation properties defined in the ontology. Default: false")
    public boolean enableAnnotations = false;

    protected static OWLDocumentFormat getDocumentFormat(String format) throws Exception {
		OWLDocumentFormat ontoFormat;

		if(format == null){
			ontoFormat = new RDFXMLDocumentFormat();
		}
		else {
		switch (format) {
			case "rdfxml":
				ontoFormat = new RDFXMLDocumentFormat();
				break;
			case "owlxml":
				ontoFormat = new OWLXMLDocumentFormat();
				break;
			case "turtle":
				ontoFormat = new TurtleDocumentFormat();
				break;
            case "n3":
                ontoFormat = new N3DocumentFormat();
                break;
			default:
				throw new Exception("Unknown format: " + format);
			}
		}
		return ontoFormat;
	}

    protected static OWLOntology extractDeclarations(OWLOntologyManager manager, OWLOntology ontology) throws OWLOntologyCreationException {

        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
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
