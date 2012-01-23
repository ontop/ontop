package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.owlrefplatform.QuestDB;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

public class QuestDBBenchmarkTool {

	final String storename;
	final QuestDB dbInstance;
	final String benchmarkFile;

	final String outputPath;

	int repeats = 1;

	Logger log = org.slf4j.LoggerFactory.getLogger(QuestDBBenchmarkTool.class);

	public QuestDBBenchmarkTool(String storename, String benchmarkFile, int repeats, String outputpath) {
		this.storename = storename;
		dbInstance = new QuestDB();
		this.benchmarkFile = benchmarkFile;
		this.repeats = repeats;
		this.outputPath = outputpath;
	}

	public static void main(String[] args) {
		String[] stores = new String[] { "lubmQSIEOTB", "lubmQSIooTB", "lubmQooEOoo", "lubmQoooooo" };

		// String storename = args[0];
		String queryFile = args[1];
		// int repeats = Integer.valueOf(args[2]);
		for (int i = 0; i < stores.length; i++) {
			QuestDBBenchmarkTool bench = new QuestDBBenchmarkTool(stores[i],
					"/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/quest-cmd-lubm-queries.txt", 1,
					"/Users/mariano/Documents/Archive/Work/projects/semantic-index/lubm/benchmarks/");
			try {
				bench.startBenchmark();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void setRepeats(int repeat) {
		repeats = repeat;
	}

	public class BenchResult {
		String query = "";
		String sql = "";
		String reformulation = "";
		int reformulations = -1;
		long[] reftime = null;
		long[] exectime = null;
		long[] tuplesreturned = null;
		String queryplans = "";
	}

	public void startBenchmark() throws Exception {
		try {
			log.info("Benchmark started");
			BufferedReader reader = new BufferedReader(new FileReader(benchmarkFile));

			String line = reader.readLine();
			StringBuffer query = new StringBuffer();
			int queryCount = 0;
			List<BenchResult> resultSummary = new LinkedList<BenchResult>();
			while (line != null) {
				if (!line.startsWith("#")) {
					query.append(line + "\n");

					if (query.toString().endsWith(";\n")) {
						queryCount += 1;

						log.info("Starting benchmark for query: {}", queryCount);
						query.deleteCharAt(query.length() - 2);
						String sparqlquery = query.toString().trim();
						BenchResult result = executeQueryBench(sparqlquery);
						resultSummary.add(result);
						query = new StringBuffer();
					}
				}
				line = reader.readLine();
			}
			log.info("Writing summaries");
			outputResults(resultSummary);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				dbInstance.shutdown();
			} catch (Exception ex) {
			}
		}

	}

	public void outputResults(List<BenchResult> resultSummary) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + storename + "-totals.csv"));
		writer.append("Totals for store " + storename + "\n");
		writer.newLine();
		int qi = 0;
		writer.append("Mean values\n");
		writer.append("query | t.ex [m] | t.ref [m] | rows | t.ex [std] | t.ref [std] \n");

		DecimalFormat format = new DecimalFormat("######.###");

		for (BenchResult sumary : resultSummary) {
			qi += 1;
			writer.append(String.valueOf(qi));
			writer.append("|");
			writer.append(String.valueOf(format.format(getMean(sumary.exectime) / 1000000000.0)));
			writer.append("|");
			writer.append(String.valueOf(format.format(getMean(sumary.reftime) / 1000000000.0)));
			writer.append("|");
			writer.append(String.valueOf(sumary.tuplesreturned[0]));
			writer.append("|");
			writer.append(String.valueOf(format.format(getSTD(sumary.exectime, getMean(sumary.exectime)) / 1000000000.0)));
			writer.append("|");
			writer.append(String.valueOf(format.format(getSTD(sumary.reftime, getMean(sumary.reftime)) / 1000000000.0)));
			writer.append("\n");

		}
		qi = 0;

		writer.append("Cold values\n");
		writer.append("query | t.ex | t.ref | rows  \n");
		for (BenchResult sumary : resultSummary) {
			qi += 1;
			writer.append(String.valueOf(qi));
			writer.append("|");
			writer.append(String.valueOf(format.format(sumary.exectime[0] / 1000000000.0)));
			writer.append("|");
			writer.append(String.valueOf(format.format(sumary.reftime[0] / 1000000000.0)));
			writer.append("|");
			writer.append(String.valueOf(sumary.tuplesreturned[0]));
			writer.append("\n");
		}

		qi = 0;
		if (repeats > 1) {
			writer.append("Warm values\n");
			writer.append("query | t.ex | t.ref | rows  \n");
			for (BenchResult sumary : resultSummary) {
				qi += 1;
				writer.append(String.valueOf(qi));
				writer.append("|");

				long[] exectime = new long[repeats - 1];
				System.arraycopy(sumary.exectime, 1, exectime, 0, repeats - 1);

				writer.append(String.valueOf(format.format(getMean(exectime) / 1000000000.0)));
				writer.append("|");

				long[] reftime = new long[repeats - 1];
				System.arraycopy(sumary.reftime, 1, reftime, 0, repeats - 1);

				writer.append(String.valueOf(format.format(getMean(reftime) / 1000000000.0)));
				writer.append("|");
				writer.append(String.valueOf(sumary.tuplesreturned[0]));
				writer.append("|");
				writer.append(String.valueOf(getSTD(exectime, getMean(exectime))));
				writer.append("|");
				writer.append(String.valueOf(getSTD(reftime, getMean(reftime))));
				writer.append("\n");

			}
		}

		writer.close();

		writer = new BufferedWriter(new FileWriter(outputPath + storename + "totals-perquery.csv"));

		qi = 0;
		for (BenchResult sumary : resultSummary) {
			qi += 1;
			writer.append("####### QUERY: " + String.valueOf(qi));
			writer.append("\n\n");
			writer.append("REFORMUALTION:\n");
			writer.append(sumary.reformulation);
			writer.append("\n\nSQL:\n");
			writer.append(sumary.sql);
			// for (int i = 0; i < sumary.queryplans.length; i++) {
			writer.append("\n\nSQL PLAN :\n");
			writer.append(sumary.queryplans);
			// }
			writer.newLine();
			writer.newLine();
			writer.newLine();
		}

		writer.close();

	}

	public double getMean(long[] values) {
		long sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum / values.length;
	}

	public double getSTD(long[] values, double mean) {
		long sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += (values[i] - mean);
		}
		double meandev = sum / values.length;
		return Math.sqrt(meandev);
	}

	public BenchResult executeQueryBench(String query) throws Exception {

		BenchResult result = new BenchResult();
		result.reftime = new long[repeats];
		result.exectime = new long[repeats];
		result.tuplesreturned = new long[repeats];
		// result.queryplans = new String[repeats];
		log.info("Computing sql and reformulations");

		QuestStatement st = dbInstance.getStatement(storename);

		result.sql = st.getUnfolding(query);
		startTimer();
		result.reformulation = st.getRewriting(query);
		// ////
		result.reftime[0] = stopTimer();
		// ////
		result.query = query;
		result.reformulations = result.reformulation.split("\n").length;
		// for (int i = 0; i < this.repeats; i++) {
		// log.info("Computing execution statistics. Iteration {}/{}", i,
		// repeats);
		// startTimer();
		// dbInstance.getReformulation(storename, query);
		// result.reftime[i] = stopTimer();
		//
		// startTimer();
		// OBDAResultSet resultset = dbInstance.executeQuery(storename,
		// "/*direct*/ SELECT COUNT(*) as countbench FROM (" + result.sql
		// + ") tempbench");
		// result.exectime[i] = stopTimer();
		//
		// resultset.nextRow();
		// result.tuplesreturned[i] = resultset.getAsInteger(1);
		// resultset.close();
		// resultset.getStatement().close();
		// }
		// OBDAResultSet resultset = dbInstance.executeQuery(storename,
		// "/*direct*/ EXPLAIN (ANALYZE TRUE, COSTS TRUE, BUFFERS TRUE) SELECT COUNT(*) as countbench FROM ("
		// + result.sql
		// + ") tempbench");
		// StringBuffer queryplan = new StringBuffer();
		// while (resultset.nextRow()) {
		// queryplan.append(resultset.getAsString(1));
		// queryplan.append('\n');
		// }
		// result.queryplans = queryplan.toString();
		// resultset.close();
		// resultset.getStatement().close();
		
		st.close();
		
		return result;
	}

	long start = -1;
	long stop = -1;

	private void startTimer() {
		start = System.nanoTime();
	}

	private long stopTimer() {
		stop = System.nanoTime();
		return stop - start;
	}

}
