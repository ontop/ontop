package it.unibz.inf.ontop.cli;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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

    @Test
    public void preserveNonPlainSelectsAndComplexSources() throws JSQLParserException {
        assertNotOptimised(
                "SELECT id FROM books UNION SELECT id FROM books",
                "(SELECT id FROM books)",
                "VALUES (1)",
                "WITH b AS (SELECT id FROM books) SELECT id FROM b",
                "SELECT id FROM (SELECT id FROM books) b",
                "SELECT id FROM generate_series(1, 2)",
                "SELECT b.id FROM books b JOIN authors a ON b.id = a.id",
                "SELECT id");
    }

    @Test
    public void preserveNonColumnProjections() throws JSQLParserException {
        assertNotOptimised(
                "SELECT COUNT(*) FROM books",
                "SELECT upper(title) FROM books",
                "SELECT id + 1 FROM books",
                "SELECT 1 FROM books",
                "SELECT (SELECT max(id) FROM books) FROM books",
                "SELECT id[1] FROM books",
                "SELECT id AS book_id FROM books",
                "SELECT * FROM books",
                "SELECT books.* FROM books",
                "SELECT * EXCEPT (id) FROM books",
                "SELECT books.* EXCEPT (id) FROM books",
                "SELECT * REPLACE (upper(title) AS title) FROM books",
                "SELECT books.* REPLACE (upper(title) AS title) FROM books");
    }

    @Test
    public void preserveSelectModifiers() throws JSQLParserException {
        assertNotOptimised(
                "SELECT DISTINCT id FROM books",
                "SELECT TOP 1 id FROM books",
                "SELECT AS STRUCT id FROM books",
                "SELECT AS VALUE id FROM books",
                "SELECT SQL_CALC_FOUND_ROWS id FROM books",
                "SELECT SQL_NO_CACHE id FROM books",
                "SELECT /*+ INDEX(books idx) */ id FROM books",
                "SELECT id INTO new_books FROM books",
                "SELECT id FROM books GROUP BY id",
                "SELECT id FROM books HAVING id > 1",
                "SELECT id FROM books ORDER BY id",
                "SELECT id FROM books LIMIT 1",
                "SELECT id FROM books LIMIT 1 BY id",
                "SELECT id FROM books OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY",
                "SELECT id FROM books FOR UPDATE",
                "SELECT id FROM books FOR XML PATH('x')");
    }

    @Test
    public void preserveNewSelectClauses() throws JSQLParserException {
        assertNotOptimised(
                "SELECT id FROM books FINAL",
                "SELECT id FROM ONLY books",
                "SELECT id FROM books PREWHERE id = 1",
                "SELECT id FROM books QUALIFY row_number() OVER () = 1",
                "SELECT id FROM books WINDOW w AS (PARTITION BY id)",
                "SELECT id FROM books EMIT FINAL",
                "SELECT id FROM books SETTINGS x = 1");
    }

    @Test
    public void preserveTableModifiers() throws JSQLParserException {
        assertNotOptimised(
                "SELECT id FROM books TABLESAMPLE SYSTEM (10)",
                "SELECT id FROM books AS b(id)",
                "SELECT id FROM books USE INDEX (idx)",
                "SELECT id FROM books WITH (NOLOCK)",
                "SELECT id FROM books FOR SYSTEM_TIME AS OF '2020-01-01'",
                "SELECT id FROM books b FOR SYSTEM_TIME AS OF '2020-01-01'",
                "SELECT id FROM books@remote",
                "SELECT id FROM books PIVOT (sum(id) FOR id IN (1)) p");
    }

    @Test
    public void simplifyOnlyTheOuterProjection() {
        OntopMappingV1ToV3 command = new OntopMappingV1ToV3();
        assertEquals("SELECT * FROM books WHERE title = ' id, title '",
                command.getSimplifiedProjection("SELECT id, title FROM books WHERE title = ' id, title '"));
        assertEquals("SELECT * FROM books WHERE EXISTS (SELECT id FROM authors)",
                command.getSimplifiedProjection("SELECT id FROM books WHERE EXISTS (SELECT id FROM authors)"));
        assertEquals("select\t*\r\nfrom books", command.getSimplifiedProjection("select\tid,\r\n\ttitle\r\nfrom books"));
        assertEquals("SELECT * FROM books", command.getSimplifiedProjection("SELECT \"FROM\", id FROM books"));
        assertEquals("SELECT * FROM \"books.with.dots\"",
                command.getSimplifiedProjection("SELECT id FROM \"books.with.dots\""));
        assertEquals("SELECT * FROM books b", command.getSimplifiedProjection("SELECT b.id FROM books b"));
        assertEquals("books", command.extractSimpleTable("SELECT b.id FROM books b"));
        assertNull(command.extractSimpleTable("SELECT id FROM library.books"));
        assertNull(command.extractSimpleTable("SELECT id FROM library:books"));
    }

    private void assertNotOptimised(String... queries) throws JSQLParserException {
        OntopMappingV1ToV3 command = new OntopMappingV1ToV3();
        for (String sql : queries) {
            // Ensure these regressions exercise supported ASTs, not the parse-error fallback.
            CCJSqlParserUtil.parse(sql);
            assertEquals(sql, sql, command.getSimplifiedProjection(sql));
            assertNull(sql, command.extractSimpleTable(sql));
        }
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
