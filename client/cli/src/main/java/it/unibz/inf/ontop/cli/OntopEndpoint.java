package it.unibz.inf.ontop.cli;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;

import java.util.ArrayList;


@Command(name = "endpoint",
        description = "starts a SPARQL endpoint powered by Ontop")
public class OntopEndpoint extends OntopMappingOntologyRelatedCommand {

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

    @Override
    public void run() {

        ArrayList<String> argList = Lists.newArrayList(
                "--mapping=" + this.mappingFile,
                "--properties=" + this.propertiesFile,
                "--port=" + this.port,
                "--lazy=" + this.lazy,
                "--dev=" + this.dev);

        if (this.corsAllowedOrigins != null)
            argList.add("--cors-allowed-origins=" + this.corsAllowedOrigins);

        if (this.owlFile != null)
            argList.add("--ontology=" + this.owlFile);

        if (this.constraintFile != null)
            argList.add("--constraint=" + this.constraintFile);

        String[] args = new String[argList.size()];
        argList.toArray(args);

        OntopEndpointApplication.main(args);
    }
}
