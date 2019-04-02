/*
 * Copyright 2014 Free University of Bozen-Bolzano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class RDF4JMetaMappingTest {

    private Connection sqlConnection;
    private RepositoryConnection conn;

    private static final String R2RML_FILE = "src/test/resources/meta/meta-mapping.ttl";
    private static final String SQL_SCRIPT = "src/test/resources/meta/meta-create.sql";
    private static final String URL = "jdbc:h2:mem:metamap";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    @Before
    public void init() throws Exception {

        sqlConnection = DriverManager.getConnection(URL, USER, PASSWORD);
        java.sql.Statement s = sqlConnection.createStatement();

        Scanner sqlFile = new Scanner(new File(SQL_SCRIPT));
        String text = sqlFile.useDelimiter("\\A").next();
        sqlFile.close();

        s.execute(text);

        s.close();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(R2RML_FILE)
                .jdbcUrl(URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopRepository repo = OntopRepository.defaultRepository(config);
        repo.initialize();
        /*
         * Prepare the data connection for querying.
         */
        conn = repo.getConnection();

    }

    @Test
    public void testSPO() throws Exception {
        String queryString = "SELECT * WHERE {s ?p ?o.}";

        // execute query
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        //assertEquals(1, count);
    }
}