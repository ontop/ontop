package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.bash.BashCompletion;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import it.unibz.inf.ontop.cli.utils.Env;

abstract class AbstractOntopCommand implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-p", "--properties"}, title = "properties file",
            description = "Properties file")
    @Env(value = {"ONTOP_PROPERTIES_FILE", "PROPERTIES_FILE"}, deprecated = "PROPERTIES_FILE")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String propertiesFile;

    @Option(type = OptionType.COMMAND, name = {"--db-password"}, title = "DB password",
            description = "DB password (overrides the properties)")
    @Env(value = "ONTOP_DB_PASSWORD", file = "ONTOP_DB_PASSWORD_FILE")
    String dbPassword;

    @Option(type = OptionType.COMMAND, name = {"-u", "--db-user"}, title = "DB user",
            description = "DB user (overrides the properties)")
    @Env(value = "ONTOP_DB_USER", file = "ONTOP_DB_USER_FILE")
    String dbUser;

    @Option(type = OptionType.COMMAND, name = {"--db-url"}, title = "DB URL",
            description = "DB URL (overrides the properties)")
    @Env(value = "ONTOP_DB_URL", file = "ONTOP_DB_URL_FILE")
    String dbUrl;

    @Option(type = OptionType.COMMAND, name = {"--db-driver"}, title = "DB driver",
            description = "DB driver (overrides the properties)")
    @Env("ONTOP_DB_DRIVER")
    String dbDriver;
}
