package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Objects;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-iri"}, title = "base IRI",
            description = "base uri of the generated mapping")
    protected String baseIRI;

    @Override
    public void run() {

        try {
            if (baseIRI.contains("#")) {
                System.err.println("Base IRI cannot contain the character '#'!");
                throw new IllegalArgumentException("Base IRI cannot contain the character '#'!");
            }

            OBDACoreConfiguration defaultConfiguration = OBDACoreConfiguration.defaultBuilder()
                    .build();
            Injector injector = defaultConfiguration.getInjector();

            Objects.requireNonNull(owlFile, "ontology file must not be null");
            File ontologyFile = new File(owlFile);
            File obdaFile = new File(mappingFile);
                    DirectMappingBootstrapper dm = new DirectMappingBootstrapper(
                    baseIRI, jdbcURL, jdbcUserName, jdbcPassword, jdbcDriverClass,
                            injector.getInstance(NativeQueryLanguageComponentFactory.class),
                            injector.getInstance(OBDAFactoryWithException.class));

                    OBDAModel model = dm.getModel();
                    OWLOntology onto = dm.getOntology();
            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(model);
            writer.save(obdaFile);
            onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));

        } catch (Exception e) {
            System.err.println("Error occurred during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}
