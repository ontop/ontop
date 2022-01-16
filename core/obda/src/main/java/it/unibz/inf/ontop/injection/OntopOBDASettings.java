package it.unibz.inf.ontop.injection;


public interface OntopOBDASettings extends OntopModelSettings {

    boolean isSameAsInMappingsEnabled();

    /**
     * If true, metadata about the black-box view will be retrieved by querying the DB.
     */
    boolean allowRetrievingBlackBoxViewMetadataFromDB();

    //--------------------------
    // Keys
    //--------------------------

    String  SAME_AS = "ontop.sameAs";
    String ALLOW_RETRIEVING_BLACK_BOX_VIEW_METADATA_FROM_DB = "ontop.allowRetrievingBlackBoxViewMetadataFromDB";
}
