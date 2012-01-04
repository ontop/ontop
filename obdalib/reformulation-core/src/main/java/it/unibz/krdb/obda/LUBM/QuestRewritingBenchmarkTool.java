package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.owlapi2.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

public class QuestRewritingBenchmarkTool {

	int repeats = 1;

	long start;
	long stop;

	Logger log = org.slf4j.LoggerFactory.getLogger(QuestRewritingBenchmarkTool.class);

	public static void main(String args[]) {
		final String queries[] = new String[] {
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :GraduateStudent2xx. ?x :takesCourse2xx ?y .}",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x ?y WHERE { ?y a :University2xx. ?z a :Department2xx. ?x a :GraduateStudent2xx. ?x :memberOf2xx ?z. ?z :subOrganizationOf2xx ?y. ?x :undergraduateDegreeFrom2xx ?y.}",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Publication2xx. ?x :publicationAuthor2xx ?y . }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :Professor2xx. ?x :worksFor2xx ?y. }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Person2xx. ?x :memberOf2xx ?y . }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Student2xx . }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y WHERE { ?x a :Student2xx. ?y a :Course2xx. ?z :teacherOf2xx ?y. ?x :takesCourse2xx ?y }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y ?z WHERE { ?x a :Student2xx. ?y a :Department2xx. ?x :memberOf2xx ?y. ?y :subOrganizationOf2xx ?m . ?x :emailAddress2xx ?z . }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x ?y ?z WHERE { ?x a :Student2xx. ?y a :Faculty2xx . ?z a :Course2xx. ?x :advisor2xx ?y. ?x :takesCourse2xx ?z. ?y :teacherOf2xx ?z . }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :Student2xx . ?x :takesCourse2xx ?y }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE { ?x a :ResearchGroup2xx . ?x :subOrganizationOf2xx ?y }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x ?y WHERE {?x a :Chair2xx . ?y a :Department2xx . ?x :worksFor2xx ?y . ?y :subOrganizationOf2xx ?z }",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :Person2xx . ?y :hasAlumnus2xx ?x}",
				"PREFIX : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?x WHERE {?x a :UndergraduateStudent2xx}" };

		try {
			URI tboxuri = new File("/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/quest/lubm-lite-2X.owl")
					.toURI();
			Properties config = new Properties();

			final PrintWriter out = new PrintWriter(new File("quest-evaluation.txt"));
			final DecimalFormat format = new DecimalFormat("##########.###");

			config.load(new FileInputStream(new File(
					"/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/quest/lubmQSIEOTB.cfg")));

			long start;
			long stop;

			QuestPreferences pref = new QuestPreferences();
			pref.putAll(config);

			start = System.nanoTime();
			QuestDBClassicStore store = new QuestDBClassicStore("test", tboxuri, pref);
			stop = System.nanoTime();
			out.println("Quest initialization time: " + format.format((stop - start) / 1000000.0) + " ms");

			out.println("Query,Ref.Time (ms), Size");
			final QuestStatement st = store.getConnection().createStatement();
			for (int i = 0; i < queries.length; i++) {
				System.out.println("Query " + (i + 1));
				final CountDownLatch latch = new CountDownLatch(1);
				final int ii = i;
				Thread ref = new Thread("benchmark-thread") {
					@Override
					public void run() {
						try {
							long start = System.nanoTime();
							String rewriting = st.getRewriting(queries[ii]);
							long stop = System.nanoTime();
							String[] split = rewriting.split("\n");
							String output = "%s,%s,%s";
							out.println(String.format(output, ii + 1, format.format((stop - start) / 1000000.0), split.length));
							out.flush();
							latch.countDown();
							System.out.println();
							System.out.println(rewriting);
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
					}
				};
				ref.start();
				if (latch.await(200, TimeUnit.SECONDS)) {
					// normal, continue
				} else {
					System.err.println("Timeout");
					//timeout
					try {
						ref.stop();
					} catch (Exception e) {
						
					}
					String output = "%s,%s,%s";
					out.println(String.format(output, ii + 1, 200000, -1));
				}

			}
			st.close();
			out.flush();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
