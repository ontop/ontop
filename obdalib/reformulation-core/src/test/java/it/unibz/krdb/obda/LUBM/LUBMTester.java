package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LUBMTester {
    private static final Logger log = LoggerFactory.getLogger(LUBMTester.class);

    static long starttime;
    static long endtime;


    public static void main(String[] args) throws Exception {
        int universityCount = 1;
        String dataDirectory = "./";

        starttime = System.nanoTime();
        DAG dag = new DAG(TBoxLoader.loadOnto(dataDirectory));
        endtime = System.nanoTime();
        log.info("Building DAG took: {}", (endtime - starttime) * 1.0e-9);

        starttime = System.nanoTime();
        dag.index();
        endtime = System.nanoTime();
        log.info("Indexing DAG took: {}", (endtime - starttime) * 1.0e-9);

        CSVDumper dumper = new CSVDumper(dag, dataDirectory);
        CSVLoader loader = new CSVLoader(dataDirectory);

//        genData(universityCount);
        dumper.dump(universityCount);

        loader.recreateDB();
        loader.loadData();
    }

//    private static void genData(int uniCount) {
//        starttime = System.nanoTime();
//        Generator generator = new Generator();
//        generator.start(uniCount, 0, 0, false, "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");
//        endtime = System.nanoTime();
//        log.info("Generating Data took: {}", (endtime - starttime) * 1.0e-9);
//
//    }

}
