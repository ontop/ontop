package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;

abstract class OntopMappingOntologyRelatedCommand implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology file",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-p", "--properties"}, title = "properties file",
            description = "Properties file")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String propertiesFile;

    @Option(type = OptionType.COMMAND, name = {"-c", "--constraint"}, title = "constraint file",
            description = "user supplied DB constraint file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String constraintFile;

    @Option(type = OptionType.COMMAND, name = {"-d", "--db-metadata"}, title = "db-metadata file",
            description = "user supplied db-metadata file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbMetadataFile;

    @Option(type = OptionType.COMMAND, name = {"-v", "--views"}, title = "views file",
            description = "user supplied views file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String viewsFile;

    @Option(type = OptionType.COMMAND, name = {"--db-password"}, title = "DB password",
            description = "DB password (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbPassword;

    @Option(type = OptionType.COMMAND, name = {"-u", "--db-user"}, title = "DB user",
            description = "DB user (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUser;

    @Option(type = OptionType.COMMAND, name = {"--db-url"}, title = "DB URL",
            description = "DB URL (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUrl;

    protected boolean isR2rmlFile(String mappingFile) {
        return !mappingFile.endsWith(".obda");
    }
}
