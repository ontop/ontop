package it.unibz.inf.ontop.docker.failing.local;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

import java.io.FileReader;

/**
 *
 * @author 
 */
public class ADPOntopTest extends AbstractVirtualModeTest  {
	
	private static final String owlFile = "/local/adp/npd-ql.owl";
	private static final String obdaFile = "/local/adp/mapping-fed.obda";
	private static final String queryFile = "/local/adp/01.q";
	private static final String propertyFile = "/local/adp/mapping-fed.properties";

	public void runQuery() throws Exception {
		try (EngineConnection connection = createReasoner(owlFile, obdaFile, propertyFile);
			 OWLStatement st = connection.createStatement()) {
			String sparqlQuery = Joiner.on("\n").join(
					CharStreams.readLines(new FileReader(queryFile)));
			try (TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery)) {
				while (rs.hasNext()) {
					final OWLBindingSet bindingSet = rs.next();
					System.out.print(bindingSet + "\n");
				}
			}

			/*
			 * Print the query summary
			 */
			OntopOWLStatement qst = (OntopOWLStatement) st;

			System.out.println();
			System.out.println("The input SPARQL query:");
			System.out.println("=======================");
			System.out.println(sparqlQuery);
			System.out.println();

			System.out.println("The output SQL query:");
			System.out.println("=====================");
			System.out.println(qst.getExecutableQuery(sparqlQuery));
		}
	}

	public static void main(String[] args) throws Exception {
		new ADPOntopTest().runQuery();
	}

	@Override
	protected OntopOWLStatement createStatement()  {
		throw new IllegalStateException("should never be called");
	}
}
