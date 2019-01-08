package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;
import org.springframework.boot.SpringApplication;


@Command(name = "endpoint",
        description = "starts a SPARQL endpoint of Ontop")
public class OntopEndpoint extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"--port"}, title = "port of the SPARQL endpoint",
            description = "Properties file")
    private int port = 8080;

    @Override
    public void run() {
        String[] args = {
                "--ontology=" + this.owlFile,
                "--mapping=" + this.mappingFile,
                "--properties=" + this.propertiesFile,
                "--port=" + this.port};

        SpringApplication.run(OntopEndpointApplication.class, args);
    }
}
