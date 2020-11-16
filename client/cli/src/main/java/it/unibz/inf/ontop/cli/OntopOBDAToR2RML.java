package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.base.Strings;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.SQLPPMappingToR2RMLConverter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

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

        OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(inputMappingFile)
                .jdbcDriver("dummy")
                .jdbcUrl("dummy")
                .jdbcUser("")
                .jdbcPassword("");

        if (!Strings.isNullOrEmpty(owlFile)) {
            configBuilder.ontologyFile(owlFile);
        }

        OntopSQLOWLAPIConfiguration config = configBuilder.build();

        try {
            SQLPPMapping ppMapping = config.loadProvidedPPMapping();
            SQLPPMappingToR2RMLConverter converter = new SQLPPMappingToR2RMLConverter(ppMapping, config.getRdfFactory(),
                    config.getTermFactory());

            converter.write(new File(outputMappingFile));
            System.out.println("R2RML mapping file " + outputMappingFile + " written!");
        }
        catch (MappingException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
