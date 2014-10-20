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
package org.semanticweb.ontop.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.r2rml.R2RMLManager;
import org.semanticweb.ontop.sesame.RepositoryConnection;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestSesameBindings {

    static String owlfile = "src/test/resources/userconstraints/uc.owl";
    static String obdafile = "src/test/resources/userconstraints/uc.obda";
    static String r2rmlfile = "src/test/resources/userconstraints/uc.ttl";

    static String uc_keyfile = "src/test/resources/userconstraints/keys.lst";
    static String uc_create = "src/test/resources/userconstraints/create.sql";

    private Connection sqlConnection;
    private RepositoryConnection conn;

    @Before
    public void init() throws Exception {

        QuestPreferences preference;
        OWLOntology ontology;
        Model model;

        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:countries", "sa", "");
        java.sql.Statement s = sqlConnection.createStatement();

        try {
            Scanner sqlFile = new Scanner(new File(uc_create));
            String text = sqlFile.useDelimiter("\\A").next();
            sqlFile.close();

            s.execute(text);
            for (int i = 1; i <= 100; i++) {
                s.execute("INSERT INTO TABLE1 VALUES (" + i + "," + i + ");");
            }

        } catch (SQLException sqle) {
            System.out.println("Exception in creating db from script");
        }

        s.close();

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        R2RMLManager rmanager = new R2RMLManager(r2rmlfile);
        model = rmanager.getModel();

        preference = new QuestPreferences();
        preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        preference.setCurrentValueOf(QuestPreferences.DBNAME, "countries");
        preference.setCurrentValueOf(QuestPreferences.JDBC_URL, "jdbc:h2:mem:countries");
        preference.setCurrentValueOf(QuestPreferences.DBUSER, "sa");
        preference.setCurrentValueOf(QuestPreferences.DBPASSWORD, "");
        preference.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, "org.h2.Driver");

        SesameVirtualRepo repo = new SesameVirtualRepo("", ontology, model, preference);
        repo.initialize();
        /*
         * Prepare the data connection for querying.
         */
        conn = repo.getConnection();

    }

    @After
    public void tearDown() throws Exception {
        if (!sqlConnection.isClosed()) {
            java.sql.Statement s = sqlConnection.createStatement();
            try {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            } catch (SQLException sqle) {
                System.out.println("Table not found, not dropping");
            } finally {
                s.close();
                sqlConnection.close();
            }
        }
    }

    @Test
    public void testSelectBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "SELECT * WHERE {?x :hasVal1 ?v1.}";

        // execute query
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        query.setBinding("x", new URIImpl("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Class1-1"));

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        assertEquals(1, count);
    }

    @Test
    public void testSelectNoBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "SELECT * WHERE {?x :hasVal1 ?v1.}";

        // execute query
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        assertEquals(100, count);
    }

    @Test
    public void testConstructBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "CONSTRUCT {?x :hasVal1 ?v1 .} WHERE {?x :hasVal1 ?v1 .}";

        // execute query
        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
        query.setBinding("x", new URIImpl("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Class1-1"));

        GraphQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        assertEquals(1, count);
    }

    @Test
    public void testConstructNoBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "CONSTRUCT {?x :hasVal1 ?v1 .} WHERE {?x :hasVal1 ?v1 .}";

        // execute query
        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
        GraphQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        assertEquals(100, count);
    }
    
    @Test
    public void testAskBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "ASK WHERE {?x :hasVal1 ?v1 .}";

        // execute query
        BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
        query.setBinding("x", new URIImpl("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Class1-107"));

        assertFalse(query.evaluate());
    }

    @Test
    public void testAskNoBindings() throws Exception {
        String queryString
                = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> "
                + "ASK WHERE {?x :hasVal1 ?v1 .}";

        // execute query
        BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
        assertTrue(query.evaluate());
    }
}
