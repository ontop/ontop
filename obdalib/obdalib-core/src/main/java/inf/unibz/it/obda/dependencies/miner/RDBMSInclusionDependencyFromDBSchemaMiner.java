package inf.unibz.it.obda.dependencies.miner;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
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
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.TermFactoryImpl;

/**
 * Miner which takes the data base schema as base for finding
 * inclusion dependencies between tables. (e.g foreign key constraints)
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 * @author Josef Hardi <josef.hardi@unibz.it>
 *		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class RDBMSInclusionDependencyFromDBSchemaMiner implements IMiner {

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
	 * An index which links each table in the data base schema to the mapping axioms where it is used
	 */
	private HashMap<String, HashSet<OBDAMappingAxiom>> tableToMappingIndex = null;
	/**
	 * the count down signal, which tells the parent thread that the child thread has finished its work
	 */
	private CountDownLatch signal = null;
	/**
	 * A collection of all found inclusion dependencies implied by the data base schema
	 */
	private HashSet<RDBMSInclusionDependency> foundInclusions= null;
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

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	/**
	 * The constructor.
	 *
	 * @param apic 
	 *           The API controller.
	 * @param ds
	 *           The selected data source.
	 * @param signal 
	 *           The count down signal to tell the parent thread when the 
	 *           mining is finished.
	 */
	public RDBMSInclusionDependencyFromDBSchemaMiner (APIController apic, 
	    DataSource ds, CountDownLatch signal) {
	  
		this.apic = apic;
		this.ds = ds;
		this.signal = signal;
		
		foundInclusions = new HashSet<RDBMSInclusionDependency>();
		mappings = apic.getMappingController().getMappings(ds.getSourceID());
		createTableIndex();
	}

	/**
	 * This Method creates the table to mapping index by parsing the
	 * SQL query of the mapping axioms.
	 */
	private void createTableIndex(){

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
					if(item != null){
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
				} 
				catch (Exception e) {
					hasErrorOccurred = true;
					exception = new MiningException("Exception thrown during mining process.\n" + e.getMessage());
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

		RDBMSInclusionDependencyController depcon = (RDBMSInclusionDependencyController) apic.getController(RDBMSInclusionDependency.class);
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
	    	Iterator<String> it2 = v.iterator();
	    	while(it2.hasNext() && !isCanceled){
	    		String tn2 = it2.next();
	    		if(!tn1.equals(tn2)){
			    	ResultSet rs = meta.getCrossReference(conn.getCatalog(), null, tn1, conn.getCatalog(), null, tn2);
		//	    	ResultSet rs = meta.getExportedKeys(conn.getCatalog(), null, tablename);
			    	HashSet<Key> keys = new HashSet<Key>();
			    	HashSet<String> fk = null;
			    	HashSet<String> pk = null;
			    	String pkTableName = null;
			    	String pkColumnName = null;
				    String fkTableName = null;
				    String fkColumnName = null;
				     while (rs.next() && !isCanceled) {

					       String seq = rs.getString("KEY_SEQ");
					       int i = Integer.valueOf(seq);
					       if(i == 1){//1 means a new sequence of columns building a key
					    	   if(fk == null && pk == null){//if both are null it is the first sequence arriving
					    		   pkTableName = rs.getString("PKTABLE_NAME");
						    	   pkColumnName = rs.getString("PKCOLUMN_NAME");
							       fkTableName = rs.getString("FKTABLE_NAME");
							       fkColumnName = rs.getString("FKCOLUMN_NAME");
					    		   fk = new HashSet<String>();
					    		   pk = new HashSet<String>();
					    		   fk.add(fkColumnName);
					    		   pk.add(pkColumnName);
					    	   }else{// if not a other seqence is already initialized
					    		   Key aux = new Key(pkTableName, fkTableName, pk, fk); //create key with old sequence
					    		   keys.add(aux);
					    		   pkTableName = rs.getString("PKTABLE_NAME");
						    	   pkColumnName = rs.getString("PKCOLUMN_NAME");
							       fkTableName = rs.getString("FKTABLE_NAME");
							       fkColumnName = rs.getString("FKCOLUMN_NAME");
							       // start new sequence
					    		   fk = new HashSet<String>();
					    		   pk = new HashSet<String>();
					    		   fk.add(fkColumnName);
					    		   pk.add(pkColumnName);
					    	   }
					       }else{// an other columns added to the key
					    	   pkTableName = rs.getString("PKTABLE_NAME");
					    	   pkColumnName = rs.getString("PKCOLUMN_NAME");
						       fkTableName = rs.getString("FKTABLE_NAME");
						       fkColumnName = rs.getString("FKCOLUMN_NAME");
						       fk.add(fkColumnName);
				    		   pk.add(pkColumnName);
					       }
					  }
				     if(pkTableName != null && fkTableName != null && fkColumnName != null && pkColumnName != null){
					     Key aux = new Key(pkTableName, fkTableName, pk, fk); //create key with old sequence
			    		 keys.add(aux);

					     Iterator<Key> key_it = keys.iterator();
					     while(key_it.hasNext()){
					    	 Key k = key_it.next();
						     HashSet<OBDAMappingAxiom> setPK = tableToMappingIndex.get(k.getPkTable());
						     HashSet<OBDAMappingAxiom> setFK = tableToMappingIndex.get(k.getFkTable());
						     Iterator<OBDAMappingAxiom> it_setPK = setPK.iterator();
						     while(it_setPK.hasNext()){
								   OBDAMappingAxiom axiom1 = it_setPK.next();
								   Iterator<OBDAMappingAxiom> it_setFK = setFK.iterator();
								   while(it_setFK.hasNext()){
									   OBDAMappingAxiom axiom2 = it_setFK.next();
									   if(axiom1 != axiom2){
										   Set<String> set_pk = k.getPrimayKeys();
										   Set<String> set_fk = k.getForeignKeys();
										   Iterator<String> pk_it = set_pk.iterator();
										   Iterator<String> fk_it = set_fk.iterator();
										   Vector<Variable> aux1 = new Vector<Variable>();
										   Vector<Variable> aux2 = new Vector<Variable>();
										   while(pk_it.hasNext() && fk_it.hasNext()){

											   String pkName = pk_it.next();
											   String fkName = fk_it.next();

											   String column1 = null;
										       String column2 = null;
										       HashMap<String, String> aliasMapPKColumnName = tablesAliasMap.get(axiom1);
										       if(aliasMapPKColumnName == null){
										    	   column1 = pkName;
										       }else{
										    	   String alias = aliasMapPKColumnName.get(pkName);
										    	   if(alias != null){
										    		   column1 = alias;
										    	   }
										       }
										       HashMap<String,String> aliasMapFKColumnNameMap = tablesAliasMap.get(axiom2);
										       if(aliasMapFKColumnNameMap == null){
										    	   column2 = fkName;
										       }else{
										    	   String alias = aliasMapFKColumnNameMap.get(fkName);
										    	   if(alias != null){
										    		   column2 = alias;
										    	   }
										       }
										       if(column1 != null && column2 != null){
												   aux1.add(termFactory.createVariable(column1));
												   aux2.add(termFactory.createVariable(column2));
										       }
									       //if one of the both colums is null, this means that the column
									       //is not selected in the mapping an therefore no dependency is created
										   }
										   //if one of those vectors is emty his means that the column
									       //is not selected in the mapping an therefore no dependency is created
										   if(!aux1.isEmpty() && !aux2.isEmpty()){
											   RDBMSInclusionDependency dep = new RDBMSInclusionDependency(ds.getSourceID(), axiom2.getId(), axiom1.getId(),(RDBMSSQLQuery) axiom2.getSourceQuery(),(RDBMSSQLQuery)axiom1.getSourceQuery(),aux2, aux1);
											   if(depcon.insertAssertion(dep)){
											    	foundInclusions.add(dep);
											   }
									   		}
									   }
								   }
							   }
					     }
				     }
				     rs.close();
	    		}
	    	}
	    }

	    r.close();
//	    st.close();
	    conn.close();
	}

	public HashSet<RDBMSInclusionDependency> getMiningResults(){
		return foundInclusions;
	}

	@Override
	public MiningException getException() {
		return exception;
	}

	@Override
	public boolean hasErrorOccurred() {
		return hasErrorOccurred;
	}

	private Connection getConnection() throws Exception {
	    Class.forName(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
	    return DriverManager.getConnection(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_URL)+ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME), ds.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME),ds.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD));
	  }

	private class Key {
		Set<String> primayKeys = null;
		Set<String> foreignKeys = null;
		String fkTable = null;
		String pkTable = null;

		private Key(String pkt, String fkt, Set<String> pk, Set<String>fk){
			primayKeys = pk;
			foreignKeys = fk;
			fkTable = fkt;
			pkTable = pkt;
		}

		public Set<String> getPrimayKeys() {
			return primayKeys;
		}

		public Set<String> getForeignKeys() {
			return foreignKeys;
		}

		public String getFkTable() {
			return fkTable;
		}

		public String getPkTable() {
			return pkTable;
		}
	}
}
