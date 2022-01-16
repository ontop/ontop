package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;

abstract class OntopMappingOntologyRelatedCommand extends AbstractOntopCommand implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology file",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-c", "--constraint"}, title = "constraint file",
            description = "user supplied DB constraint file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String constraintFile;

    @Option(type = OptionType.COMMAND, name = {"-d", "--db-metadata"}, title = "db-metadata file",
            description = "User-supplied db-metadata file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbMetadataFile;

    @Option(type = OptionType.COMMAND, name = {"-v", "--ontop-views"}, title = "Ontop view file",
            description = "User-supplied view file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String ontopViewFile;

    protected boolean isR2rmlFile(String mappingFile) {
        return !mappingFile.endsWith(".obda");
    }
}
