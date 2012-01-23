package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.owlrefplatform.QuestDB;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.slf4j.Logger;

public class QuestDBBenchmarkFinal {
	DecimalFormat format = new DecimalFormat("##########.###");
	Logger log = org.slf4j.LoggerFactory.getLogger(QuestDBBenchmarkFinal.class);

	String ontology = "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/jena/lubm3x-lite-rdfxml.owl";
	// String ontology =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/univ-bench-original.owl";
	String ontouri = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";

	// String data =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm10/university-data-10univ.nt";
	// String data =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm10/university-data-1univ.nt";
	// String data =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm50/University0-99-clean2.nt";
	// String data =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm50/university-data-100univ.nt";

	String data = "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm100/nt/university-data-%s.nt";

	// String data =
	// "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm10/university-data-10univ.nt";

	PrintWriter out;

	public QuestDBBenchmarkFinal() throws Exception {
		out = new PrintWriter(new File("summary-results-n.txt"));
	}

	public void stop() {
		out.flush();
		out.close();
	}

	public void infer() throws Exception {

		log.info("Loading data and ontology (1 unis)");
		long start = System.nanoTime();

		boolean createdb = false;
		boolean query = true;

		/* Preparing the database */
		QuestDB db = new QuestDB();

		if (createdb) {
			Properties cfg = new Properties();
			cfg.load(new FileReader("/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/quest/mysql/lubmQSIEOTB.cfg"));
			db.createClassicStore("lubm50myisam", URI.create(ontology), cfg);
		}

		/*
		 * Loading data and ontology, setting up reasoner and storing inferences
		 * in the db
		 */

		try {

			if (createdb) {
				// uni, time for operation, tuples in operation, tuples so far,
				// times so far
				long totalcount = 0;
				long totaltime = 0;
				long time = 0;
				long count = 0;
				for (int i = 0; i < 50; i++) {
					long s = System.nanoTime();
					count = db.load("lubm50myisam", URI.create(String.format(data, i)), false);
					long e = System.nanoTime();
					time = e - s;
					totalcount += count;
					totaltime += time;
					log.info("Commited uni {}:   Time: {} s", i, format.format((time) / 1000000000.0));
					out.println(i + "," + format.format((time) / 1000000000.0) + "," + count + "," + totalcount + ","
							+ format.format((totaltime) / 1000000000.0));
					out.flush();
				}
				log.info("Creating indexes");
				db.createIndexes("lubm50myisam");
//				db.analyze("lubm50myisam");

				long stop = System.nanoTime();
				log.info("Time to load the data: " + format.format((stop - start) / 1000000.0) + " ms");
				out.println("Time to load the data: " + format.format((stop - start) / 1000000.0) + " ms");
				out.flush();
				log.info("done");
			}
			
			

			if (query) {

				// /* Querying the TDB database directly */
				DecimalFormat format = new DecimalFormat("##########.###");
				//
				String queries[] = new String[] {
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :GraduateStudent2xx. ?x :takesCourse2xx <http://www.Department0.University0.edu/GraduateCourse0> .}",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y WHERE { ?y a :University2xx. ?z a :Department2xx. ?x a :GraduateStudent2xx. ?x :memberOf2xx ?z. ?z :subOrganizationOf2xx ?y. ?x :undergraduateDegreeFrom2xx ?y.}",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Publication2xx. ?x :publicationAuthor2xx <http://www.Department0.University0.edu/AssistantProfessor0> . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :Professor2xx. ?x :worksFor2xx <http://www.Department0.University0.edu> . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Person2xx. ?x :memberOf2xx <http://www.Department0.University0.edu> . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Student2xx . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y WHERE { ?x a :Student2xx. ?y a :Course2xx. <http://www.Department0.University0.edu/AssociateProfessor0> :teacherOf2xx ?y. ?x :takesCourse2xx ?y }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y ?z WHERE { ?x a :Student2xx. ?y a :Department2xx. ?x :memberOf2xx ?y. ?y :subOrganizationOf2xx <http://www.University0.edu> . ?x :emailAddress2xx ?z . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y ?z WHERE { ?x a :Student2xx. ?y a :Faculty2xx . ?z a :Course2xx. ?x :advisor2xx ?y. ?x :takesCourse2xx ?z. ?y :teacherOf2xx ?z . }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Student2xx . ?x :takesCourse2xx <http://www.Department0.University0.edu/GraduateCourse0> }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :ResearchGroup2xx . ?x :subOrganizationOf2xx <http://www.University0.edu> }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y WHERE {?x a :Chair2xx . ?y a :Department2xx . ?x :worksFor2xx ?y . ?y :subOrganizationOf2xx <http://www.University0.edu> }",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :Person2xx . <http://www.University0.edu> :hasAlumnus2xx ?x}",
						"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :UndergraduateStudent2xx}" };

				log.info("Executing queries");

				QuestStatement st = db.getStatement("lubm50myisam");

				for (int j = 0; j < 1; j++) {
					log.info("Query performance. Run: " + j + "\n");
					out.println("Query performance. Run: " + j + "\n");
					long total = 0;
					for (int i = 0; i < queries.length; i++) {

						start = System.nanoTime();
						String sql = st.getUnfolding(queries[i]);
						log.info("Query {} SQL:", i);
						log.info(sql);
//						OBDAResultSet results = db.executeQuery("lubm50myisam", queries[i]);
//						try {
//
//							int rcount = 0;
//							while (results.nextRow()) {
//								rcount += 1;
//							}
//							long end = System.nanoTime();
//
//							total += end - start;
//							log.info((i + 1) + "," + format.format((end - start) / 1000000.0) + "," + rcount);
//							out.println((i + 1) + "," + format.format((end - start) / 1000000.0) + "," + rcount);
//							out.flush();
//						} finally {
//							results.close();
//							try {
//								results.getStatement().close();
//							} catch (Exception e) {
//
//							}
//						}
					}
					log.info("Total: {}", format.format((total) / 1000000.0));

				}
				st.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			out.flush();
			try {
				db.shutdown();
			} catch (Exception e) {
			}
			log.info("Done");
		}

	}

	public void query() throws Exception {
		try {

			// PrintWriter out = new PrintWriter("summary-results-n.txt",);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {
		try {
			QuestDBBenchmarkFinal bench = new QuestDBBenchmarkFinal();
			bench.infer();
			// bench.query();
			bench.stop();
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof SQLException) {
				SQLException se = ((SQLException) e).getNextException();
				while (se != null) {
					System.err.println(se.getMessage());
					se = se.getNextException();
				}
			}
		}
	}
}
