package inf.unibz.it.obda.dependencies.miner;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.miner.exception.MiningException;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;
import inf.unibz.it.sql.parser.SimpleSQLLexer;
import inf.unibz.it.sql.parser.SimpleSQLParser;
import inf.unibz.it.sql.parser.SimpleSQLParser.ConstantSubExpression;
import inf.unibz.it.sql.parser.SimpleSQLParser.IConstant;
import inf.unibz.it.sql.parser.SimpleSQLParser.IExpression;
import inf.unibz.it.sql.parser.SimpleSQLParser.SelectItem;
import inf.unibz.it.sql.parser.SimpleSQLParser.SimpleExpression;
import inf.unibz.it.sql.parser.SimpleSQLParser.StringConstant;
import inf.unibz.it.sql.parser.SimpleSQLParser.TableSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.imp.TermFactoryImpl;


public class RDBMSFunctionalDependencyFromDBSchemaMiner implements IMiner {

	/**
	 * The API controller
	 */
	private APIController apic = null;
	/**
	 * The result set table model factory for the currently selected data source
	 */
	private DataSource	ds = null;
	/**
	 * Collection of all mapping axioms for the currently selected data source
	 */
	private final Collection<OBDAMappingAxiom> mappings;
	/**
	 * An index which links each table in the data base schema to the mapping
	 * axioms where it is used
	 */
	private HashMap<String, HashSet<OBDAMappingAxiom>> tableToMappingIndex = null;
	/**
	 * the count down signal, which tells the parent thread that the child
	 * thread has finished its work
	 */
	private CountDownLatch signal = null;
	/**
	 * A collection of all found inclusion dependencies implied by the
	 * database schema
	 */
	private HashSet<RDBMSFunctionalDependency> foundInclusions= null;
	/**
	 * The thread which does the interaction with the database.
	 */
	private Thread miningThread = null;

	/**
	 * A Map, which stores for each mapping the aliases used in the SQL query
	 */
	private Map<OBDAMappingAxiom, HashMap<String, String>> tablesAliasMap = null;

	private boolean isCanceled = false;

	private boolean hasErrorOccurred = false;

	private MiningException exception = null;

	private final TermFactoryImpl termFactory =
		(TermFactoryImpl) TermFactory.getInstance();

	/**
	 * The construction creates a new instance of the
	 * {@link RDBMSSchemaInclusionDependencyMiner}
	 *
	 * @param con the api controller
	 * @param latch the count down signal to tell the parent thread when
	 * the mining is finished
	 */
	public RDBMSFunctionalDependencyFromDBSchemaMiner(APIController con,
			CountDownLatch latch) {
		apic = con;
		signal = latch;
		foundInclusions = new HashSet<RDBMSFunctionalDependency>();
		try {
			ds= apic.getDatasourcesController().getCurrentDataSource();
			} catch (Exception e) {
			e.printStackTrace();
		}
		mappings = apic.getMappingController().getMappings(
				apic.getDatasourcesController().getCurrentDataSource().getSourceID());
		createTableIndex();
	}

	/**
	 * This Method creates the table to mapping index by parsing the
	 * SQL query of the mapping axioms.
	 */
	private void createTableIndex() {

		tableToMappingIndex = new HashMap<String, HashSet<OBDAMappingAxiom>>();
		tablesAliasMap = new HashMap<OBDAMappingAxiom, HashMap<String, String>>();
		Iterator<OBDAMappingAxiom> it = mappings.iterator();
		while(it.hasNext()){
			OBDAMappingAxiom axiom = it.next();
			RDBMSSQLQuery t = (RDBMSSQLQuery) axiom.getSourceQuery();
			SimpleSQLParser parser = parseSQLQuery(t.toString());
			Map<String,TableSource> tableMap = parser.getTableSources();
			if(tableMap != null && tableMap.size() == 1){
				Set<String> set = tableMap.keySet();
				String table = set.iterator().next();
				HashSet<OBDAMappingAxiom> aux = tableToMappingIndex.get(table);
				if(aux == null){
					aux = new HashSet<OBDAMappingAxiom>();
				}
				aux.add(axiom);
				tableToMappingIndex.put(table, aux);
				List<SelectItem> items = parser.getSelectItems();
				HashMap<String, String> aliases = new HashMap<String, String>();
				Iterator<SelectItem> items_it = items.iterator();
				while(items_it.hasNext()){
					SelectItem item = items_it.next();
					IExpression ex = item.getWhat();
					if(ex == null){
						String name = item.getName();
						aliases.put(name, name);
					}else{
						if(ex instanceof SimpleExpression){
							SimpleExpression sim = (SimpleExpression) ex;
							if(sim.getSubExpression() instanceof ConstantSubExpression){
								ConstantSubExpression sce = (ConstantSubExpression) sim.getSubExpression();
								IConstant c = sce.getConstant();
								if(c instanceof StringConstant){
									String column = ((StringConstant)c).getText();
									String alias = item.getName();
									aliases.put(column, alias);
								}
							}
						}
					}
				}
				tablesAliasMap.put(axiom, aliases);
			}
		}
	}

	/**
	 * This method parses the given SQL query and splits it in
	 * different tokens like the selected columns the table and the where
	 * clause
	 *
	 * @param input the SQL query
	 * @return a map containing information about the tables used in the sql query
	 */
	private SimpleSQLParser parseSQLQuery(String input){
		try {
			SimpleSQLParser parser = null;
			byte currentBytes[] = input.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
			ANTLRInputStream inputst = null;
			try {
				inputst = new ANTLRInputStream(byteArrayInputStream);

			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			SimpleSQLLexer lexer = new SimpleSQLLexer(inputst);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new SimpleSQLParser(tokens);
			parser.parse();
			return parser;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void startMining(){
		miningThread = new Thread(){
			@Override
			public void run(){
				try {
					mine();
					signal.countDown();
				} catch (Exception e) {
					hasErrorOccurred = true;
					exception = new MiningException("Excetpion thrown during mining process.\n" + e.getMessage());
					signal.countDown();
				}

			}
		};
		miningThread.start();
	}

	public void cancelMining(){
		if(miningThread != null){
			isCanceled = true;
			miningThread.interrupt();
		}

	}

	private void mine() throws Exception{

		RDBMSFunctionalDependencyController depcon =
			(RDBMSFunctionalDependencyController) apic.getController(RDBMSFunctionalDependency.class);
		Connection conn = getConnection();
	    DatabaseMetaData meta;
	    ResultSet r = null;
		meta = conn.getMetaData();
		String schemaPattern = "%";
		String tableNamePattern = "%";
		String types[] = { "TABLE" };
		r = meta.getTables(null, schemaPattern, tableNamePattern, types);
		ArrayList<String> v = new ArrayList<String>();
	    while (r.next() && !isCanceled) {
			 v.add(r.getString("TABLE_NAME"));
		}

	    Iterator<String> it = v.iterator();
	    while(it.hasNext() && !isCanceled){
	    	String tn1 = it.next();
	    	ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null, tn1);
	    	Vector<String> keyColumns = new Vector<String>();
	    	while(rs.next()){
	    		String column = rs.getString("COLUMN_NAME");
	    		keyColumns.add(column);
	    	}
	    	HashSet<OBDAMappingAxiom> axioms =tableToMappingIndex.get(tn1);
	    	Iterator<OBDAMappingAxiom> ax_it = axioms.iterator();
	    	while(ax_it.hasNext()){
	    		OBDAMappingAxiom ax = ax_it.next();
	    		HashMap<String,String> aliasMap =tablesAliasMap.get(ax);
		    	Vector<Term> aux1 = new Vector<Term>();
		    	Iterator<String> columns_it = keyColumns.iterator();
		    	while(columns_it.hasNext()){
		    		String alias = aliasMap.get(columns_it.next());
		    		if(alias != null){
		    			aux1.add(termFactory.createVariable(alias));
		    		}
		    	}
		    	Set<String> keyset = aliasMap.keySet();
		    	Iterator<String> keyset_it = keyset.iterator();
		    	Vector<Term> aux2 = new Vector<Term>();
		    	while(keyset_it.hasNext()){
		    		String column = keyset_it.next();
		    		if(!keyColumns.contains(column)){
		    			String alias = aliasMap.get(column);
		    			if(alias != null){
			    			aux2.add(termFactory.createVariable(alias));
		    			}
		    		}
		    	}
		    	//id one of those vector is emtpy, this means that the
		    	//column is not selected in the mapping therefor we dont add a dependency
		    	if(!aux1.isEmpty() && !aux2.isEmpty()){
	    			RDBMSFunctionalDependency func = new RDBMSFunctionalDependency(ds.getSourceID(), ax.getId(),ax.getId(),(RDBMSSQLQuery)ax.getSourceQuery(),(RDBMSSQLQuery)ax.getSourceQuery(), aux2, aux1);
	    			if(depcon.insertAssertion(func)){
	    				foundInclusions.add(func);
	    			}
    			}
	    	}

	    }

	    r.close();
//	    st.close();
	    conn.close();
	}

	public HashSet<RDBMSFunctionalDependency> getMiningResults(){
		return foundInclusions;
	}

	private Connection getConnection() throws Exception {
	    Class.forName(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
	    return DriverManager.getConnection(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_URL) + ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME), ds.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME), ds.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD));
	}

	public MiningException getException() {
		return exception;
	}

	@Override
	public boolean hasErrorOccurred() {
		return hasErrorOccurred;
	}
}
