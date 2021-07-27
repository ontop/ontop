package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;

import it.unibz.inf.ontop.injection.OntopCTablesConfiguration;

@Command(name = "ctables", description = "Start the engine managing computed tables (ctables)")
public class OntopCTables extends OntopMappingOntologyRelatedCommand {

    // TODO evaluate supporting --constraint, --db-metadata, --ontop-views

    @Option(type = OptionType.COMMAND, name = { "-p",
            "--properties" }, title = "properties file", description = "Properties file")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String propertiesFile;

    @Option(type = OptionType.COMMAND, name = {
            "--db-url" }, title = "DB URL", description = "DB URL (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUrl;

    @Option(type = OptionType.COMMAND, name = { "-u",
            "--db-user" }, title = "DB user", description = "DB user (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUser;

    @Option(type = OptionType.COMMAND, name = {
            "--db-password" }, title = "DB password", description = "DB password (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbPassword;

    @Option(type = OptionType.COMMAND, name = {
            "--ctables" }, title = "ctables file", description = "ctables configuration file (.yml)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String ctablesRulesetFile;

    @Override
    public void run() {

        final OntopCTablesConfiguration.Builder<? extends OntopCTablesConfiguration.Builder> builder;
        builder = OntopCTablesConfiguration.defaultBuilder().propertyFile(this.propertiesFile);

        if (this.dbUrl != null) {
            builder.jdbcUrl(this.dbUrl);
        }

        if (this.dbUser != null) {
            builder.jdbcUser(this.dbUser);
        }

        if (this.dbPassword != null) {
            builder.jdbcPassword(this.dbPassword);
        }

        if (this.ctablesRulesetFile != null) {
            builder.ctablesRulesetFile(this.ctablesRulesetFile);
        }

        final OntopCTablesConfiguration configuration = builder.build();

        // TODO
    }

}
