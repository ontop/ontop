package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.RequiredOnlyIf;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.collect.Lists;
import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;

import java.util.ArrayList;


@Command(name = "endpoint",
        description = "Start a SPARQL endpoint powered by Ontop")
public class OntopEndpoint extends OntopReasoningCommandBase {

    @Option(type = OptionType.COMMAND, name = {"--portal"}, title = "endpoint portal file",
            description = "endpoint portal file (including title and queries)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String portalFile;

    @Option(type = OptionType.COMMAND, name = {"--port"}, title = "port",
            description = "port of the SPARQL endpoint")
    private int port = 8080;

    @Option(type = OptionType.COMMAND, name = {"--cors-allowed-origins"}, title = "origins",
            description = "CORS allowed origins")
    private String corsAllowedOrigins;

    @Option(type = OptionType.COMMAND, name = {"--lazy"}, title = "lazy",
            description = "lazy initialization")
    private boolean lazy = false;

    @Option(type = OptionType.COMMAND, name = {"--dev"}, title = "dev",
            description = "development mode")
    private boolean dev = false;

    @Option(type = OptionType.COMMAND, name = {"--predefined-config"}, title = "predefined query JSON config file",
            description = "predefined query config file")
    @RequiredOnlyIf(names = {"--predefined-queries"})
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String predefinedConfig;

    @Option(type = OptionType.COMMAND, name = {"--predefined-queries"}, title = "predefined query TOML file",
            description = "predefined SPARQL queries file")
    @RequiredOnlyIf(names = {"--predefined-config"})
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String predefinedQueries;

    @Option(type = OptionType.COMMAND, name = {"--contexts"}, title = "JSON-LD context file for predefined queries",
            description = "File containing JSON-LD contexts for predefined queries")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String contexts;

    @Option(type = OptionType.COMMAND, name = {"--disable-portal-page"}, title = "disable the portal page",
            description = "Disable the portal page (/index.html) of the SPARQL endpoint. ")
    private boolean disablePortalPage = false;

    @Option(type = OptionType.COMMAND, name = {"--enable-download-ontology"}, title = "allow to download the ontology",
            description = "Allow to download the ontology as a plain text file (/ontology). Default: false")
    private boolean enableDownloadOntology = false;
    
    @Override
    public void run() {

        ArrayList<String> argList = Lists.newArrayList(
                "--mapping=" + this.mappingFile,
                //"--properties=" + this.propertiesFile,
                "--port=" + this.port,
                "--lazy=" + this.lazy,
                "--dev=" + this.dev,
                "--disable-portal-page=" + this.disablePortalPage,
                "--enable-download-ontology=" + this.enableDownloadOntology
                );

        if (this.propertiesFile != null)
            argList.add("--properties=" + this.propertiesFile);

        if (this.corsAllowedOrigins != null)
            argList.add("--cors-allowed-origins=" + this.corsAllowedOrigins);

        if (this.owlFile != null)
            argList.add("--ontology=" + this.owlFile);

        if (this.xmlCatalogFile != null)
            argList.add("--xml-catalog=" + this.xmlCatalogFile);

        if (this.constraintFile != null)
            argList.add("--constraint=" + this.constraintFile);

        if (this.dbMetadataFile != null)
            argList.add("--db-metadata=" + this.dbMetadataFile);

        if (this.ontopViewFile != null)
            argList.add("--ontop-views=" + this.ontopViewFile);

        if (this.portalFile != null)
            argList.add("--portal=" + this.portalFile);

        if (this.predefinedConfig != null)
            argList.add("--predefined-config=" + this.predefinedConfig);

        if (this.predefinedQueries != null)
            argList.add("--predefined-queries=" + this.predefinedQueries);

        if (this.contexts != null)
            argList.add("--contexts=" + this.contexts);

        if (dbUser != null)
            argList.add("--db-user=" + this.dbUser);

        if (this.dbPassword != null)
            argList.add("--db-password=" + this.dbPassword);

        if (dbUrl != null)
            argList.add("--db-url=" + this.dbUrl);

        if (dbDriver != null)
            argList.add("--db-driver=" + this.dbDriver);

        String[] args = new String[argList.size()];
        argList.toArray(args);

        // Spring boot gives warns when the logback.configurationFile property is set
        System.clearProperty("logback.configurationFile");
        // System.setProperty("logback.configurationFile", "");

        OntopEndpointApplication.main(args);
    }
}
