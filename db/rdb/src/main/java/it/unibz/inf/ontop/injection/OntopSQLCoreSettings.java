package it.unibz.inf.ontop.injection;

public interface OntopSQLCoreSettings extends OntopOBDASettings {

    String getJdbcUrl();
    String getJdbcDriver();

    boolean useCommonTableExpressionsForBlackViewsIfSupported();
    String getOntopCommonTableExpressionsPrefix();

    //-------
    // Keys
    //-------

    String JDBC_URL = "jdbc.url";
    String JDBC_DRIVER = "jdbc.driver";

    String ONTOP_USE_COMMON_TABLE_EXPRESSIONS_FOR_BLACK_BOX_VIEWS_IF_SUPPORTED = "ontop.useCTEsForBlackBoxViewsIfSupported";
    String ONTOP_COMMON_TABLE_EXPRESSIONS_PREFIX = "ontop.commonTableExpressionsPrefix";
}
