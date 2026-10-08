package it.unibz.inf.ontop.cli;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OntopMappingV1ToV3Test {

    @Test
    public void simplifyProjection() {
        OntopMappingV1ToV3 command = new OntopMappingV1ToV3();
        assertEquals("SELECT * FROM books", command.getSimplifiedProjection("SELECT id, title FROM books"));
        assertEquals("SELECT id AS book_id FROM books",
                command.getSimplifiedProjection("SELECT id AS book_id FROM books"));
        assertEquals("SELECT books.* FROM books", command.getSimplifiedProjection("SELECT books.* FROM books"));
        assertEquals("SELECT * FROM books", command.getSimplifiedProjection("SELECT * FROM books"));
    }

    @Test
    public void extractSimpleTable() {
        OntopMappingV1ToV3 command = new OntopMappingV1ToV3();
        assertEquals("books", command.extractSimpleTable("SELECT id, title FROM books"));
        assertEquals("\"books.with.dots\"", command.extractSimpleTable("SELECT id FROM \"books.with.dots\""));
        assertNull(command.extractSimpleTable("SELECT * FROM books"));
        assertNull(command.extractSimpleTable("SELECT books.* FROM books"));
        assertNull(command.extractSimpleTable("SELECT id AS book_id FROM books"));
        assertNull(command.extractSimpleTable("SELECT id FROM books WHERE id = 1"));
    }

    @Ignore
    @Test
    public void testOntopHelp (){
        Ontop.main("help", "mapping", "v1-to-v3");
    }

    @Test
    public void OntopOBDACleanup (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/bootstrapped-univ-benchQL.obda",
                "-o", "src/test/resources/output/bootstrapped-univ-benchQL.obda");
    }

    @Ignore("Overwrite the file")
    @Test
    public void OntopOBDACleanupNoOutput (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/bootstrapped-univ-benchQL.obda",
                "--overwrite");
    }

    @Test
    public void OntopOBDACleanupProjection (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/books/exampleBooks.obda",
//                "--overwrite",
                "-o", "src/test/resources/output/exampleBooks-cleanup.obda",
                "--simplify-projection");
    }


    @Test
    public void OntopR2rmlCleanupAllSetting (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/npd-old-v2-ql-mysql-ontop1.17.ttl",
                "-o", "src/test/resources/output/npd-old-v2-ql-mysql-ontop1.17.ttl");
    }

    @Ignore("Overwrite the file")
    @Test
    public void OntopR2RMLCleanupNoOutput (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/npd-old-v2-ql-mysql-ontop1.17.ttl",
                "--overwrite");
    }

    @Test
    public void OntopR2rmlCleanupProjection (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "-o", "src/test/resources/output/exampleBooks.ttl",
                "--simplify-projection");
    }

    @Ignore("Overwrite the file")
    @Test
    public void OntopR2rmlCleanupOverwrite (){
        Ontop.main("mapping", "v1-to-v3",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "--overwrite");
    }
}
