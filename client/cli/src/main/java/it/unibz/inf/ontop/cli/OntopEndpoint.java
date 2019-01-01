package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;

import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;
import org.springframework.boot.SpringApplication;


@Command(name = "endpoint",
        description = "starts a SPARQL endpoint of Ontop")
public class OntopEndpoint extends OntopMappingOntologyRelatedCommand {

    @Override
    public void run() {
        String[] args = {"--t=" + this.owlFile, "--m=" + this.mappingFile, "--p=" + this.propertiesFile};

        SpringApplication.run(OntopEndpointApplication.class, args);

        //OntopEndpointApplication.main(args);
    }
}
