package inf.unibz.it.obda.dependencies.miner;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.dependencies.miner.exception.MiningException;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.gui.swing.datasource.panels.IncrementalResultSetTableModel;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Query;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.ObjectVariableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBMSDisjointnessDependencyMiner implements IMiner {

	/**
	 *A collection of all mappings
	 */
	private final Collection<OBDAMappingAxiom> mappings;
	/**
	 * Indicates how many threads should be used to do the containment checking.
	 */
	private final int nrOfThreads = 1;
	/**
	 * This map contains for each SQL query executed by the threads the result as
	 * a boolean Object. Using this map we avoid executing the same query several
	 * times.
	 */
	private Map<String, Boolean> resultOfSQLQueries =null;
	/**
	 * A list of jobs, which has to be executed by the threads. The length of the
	 * object array can either be 3 or 4. If the length is 3 it contains at position
	 * 0 the SQL query to execute and at position 1 and 2 the TermMappings for which
	 * the check whether the TermMapping in position 1 is contained in the one at
	 * position 2.
	 * If the array length is 4 again at position 0 one can find the sql query. At
	 * Position 1 and 2 the two atoms and at pos 3 the index of the term one which
	 * the check is done.
	 */
	private List<Job> queue = null;
	/**
	 * The data source manager is used to identify the used DBMS on which it
	 * depends how the sql queries are produced.
	 */
	private CountDownLatch doneSignal = null;
	/**
	 * A logger for doing a logfile, which contains information about possible
	 * errors or exceptions
	 */
	private final Logger log = LoggerFactory.getLogger(RDBMSDisjointnessDependencyMiner.class);

	/**
	 * A set of found mining results, which later will be tranformed in
	 * inclusion dependencies
	 */
	private HashSet<DisjointnessMiningResult> foundInclusion = null;

	/**
	 * The API controller
	 */
	private APIController apic = null;

	/**
	 * A set of all target queries in the mappings
	 */
	private HashSet<Atom> splitted = null;

	/**
	 * A map which show from which mapping each target query comes from
	 */
	private HashMap<Atom, OBDAMappingAxiom> sourceQueryMap= null;

	/**
	 * A list of threads, which are execution the mining
	 */
	private List<MiningThread> threads = null;

	private boolean hasErrorOccurred = false;

	private MiningException exception = null;

	private String currentDriver = null;

	private HashSet<String> queries = null;

	private static final String ORACLE_DRIVER ="oracle.jdbc.driver.OracleDriver";
	private static final String DB2_DRIVER ="com.ibm.db2.jcc.DB2Driver";
	private static final String POSTGRES_DRIVER ="org.postgresql.Driver";
	private static final String tmp_file = "queries.tmp";

	/**
	 * Creates a new instance of the RDBMSInclusionDependencyMiner
	 *
	 * @param apic the API controller
	 * @param signal a count down latch
	 */
	public RDBMSDisjointnessDependencyMiner(APIController apic,
			CountDownLatch signal){

			doneSignal = signal;
			this.apic = apic;
			resultOfSQLQueries = new HashMap<String, Boolean>();
			mappings = apic.getMappingController().getMappings(apic.getDatasourcesController().getCurrentDataSource().getSourceID());
			queue = new Vector<Job>();
			sourceQueryMap = new HashMap<Atom, OBDAMappingAxiom>();
			foundInclusion = new HashSet<DisjointnessMiningResult>();
			DataSource ds = apic.getDatasourcesController().getCurrentDataSource();
			currentDriver = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
			readQueryFile();
			splittMappings();
			createJobs();
	}

	private void readQueryFile(){
		queries = new HashSet<String>();
		File f = new File(tmp_file);
		try {
			if(!f.exists()){

				f = File.createTempFile("queries", "tmp");
			}

			 BufferedReader input =  new BufferedReader(new FileReader(f));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){

		        	if(!line.equals("")){
		        		queries.add(line);
		        	}
		        }
		      }
		      finally {
		        input.close();
		      }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void putQueriesToFile(){

		try {
			File f = new File(tmp_file);
			if(!f.exists()){
				File.createTempFile("queries", "tmp");
				f = new File(tmp_file);
			}
			FileWriter fstream = new FileWriter(f);
	        BufferedWriter out = new BufferedWriter(fstream);
	        Iterator<String> it = queries.iterator();
	        while(it.hasNext()){
	        	String q = it.next();
	        	out.write(q +"\n");
	        }

		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Splits the target query of all mappings in it component parts.
	 */
	private void splittMappings(){

		splitted = new HashSet<Atom>();
		Iterator<OBDAMappingAxiom> it = mappings.iterator();
		while(it.hasNext()){
			OBDAMappingAxiom axiom = it.next();
			CQIEImpl q = (CQIEImpl) axiom.getTargetQuery();
			List<Atom> list = q.getBody();
			Iterator<Atom> it2 = list.iterator();
			while(it2.hasNext()){
				Atom atom = it2.next();
				splitted.add(atom);
				sourceQueryMap.put(atom, axiom);
			}
		}

	}

	/**
	 * The method goes through and tries to find possible inclusion
	 * dependencies. When it finds a two candidates it creates
	 * a job object and adds it to the queue. Later the job will
	 * be executed to whether it actually is inclusion dependency or not
	 */
	private void createJobs(){

		Iterator<Atom> it1 = splitted.iterator();
		while(it1.hasNext()){
			Atom candidate = it1.next();
			List<Term> termsOfCandidate = candidate.getTerms();
			Iterator<Term> canIt = termsOfCandidate.iterator();
			OBDAMappingAxiom axForCan = sourceQueryMap.get(candidate);
			while(canIt.hasNext()){
				Term canTerm = canIt.next();
				Iterator<Atom> it2 = splitted.iterator();
				while(it2.hasNext()){
					Atom container = it2.next();
					OBDAMappingAxiom axForCon = sourceQueryMap.get(container);
					Query sqCan = axForCan.getSourceQuery();
					Query sqCon = axForCon.getSourceQuery();
					if(container != candidate && sqCan != sqCon){
						List<Term> termsOfContainer = container.getTerms();
						Iterator<Term> conIt = termsOfContainer.iterator();
						while(conIt.hasNext()){
							Term containerTerm = conIt.next();
							String sql = checkContainment(axForCan.getSourceQuery().toString(), axForCon.getSourceQuery().toString(), canTerm, containerTerm);
							if(sql != null){
								Job aux = new Job(sql,axForCan.getId(), axForCon.getId(),(RDBMSSQLQuery)axForCan.getSourceQuery(), canTerm,(RDBMSSQLQuery) axForCon.getSourceQuery(), containerTerm);
								queue.add(aux);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * The method takes the queue of jobs, divides them in to equal parts
	 * and passes them to different threads, which executes the jobs.
	 */
	public void startMining(){

		threads = new Vector<MiningThread>();
		int qLength = queue.size()/nrOfThreads;
		log.debug("Number of queries to execute:" + queue.size());
		Random random = new Random();
		// makes n-1 threads
		for(int i=0; i<nrOfThreads-1; i++){
			Vector<Job> auxQueue = new Vector<Job>();
			for (int j=0; j<qLength; j++){
				int index = random.nextInt(queue.size());
				auxQueue.add(queue.get(index));
				queue.remove(index);
			}
			MiningThread thread = new MiningThread(auxQueue, doneSignal);
			threads.add(thread);
			thread.start();
		}
		// makes the n-th thread an assigns it all remaining jobs
		MiningThread thread = new MiningThread(queue,doneSignal);
		threads.add(thread);
		thread.start();
	}

	/**
	 * Interrupts all started Mining threads
	 */
	public void cancelMining(){
		Iterator<MiningThread> it= threads.iterator();
		while(it.hasNext()){
			it.next().interrupt();
			doneSignal.countDown();
		}
//		foundInclusion = new HashSet<DisjointnessMiningResult>();
	}

	/**
	 * private method which check whether tm1 can be included in tm2
	 * according to the formulation of their mapping axioms
	 *
	 * @param m1 mapping where tm1 comes from
	 * @param m2 mapping where tm2 comes from
	 * @param tm1 first candidate term
	 * @param tm2 second candidate term
	 * @return null if not possible, an query to check the dependency on the data in the source
	 */
	private String checkContainment (String m1, String m2, Term tm1, Term tm2){

		if(m1.equals(m2)){
			return null;
		}
		if(tm1 instanceof ObjectVariableImpl && tm2 instanceof ObjectVariableImpl){
			ObjectVariableImpl ft1 = (ObjectVariableImpl) tm1;
			ObjectVariableImpl ft2 = (ObjectVariableImpl) tm2;
			if(ft1.getName().equals(ft2.getName()) && ft1.getTerms().size() == ft2.getTerms().size()){ // TODO Check getName is a URI.

				if(currentDriver.equals(DB2_DRIVER)){
					return produceSQLForDB2(m1,m2,ft1,ft2);
				}else if(currentDriver.equals(ORACLE_DRIVER)){
					return produceSQLForOracle(m1, m2, ft1, ft2);
				}else if (currentDriver.equals(POSTGRES_DRIVER)){
					return produceSQLForPostgres(m1, m2, ft1, ft2);
				}else{
					return null;
				}
			}else {
				return null;
			}
		}else {
			return null;
		}
	}

	/**
	 * Returns the query which can be use to check the dependency on the
	 * data in the source if the involved terms are functional terms.
	 */
	private String produceSQLForDB2(String candidate, String container, ObjectVariableImpl ft1, ObjectVariableImpl ft2){

		List<Term> termsOfFT1 = ft1.getTerms();
		List<Term> termsOfFT2 = ft2.getTerms();
		String var1 = "";
		Iterator<Term> it = termsOfFT1.iterator();
		while(it.hasNext()){
			if(var1.length() >0){
				var1 = var1 +", ";
			}
			var1 = var1 + "table1."+it.next().getName();
		}
		String var2 = "";
		Iterator<Term> it1 = termsOfFT2.iterator();
		while(it1.hasNext()){
			if(var2.length() >0){
				var2 = var2 +", ";
			}
			var2 = var2 +  "table2."+it1.next().getName();
		}

		String query = "SELECT " + var1 +" FROM (" + candidate + ") table1 WHERE ("+
						var1+") IN (SELECT " + var2 + " FROM (" + container +") table2)";
		return query;
	}

	private String produceSQLForOracle(String candidate, String container, ObjectVariableImpl ft1, ObjectVariableImpl ft2){

		List<Term> termsOfFT1 =ft1.getTerms();
		List<Term> termsOfFT2 =ft2.getTerms();
		String var1 = "";
		Iterator<Term> it = termsOfFT1.iterator();
		while(it.hasNext()){
			if(var1.length() >0){
				var1 = var1 +", ";
			}
			var1 = var1 + "table1."+it.next().getName();
		}
		String var2 = "";
		Iterator<Term> it1 = termsOfFT2.iterator();
		while(it1.hasNext()){
			if(var2.length() >0){
				var2 = var2 +", ";
			}
			var2  = var2 + "table2."+it1.next().getName();
		}

		String query = "SELECT " + var1 +" FROM (" + candidate + ") table1 WHERE ("+
						var1+") IN (SELECT " + var2 + " FROM (" + container +") table2)";
		return query;
	}


	private String produceSQLForPostgres(String candidate, String container, ObjectVariableImpl ft1, ObjectVariableImpl ft2){

		List<Term> termsOfFT1 = ft1.getTerms();
		List<Term> termsOfFT2 = ft2.getTerms();
		String var1 = "";
		Iterator<Term> it = termsOfFT1.iterator();
		while(it.hasNext()){
			if(var1.length() >0){
				var1 = var1 +",";
			}
			var1 = var1 + "table1."+it.next().getName();
		}
		String var2 = "";
		Iterator<Term> it1 = termsOfFT2.iterator();
		while(it1.hasNext()){
			if(var2.length() >0){
				var2 =var2+",";
			}
			var2 = var2 + "table2."+it1.next().getName();
		}

		String query = "SELECT " + var1 +" FROM (" + candidate + ") table1 WHERE ROW("+
						var1+") IN (SELECT " + var2 + " FROM (" + container +") table2) LIMIT 1";
		return query;
	}
	/**
	 * Returns the results of the mining
	 * @return set of mining results
	 */
	public HashSet<DisjointnessMiningResult> getFoundInclusionDependencies() {
		log.debug("Found disjointness assertions: "+foundInclusion.size());
		putQueriesToFile();
		return foundInclusion;
	}

	@Override
	public MiningException getException() {
		return exception;
	}

	@Override
	public boolean hasErrorOccurred() {
		return hasErrorOccurred;
	}

	/**
	 * Class representing a mining result. It contains all
	 * necessary information to create inclusion dependency, which
	 * can be used by the reasoner.
	 *
	 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
	 *
	 */
	public class DisjointnessMiningResult{

		/**
		 * The frist query
		 */
		private RDBMSSQLQuery firstMappingElement = null;
		/**
		 *the second query
		 */
		private RDBMSSQLQuery secondMappingElement = null;
		/**
		 * the query term associated to the first query
		 */
		private Term firstPositionElement = null;
		/**
		 * the query term associated to the second query
		 */
		private Term secondPositionElement = null;
		/**
		 * the mapping id of the mapping where the first query comes from
		 */
		private String mappingIdOfFirstMapping = null;
		/**
		 * the mapping id of the mapping where the first query comes from
		 *
		 */
		private String mappingIdOfSecondMapping = null;

		/**
		 * Return a new MiningResult object
		 *
		 * @param id1 first mapping id
		 * @param id2 second mapping id
		 * @param map1	first query
		 * @param pos1	term associated to the first query
		 * @param map2	second query
		 * @param pos2	term associated to the second query
		 */
		private DisjointnessMiningResult(String id1, String id2, RDBMSSQLQuery map1, Term pos1,
				RDBMSSQLQuery map2, Term pos2){

			firstMappingElement = map1;
			secondMappingElement = map2;
			firstPositionElement = pos1;
			secondPositionElement = pos2;
			mappingIdOfFirstMapping = id1;
			mappingIdOfSecondMapping = id2;
		}

		/**
		 * Returns the first query
		 * @return first query
		 */
		public RDBMSSQLQuery getFirstMappingElement() {
			return firstMappingElement;
		}
		/**
		 * Returns the second query
		 * @return second query
		 */
		public RDBMSSQLQuery geSeecondMappingElement() {
			return secondMappingElement;
		}
		/**
		 * Returns the term associated to the first query
		 * @return term associated to first query
		 */
		public Term getFirstElement() {
			return firstPositionElement;
		}
		/**
		 * Returns the term associated to the second query
		 * @return term associated to second query
		 */
		public Term getSecondElement() {
			return secondPositionElement;
		}
		/**
		 * Returns the mapping id where the first query comes from
		 * @return mapping id where the first query comes from
		 */
		public String getMappingIdOfFirstMapping() {
			return mappingIdOfFirstMapping;
		}
		/**
		 * Returns the mapping id where the second query comes from
		 * @return mapping id where the second query comes from
		 */
		public String getMappingIdOfSecondMapping() {
			return mappingIdOfSecondMapping;
		}
	}

	/**
	 * Class representing a job, which will be executed by a Mining Thread
	 *
	 * @author Manfred Gerstgrasser
	 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
	 *
	 */
	private class Job {

		/**
		 * the query to execute
		 */
		private String sqlquery = null;
		/**
		 * the first query
		 */
		private RDBMSSQLQuery candidate = null;
		/**
		 * the second query
		 */
		private RDBMSSQLQuery container = null;
		/**
		 * the term associated to the first query
		 */
		private Term candidateTerm = null;
		/**
		 * the term associated to the second query
		 */
		private Term containerTerm = null;
		/**
		 * the id of the mapping where the first query comes from
		 */
		private String firstMappingID =null;
		/**
		 * the id of the mapping where the second query comes from
		 */
		private String secondMappingID=null;

		/**
		 * Returns a new Job object
		 *
		 * @param sql	the query to execute
		 * @param id1	id of first mapping
		 * @param id2	id of second mapping
		 * @param can	the first query
		 * @param posCan term associated to first query
		 * @param con	the second query
		 * @param posCon	term associated to second query
		 */
		private Job(String sql, String id1, String id2, RDBMSSQLQuery can, Term posCan,
				RDBMSSQLQuery con, Term posCon){

			sqlquery = sql;
			candidate = can;
			container = con;
			candidateTerm = posCan;
			containerTerm = posCon;
			firstMappingID = id1;
			secondMappingID = id2;
		}

		/**
		 * Returns the query to execute
		 * @return query to execute
		 */
		public String getSqlquery() {
			return sqlquery;
		}
		/**
		 * Returns the first query
		 * @return first query
		 */
		public RDBMSSQLQuery getCandidate() {
			return candidate;
		}
		/**
		 * Returns the term associated to the first query
		 * @return second query
		 */
		public Term getCandidateTerm() {
			return candidateTerm;
		}
		/**
		 * Returns the term associated to the second query
		 * @return the term associated to the second query
		 */
		public Term getContainerTerm() {
			return containerTerm;
		}
		/**
		 * Returns the second query
		 * @return second query
		 */
		public RDBMSSQLQuery getContainer() {
			return container;
		}
		/**
		 * Returns the id of the mapping where the first query comes from
		 * @return	id of first mapping
		 */
		public String getFirstMappingID() {
			return firstMappingID;
		}
		/**
		 * Returns the id of the mapping where the second query comes from
		 * @return	id of second mapping
		 */
		public String getSecondMappingID() {
			return secondMappingID;
		}

	}
//
//
//
//	// This class extends the class java.lang.Thread and executes the jobs assigned to it
//	// by executing the sql query, checking the result and if necessary it inserts or
//	// updating an entry in the corresponding index
	private class MiningThread extends Thread{

		/**
		 * The list of jobs the threads has to do.
		 */
		private List<Job> jobs = null;
		/**
		 * The count down signal
		 */
		private final CountDownLatch signal;
		/**
		 * the result set model factory
		 */
		private final JDBCConnectionManager	man	= null;
		/**
		 * the result set model
		 */
		private final IncrementalResultSetTableModel	model			= null;

		/**
		 * returns a new Mining Thread
		 * @param obj list of jobs to execute
		 * @param latch the count down signal
		 */
		private MiningThread (List<Job> obj, CountDownLatch latch){
			jobs = obj;
			this.signal = latch;
		}

		@Override
		public void run(){
			int i = 0;
			JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
			try {
				man.setProperty(JDBCConnectionManager.JDBC_AUTOCOMMIT, true);
				man.setProperty(JDBCConnectionManager.JDBC_RESULTSETTYPE, ResultSet.TYPE_SCROLL_INSENSITIVE);
				Iterator<Job> it = jobs.iterator();
				while(it.hasNext()){
					Job job = it.next();
					String sql = job.getSqlquery(); //get the sql query
					log.debug(sql);
					synchronized(resultOfSQLQueries){
						if(queries.add(sql)){
							try {
									DataSource ds = apic.getDatasourcesController().getCurrentDataSource();
									if(!man.isConnectionAlive(ds.getSourceID())){
										man.createConnection(ds);
									}
//									if (model != null) {
//
//										IncrementalResultSetTableModel rstm = (IncrementalResultSetTableModel) model;
//										rstm.close();
//									}
									ResultSet set =  man.executeQuery(ds.getSourceID(), sql, ds);
									synchronized(resultOfSQLQueries){
										if(set.next()){
	//										System.out.print(model.getRowCount());
											resultOfSQLQueries.put(sql, new Boolean("false"));
										}else{
											RDBMSSQLQuery mapCandidate = job.getCandidate();
											RDBMSSQLQuery mapContainer = job.getContainer();
											String id1 = job.getFirstMappingID();
											String id2 = job.getSecondMappingID();
											Term candidateTerm = job.getCandidateTerm();
											Term containerTerm = job.getContainerTerm();
											DisjointnessMiningResult entry = new DisjointnessMiningResult(id1, id2, mapCandidate,candidateTerm,mapContainer,containerTerm);
											synchronized(foundInclusion){
												foundInclusion.add(entry);
											}
											resultOfSQLQueries.put(sql, new Boolean("true"));
										}
									}
							} catch (SQLException e) {
								e.printStackTrace();
								resultOfSQLQueries.put(sql, new Boolean("false"));
							}
						}
					}
					i++;
					int a = i%10;
					if(a == 0){
						log.debug(i + "/" + jobs.size());
					}
				}
				signal.countDown();//notify the Latch that the thread has finished
//				log.debug("Thead ended");
			} catch (Exception e) {
				signal.countDown();
				hasErrorOccurred = true;
				exception = new MiningException("Excetpion thrown during mining process.\n" + e.getMessage());
			}
		}
	}


}
