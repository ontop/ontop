package org.semanticweb.ontop.cli;


import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;

public abstract class OntopMappingOntologyRelatedCommand implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontologyFile",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mappingFile",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-u", "--username"}, title = "jdbcUserName",
            description = "user name for the jdbc connection (only for R2RML mapping)")
    protected String jdbcUserName;

    @Option(type = OptionType.COMMAND, name = {"-p", "--password"}, title = "jdbcPassword",
            description = "password for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcPassword;

    @Option(type = OptionType.COMMAND, name = {"-l", "--url"}, title = "jdbcURL",
            description = "jdbcURL for the jdbc connection  (only for R2RML mapping)")
    protected String jdbcURL;

    @Option(type = OptionType.COMMAND, name = {"-d", "--driver-class"}, title = "jdbcDriver",
            description = "class name of the jdbc Driver (only for R2RML mapping)")
    protected String jdbcDriverClass;
}
