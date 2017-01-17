package it.unibz.inf.ontop.injection;


public interface OntopMappingSettings extends OntopOBDASettings {

    boolean isFullMetadataExtractionEnabled();

    String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

}
