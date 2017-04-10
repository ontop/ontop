package it.unibz.inf.ontop.mysql;


import it.unibz.inf.ontop.AbstractVirtualModeTest;
import org.junit.Test;

public class ViewGenerationParserTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/mysql/northwind/northwind.owl";
    static final String obdaFile = "src/test/resources/mysql/northwind/northwindComplex.obda";
    static final String propertiesFile = "src/test/resources/mysql/northwind/northwindComplex.properties";

    public ViewGenerationParserTest() {
        super(owlFile, obdaFile, propertiesFile);
    }

    public void testComplexFunction() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        countResults(query, 8);
    }

    public void testComplexFunction2() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        countResults(query, 93);
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

