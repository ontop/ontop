package it.unibz.inf.ontop.endpoint;

public class EndpointConfig {
    private final String ontologyFile;
    private final String mappingFile;
    private final String propertiesFile;

    public EndpointConfig(String ontologyFile, String mappingFile, String propertiesFile) {
        this.ontologyFile = ontologyFile;
        this.mappingFile = mappingFile;
        this.propertiesFile = propertiesFile;
    }

    public String getOntologyFile() {
        return ontologyFile;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }
}
