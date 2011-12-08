package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.questdb.QuestDB;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
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

	String data = "file:/Users/mariano/Documents/Archive/Work/projects/semantic-index/uba1.7/lubm50/university-data-%s.nt";

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

		/* Preparing the database */
		QuestDB db = new QuestDB();
//		Properties cfg = new Properties();
//		cfg.load(new FileReader("/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/quest/lubmQSIEOTB.cfg"));
//		db.createClassicStore("lubm50", URI.create(ontology), cfg);

		/*
		 * Loading data and ontology, setting up reasoner and storing inferences
		 * in the db
		 */

		try {

			// uni, time for operation, tuples in operation, tuples so far,
			// times so far
//			long totalcount = 0;
//			long totaltime = 0;
//			long time = 0;
//			long count = 0;
//			for (int i = 0; i < 100; i++) {
//				long s = System.nanoTime();
//				count = db.load("lubm50", URI.create(String.format(data, i)), true);
//				long e = System.nanoTime();
//				time = e - s;
//				totalcount += count;
//				totaltime += time;
//				log.info("Commited uni {}:   Time: {} s", i, format.format((time) / 1000000000.0));
//				out.println(i + "," + format.format((time) / 1000000000.0) + "," + count + "," + totalcount + "," + format.format((totaltime) / 1000000000.0));
//				out.flush();
//			}
//			log.info("Creating indexes");
//			db.createIndexes("lubm50");

//			long stop = System.nanoTime();
//			log.info("Time to load the data: " + format.format((stop - start) / 1000000.0) + " ms");
//			out.println("Time to load the data: " + format.format((stop - start) / 1000000.0) + " ms");
//			out.flush();
//			log.info("done");

//			/* Querying the TDB database directly */
//			DecimalFormat format = new DecimalFormat("##########.###");
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

			for (int j = 0; j < 2; j++) {
				out.println("Query performance");
				for (int i = 0; i < queries.length; i++) {

					start = System.nanoTime();
					OBDAResultSet results = db.executeQuery("lubm50", queries[i]);
					try {

						int rcount = 0;
						while (results.nextRow()) {
							rcount += 1;
						}
						long end = System.nanoTime();
						log.info((i + 1) + "," + format.format((end - start) / 1000000.0) + "," + rcount);
						out.println((i + 1) + "," + format.format((end - start) / 1000000.0) + "," + rcount);
						out.flush();
					} finally {
						results.close();
						try {
							results.getStatement().close();
						} catch (Exception e) {

						}
					}
				}
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
		}
	}
}
