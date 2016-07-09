package it.unibz.inf.ontop.obda;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;
import org.junit.Test;

public class ViewGenerationParserTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/northwind/northwind.owl";
    static final String obdaFile = "src/test/resources/northwind/northwindComplex.obda";

    public ViewGenerationParserTest() {
        super(owlFile, obdaFile);
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

