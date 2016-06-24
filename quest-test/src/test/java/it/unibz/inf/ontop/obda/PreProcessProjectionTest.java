package it.unibz.inf.ontop.obda;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

public class PreProcessProjectionTest extends AbstractVirtualModeTest {

    static final String owlfile = "src/test/resources/northwind/northwind.owl";
    static final String obdafile = "src/test/resources/mappingStars.obda";

    public PreProcessProjectionTest() {
        super(owlfile, obdafile);
    }

    public void testSimpleQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Category}";
        countResults(query, 8);
    }

    public void testSimpleQueryJoin() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Customer}";
        countResults(query, 2155);
    }

    public void testSimpleQueryAlias() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationRegion ?y}";
        countResults(query, 53);
    }

    public void testSimpleQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :orderDetailDiscount ?y}";
        countResults(query, 2155);
    }

    public void testComplexQueryView() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :Location}";
        countResults(query, 91);
    }

    public void testjoinWithSameName() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x a :OrderDetail}";
        countResults(query, 4310);
    }

    public void testjoinWithAliasInSubQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/vidar/ontologies/2014/11/northwind-handmade#>" +
                " select * {?x :locationAddress ?y}";
        countResults(query, 19);
    }




}

