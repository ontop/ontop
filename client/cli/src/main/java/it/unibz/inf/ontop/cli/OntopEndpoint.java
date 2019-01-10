package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;
import org.springframework.boot.SpringApplication;


@Command(name = "endpoint",
        description = "starts a SPARQL endpoint powered by Ontop")
public class OntopEndpoint extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"--port"}, title = "port",
            description = "port of the SPARQL endpoint")
    private int port = 8080;

    @Option(type = OptionType.COMMAND, name = {"--cors-allowed-origins"}, title = "origins",
            description = "CORS allowed origins")
    private String corsAllowedOrigins = ",";

    @Override
    public void run() {
        String[] args = {
                "--ontology=" + this.owlFile,
                "--mapping=" + this.mappingFile,
                "--properties=" + this.propertiesFile,
                "--port=" + this.port,
                "--cors-allowed-origins=" + this.corsAllowedOrigins
        };

        SpringApplication.run(OntopEndpointApplication.class, args);
    }
}
