package it.unibz.inf.ontop.injection;


public interface OntopOBDASettings extends OntopModelSettings {

    boolean isSameAsInMappingsEnabled();

    /**
     * If true, metadata about the black-box view will be retrieved by querying the DB.
     */
    boolean allowRetrievingBlackBoxViewMetadataFromDB();

    /**
     * If true, the OBDA loading procedure will not fail if some of the provided mappings are invalid, they will be
     * ignored instead.
     */
    boolean ignoreInvalidMappingEntries();
    boolean ignoreInvalidLensEntries();

    /**
     * If true, system tables will also be accessible by ontop.
     */
    boolean exposeSystemTables();

    //--------------------------
    // Keys
    //--------------------------

    String  SAME_AS = "ontop.sameAs";
    String ALLOW_RETRIEVING_BLACK_BOX_VIEW_METADATA_FROM_DB = "ontop.allowRetrievingBlackBoxViewMetadataFromDB";
    String IGNORE_INVALID_MAPPING_ENTRIES = "ontop.ignoreInvalidMappingEntries";
    String IGNORE_INVALID_LENS_ENTRIES = "ontop.ignoreInvalidLensEntries";
    String EXPOSE_SYSTEM_TABLES = "ontop.exposeSystemTables";
}
