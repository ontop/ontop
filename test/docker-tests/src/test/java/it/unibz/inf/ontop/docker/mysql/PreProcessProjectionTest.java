package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.LegacyMappingDatatypeFiller;
import org.junit.Test;

public class PreProcessProjectionTest extends AbstractVirtualModeTest {

    static final String owlfile = "/mysql/northwind/northwind.owl";
    static final String obdafile = "/mysql/northwind/mappingStars.obda";
    static final String propertiesfile = "/mysql/northwind/mapping-northwind.properties";

    public PreProcessProjectionTest() { super(owlfile, obdafile, propertiesfile); }
    @Test
    public void testSimpleQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        countResults(query, 8);
    }
    @Test
    public void testSimpleQueryJoin() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        countResults(query, 2155);
    }
    @Test
    public void testSimpleQueryAlias() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationRegion ?y}";
        countResults(query, 53);
    }
    @Test
    public void testSimpleQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :orderDetailDiscount ?y}";
        countResults(query, 2155);
    }
    @Test
    public void testComplexQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Location}";
        countResults(query, 53);
    }

    @Test
    public void testjoinWithAliasInSubQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationAddress ?y}";
        countResults(query, 19);
    }




}

