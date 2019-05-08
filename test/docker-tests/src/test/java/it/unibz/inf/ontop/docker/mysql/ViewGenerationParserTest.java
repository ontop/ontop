package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;

public class ViewGenerationParserTest extends AbstractVirtualModeTest {

    static final String owlFile = "/mysql/northwind/northwind.owl";
    static final String obdaFile = "/mysql/northwind/northwindComplex.obda";
    static final String propertiesFile = "/mysql/northwind/northwindComplex.properties";

    public ViewGenerationParserTest() {
        super(owlFile, obdaFile, propertiesFile);
    }

    @Test
    public void testComplexFunction() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        countResults(query, 8);
    }
    @Test
    public void testComplexFunction2() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        // NB: Two entries are not having the required "Address" value
        countResults(query, 91);
    }

    @Test
    public void testSimpleFunction() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationRegion ?y}";
        countResults(query, 4);
    }

    @Test
    public void testStar() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :orderDetailDiscount ?y}";
        countResults(query, 830);
    }

}

