package sesameWrapper.example;

import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import sesameWrapper.SesameVirtualRepo;

public class QuestSesameVirtualExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	final String owlFile = "src/main/resources/example/exampleBooks.owl";
	final String obdaFile = "src/main/resources/example/exampleBooks.obda";
	
	public void runQuery() throws Exception {

		/*
		 * Create a Quest Sesame repository with additional setup that uses no
		 * existential reasoning and the rewriting technique is using
		 * TreeWitness algorithm.
		 */
		Repository repo = new SesameVirtualRepo("virtualExample", owlFile, obdaFile, false, "TreeWitness");

		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		RepositoryConnection con = repo.getConnection();

		/*
		 * Sample query: show all books with their title.
		 */
		String sparqlQuery = 
				"PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" + 
				"SELECT ?x ?y \n" +
				"WHERE {?x a :Book; :title ?y}";
			
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
			TupleQueryResult result = tupleQuery.evaluate();
	
			/*
			 * Print out the results to the standard output
			 */
			List<String> bindingNames = result.getBindingNames();
			System.out.println(bindingNames);
	
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				boolean needSeparator = false;
				for (String binding : bindingNames) {
					if (needSeparator) {
						System.out.print(", ");
					}
					Value value = bindingSet.getValue(binding);
					System.out.print(value.toString());
					needSeparator = true;
				}
				System.out.println();
			}
	
			/*
			 * Close result set to release resources
			 */
			result.close();
		} finally {
			
			/*
			 * Finally close the connection to release resources
			 */
			if (con != null && con.isOpen()) {
				con.close();
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			QuestSesameVirtualExample example = new QuestSesameVirtualExample();
			example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
