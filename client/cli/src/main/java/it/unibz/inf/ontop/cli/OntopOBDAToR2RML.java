package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.base.Strings;
import eu.optique.r2rml.api.binding.jena.JenaR2RMLMappingManager;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.SQLPPMappingToR2RMLConverter;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;

@Command(name = "to-r2rml",
        description = "Convert ontop native mapping format (.obda) to R2RML format")
public class OntopOBDAToR2RML implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.obda",
            description = "Input mapping file in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology.owl",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Nullable // optional
    private String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.ttl",
            description = "Output mapping file in R2RML format (.ttl)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String outputMappingFile;

    @Override
    public void run() {

        if (Strings.isNullOrEmpty(outputMappingFile)) {
            outputMappingFile = inputMappingFile.substring(0, inputMappingFile.length() - ".obda".length())
                    .concat(".ttl");
        }

        File out = new File(outputMappingFile);

        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(inputMappingFile)
                .jdbcDriver("dummy")
                .jdbcUrl("dummy")
                .jdbcUser("")
                .jdbcPassword("");

        if (!Strings.isNullOrEmpty(owlFile)){
            configBuilder.ontologyFile(owlFile);
        }

        OntopSQLOWLAPIConfiguration config = configBuilder.build();

        SQLPPMapping ppMapping;
        /*
         * load the mapping in native Ontop syntax
         */
        try {
            ppMapping = config.loadProvidedPPMapping();
        } catch (MappingException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        OWLOntology ontology;
        try {
            ontology = config.loadInputOntology().orElse(null);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        SQLPPMappingToR2RMLConverter converter = new SQLPPMappingToR2RMLConverter(ppMapping, ontology,
                config.getRdfFactory());

        final Collection<TriplesMap> tripleMaps = converter.getTripleMaps();
//        final RDF4JR2RMLMappingManager mm = RDF4JR2RMLMappingManager.getInstance();
//        final RDF4JGraph rdf4JGraph = mm.exportMappings(tripleMaps);
//        final JenaRDF jena = new JenaRDF();
//        final Graph jenaGraph = jena.asJenaGraph(rdf4JGraph);

        final JenaR2RMLMappingManager mm = JenaR2RMLMappingManager.getInstance();
        final JenaGraph jenaGraph = mm.exportMappings(tripleMaps);
        final Graph graph = new JenaRDF().asJenaGraph(jenaGraph);

        try {
            // use Jena to output pretty turtle syntax
            RDFDataMgr.write(new FileOutputStream(out), graph, RDFFormat.TURTLE_PRETTY) ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("R2RML mapping file " + outputMappingFile + " written!");
    }
}
