package inf.unibz.it.obda.constraints.miner;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;
import inf.unibz.it.sql.parser.SQLCondition;
import inf.unibz.it.sql.parser.SQLLexer;
import inf.unibz.it.sql.parser.SQLParser;
import inf.unibz.it.sql.parser.SQLQuery;
import inf.unibz.it.sql.parser.SQLSelection;
import inf.unibz.it.sql.parser.SQLTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.TermFactoryImpl;

public class AbstractAssertionMiner {

	private APIController apic = null;
	private DataSource	ds = null;
	private final Collection<OBDAMappingAxiom> mappings;
	private HashMap<String, HashSet<OBDAMappingAxiom>> tableToMappingIndex = null;
	private Map<OBDAMappingAxiom, HashMap<String, String>> tablesAliasMap = null;
	private HashMap<String, List<String>> tableColumns = null;
	private HashMap<OBDAMappingAxiom, SQLQuery> axiomToParsedQueryMap = null;
	private List<String> tables = null;
	private List<OBDAMappingAxiom> queriesWithJoins = null;
	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	public AbstractAssertionMiner(APIController con){

		apic = con;
		ds= apic.getDatasourcesController().getCurrentDataSource();
		ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
		mappings = apic.getMappingController().getMappings(apic.getDatasourcesController().getCurrentDataSource().getSourceID());
//		schemaPattern = schema;
		createTableIndex();
		retrieveTableInfo();
	}

	private void retrieveTableInfo(){
		tableColumns = new HashMap<String, List<String>>();
		tables = new Vector<String>();

		try {
			Connection con = getConnection();
			DataSource source = ds;
			DatabaseMetaData meta;
			meta = con.getMetaData();
			if(con != null){
				if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("com.ibm.db2.jcc.DB2Driver")) {

					Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					String dbname = source.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
					// jdbc:db2://5.90.168.104:50000/MINIST:currentSchema=PROP;
					String[] sp1 = dbname.split("/");
					String catalog = sp1[sp1.length - 1].split(":")[0];
					String t2 = dbname.split("=")[1];
					String schema = t2.substring(0, t2.length() - 1);
					ResultSet r = statement.executeQuery("SELECT TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_CATALOG = '" + catalog
							+ "' AND TABLE_SCHEMA = '" + schema + "'");
					makeTableMap(r, meta, con);
				} if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("oracle.jdbc.driver.OracleDriver")) {
					// select table_name from user_tables
					Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet r = statement.executeQuery("select table_name from user_tables");
					makeTableMap(r, meta, con);
				} else {
					//postgres and mysql
					DatabaseMetaData metdata = con.getMetaData();
					String catalog = null;
					String schemaPattern = "%";
					String tableNamePattern = "%";
					String types[] = { "TABLE" };
					con.setAutoCommit(true);
					ResultSet r = metdata.getTables(catalog, schemaPattern, tableNamePattern, types);
					con.setAutoCommit(false);
					makeTableMap(r, meta, con);
				}
			}else{
				throw new SQLException("No connection established for id: " + source.getSourceID());
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void makeTableMap(ResultSet r, DatabaseMetaData meta, Connection con) throws Exception{
		while (r.next()) {
			String name = r.getString("TABLE_NAME");
			 tables.add(name);
			 ResultSet rs = meta.getColumns(con.getCatalog(), "%", name, "%");

			 Vector<String> aux = new Vector<String>();
			 while(rs.next()){
				 String column = rs.getString("COLUMN_NAME");
				 aux.add(column);
			 }
			 rs.close();
			 tableColumns.put(name, aux);
		}
		r.close();
	}

	private void createTableIndex(){

		tableToMappingIndex = new HashMap<String, HashSet<OBDAMappingAxiom>>();
		tablesAliasMap = new HashMap<OBDAMappingAxiom, HashMap<String, String>>();
		axiomToParsedQueryMap = new HashMap<OBDAMappingAxiom, SQLQuery>();
		queriesWithJoins = new Vector<OBDAMappingAxiom>();
		Iterator<OBDAMappingAxiom> it = mappings.iterator();
		while(it.hasNext()){
			OBDAMappingAxiom axiom = it.next();
			RDBMSSQLQuery t = (RDBMSSQLQuery) axiom.getSourceQuery();
			SQLQuery query = parseSQLQuery(t.toString());

			if(query != null){
				axiomToParsedQueryMap.put(axiom, query);
				List<SQLTable> tables = query.getTables();
				if(tables.size() == 1){
					String table = tables.get(0).getName();
					HashSet<OBDAMappingAxiom> aux = tableToMappingIndex.get(table);
					if(aux == null){
						aux = new HashSet<OBDAMappingAxiom>();
					}
					aux.add(axiom);
					tableToMappingIndex.put(table, aux);

					List<SQLSelection> sel = query.getSelect();
					HashMap<String, String> aliases = new HashMap<String, String>();
					Iterator<SQLSelection> it1 = sel.iterator();
					while(it1.hasNext()){
						SQLSelection s = it1.next();
						if(s.getAlias() != null){
							aliases.put(s.getName(), s.getAlias());
						}
					}
					tablesAliasMap.put(axiom, aliases);
				}else if(tables.size() > 1){
					queriesWithJoins.add(axiom);
				}
			}
		}
	}

	public List<RDBMSForeignKeyConstraint> mineForeignkeyConstraints() throws Exception{
		Vector<RDBMSForeignKeyConstraint> fkconstraints = new Vector<RDBMSForeignKeyConstraint>();

		Connection conn = getConnection();
		DatabaseMetaData meta;
	    meta = conn.getMetaData();
	    Iterator<String> it = tables.iterator();
	    while(it.hasNext()){
	    	String t1 = it.next();
	    	Iterator<String> it1 = tables.iterator();
	    	while(it1.hasNext()){
	    		String t2 = it1.next();
		    	ResultSet rs = meta.getCrossReference(conn.getCatalog(), null, t1, conn.getCatalog(), null, t2);
		    	Vector<String> varoft1 = new Vector<String>();
		    	Vector<String> varoft2 = new Vector<String>();
		    	while(rs.next()){
		    		varoft1.add(rs.getString("PKCOLUMN_NAME"));
		    		varoft2.add(rs.getString("FKCOLUMN_NAME"));
		    	}
		    	if(varoft1.size() != varoft2.size()){
		    		return null;
		    	}

		    	HashSet<OBDAMappingAxiom> mappingsOfT1 = tableToMappingIndex.get(t1);
		    	HashSet<OBDAMappingAxiom> mappingsOfT2 = tableToMappingIndex.get(t2);

		    	if(mappingsOfT1 != null && mappingsOfT2 != null){
		    		Iterator<OBDAMappingAxiom> itT1 = mappingsOfT1.iterator();
			    	Iterator<OBDAMappingAxiom> itT2 = mappingsOfT2.iterator();
			    	while(itT1.hasNext()){

			    		OBDAMappingAxiom axiom1 = itT1.next();
			    		HashMap<String, String> aliases1 = tablesAliasMap.get(axiom1);
			    		Vector<Variable> vars1 = new Vector<Variable>();
			    		Iterator<String> varit1 = varoft1.iterator();
			    		while(varoft1.size() >0 && varit1.hasNext()){
			    			String name1 = varit1.next();
			    			String alias1 = aliases1.get(name1);
			    			if(alias1 == null){
			    				vars1.add(termFactory.createVariable(name1.toLowerCase()));
			    			}else{
			    				vars1.add(termFactory.createVariable(alias1));
			    			}
			    		}

			    		while(varoft2.size() >0 && itT2.hasNext()){

			    			OBDAMappingAxiom axiom2 = itT2.next();
				    		HashMap<String, String> aliases2 = tablesAliasMap.get(axiom2);
				    		Vector<Variable> vars2 = new Vector<Variable>();
				    		Iterator<String> varit2 = varoft2.iterator();
				    		while(varit2.hasNext()){
				    			String name2 = varit2.next();
				    			String alias2 = aliases2.get(name2);
				    			if(alias2 == null){
				    				vars2.add(termFactory.createVariable(name2.toLowerCase()));
				    			}else{
				    				vars2.add(termFactory.createVariable(alias2));
				    			}
				    		}
				    		if(!axiom1.getId().equals(axiom2.getId())){
				    			fkconstraints.add(new RDBMSForeignKeyConstraint(axiom2.getId(), axiom1.getId(),(RDBMSSQLQuery)axiom2.getSourceQuery(),(RDBMSSQLQuery)axiom1.getSourceQuery(),vars2, vars1));
				    		}
			    		}
			    	}
		    	}
		    	rs.close();
	    	}
	    }


	    Iterator<OBDAMappingAxiom> it3 = queriesWithJoins.iterator();
	    while(it3.hasNext()){
	    	OBDAMappingAxiom ax = it3.next();
	    	SQLQuery query = axiomToParsedQueryMap.get(ax);
	    	if(query!= null){
	    		List<SQLTable> ts = query.getTables();
	    		Iterator<SQLTable> it4 = ts.iterator();
	    		while(it4.hasNext()){
	    			String table = it4.next().getName();
	    			List<SQLCondition> con = getCondtionsForTable(query);
	    			HashSet<OBDAMappingAxiom> maps = tableToMappingIndex.get(table);
	    			if(maps != null){
	    				Iterator<OBDAMappingAxiom> it5 = maps.iterator();
	    				while(it5.hasNext()){
	    					OBDAMappingAxiom ax5 = it5.next();
	    					if(foreignKeyConstraintCanBeMade(con, ax5)){
	    						List<String> cn = tableColumns.get(table);
	    						Vector<Variable> vars1 = new Vector<Variable>();
	    						Vector<Variable> vars2 = new Vector<Variable>();
	    						HashMap<String, String> aliases1 = tablesAliasMap.get(ax);
	    						HashMap<String, String> aliases2 = tablesAliasMap.get(ax5);
	    						if(aliases1 != null && aliases2 != null){
		    						Iterator<String> cnit = cn.iterator();
		    						while(cnit.hasNext()){
		    							String name = cnit.next();
		    							String alias1 = aliases1.get(name);
		    							if(alias1 == null){
		    								alias1 = name;
		    							}
		    							String alias2 = aliases2.get(name);
		    							if(alias2 == null){
		    								alias2 = name;
		    							}
		    							vars1.add(termFactory.createVariable(alias1.toLowerCase()));
		    							vars2.add(termFactory.createVariable(alias2.toLowerCase()));
		    						}

		    						if(!ax.getId().equals(ax5.getId())){
						    			fkconstraints.add(new RDBMSForeignKeyConstraint(ax5.getId(),ax.getId(),(RDBMSSQLQuery)ax5.getSourceQuery(),(RDBMSSQLQuery)ax.getSourceQuery(),vars2, vars1));
						    		}
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
	    }

		Iterator<String> it5 = tables.iterator();
		while(it5.hasNext()){
			String table = it5.next();
			HashSet<OBDAMappingAxiom> maps = tableToMappingIndex.get(table);
			if(maps != null){
				int c = 0;
				while(true){
					List<OBDAMappingAxiom> general = findMappingWithIConditionsInSQL(maps, c);
					List<OBDAMappingAxiom> less	= findMappingsWichMoreThenIConditionsInSQL(maps, c);
					if(less.size() == 0){
						break;
					}
					Iterator<OBDAMappingAxiom> git = general.iterator();
					while(git.hasNext()){
						OBDAMappingAxiom ax1 = git.next();
						Iterator<OBDAMappingAxiom> lit = less.iterator();
						while(lit.hasNext()){
							OBDAMappingAxiom ax2 = lit.next();
							if(isMoreGeneralThen(ax1,ax2)){
								List<Variable> vars1 = new Vector<Variable>();
								List<Variable> vars2 = new Vector<Variable>();

								List<String> columnnames = tableColumns.get(table);
								Iterator<String> cit = columnnames.iterator();
								HashMap<String, String> aliasesAx1 = tablesAliasMap.get(ax1);
								HashMap<String, String> aliasesAx2 = tablesAliasMap.get(ax2);
	 							while(cit.hasNext()){
									String name = cit.next();
									String aliasAx1 = aliasesAx1.get(name);
									if(aliasAx1 == null){
										aliasAx1 = name;
									}
									String aliasAx2 = aliasesAx2.get(name);
									if(aliasAx2 == null){
										aliasAx2 = name;
									}
									vars1.add(termFactory.createVariable(aliasAx1.toLowerCase()));
									vars2.add(termFactory.createVariable(aliasAx2.toLowerCase()));
								}

								RDBMSForeignKeyConstraint dep = new RDBMSForeignKeyConstraint(ax2.getId(), ax1.getId(),(RDBMSSQLQuery) ax2.getSourceQuery(), (RDBMSSQLQuery) ax1.getSourceQuery(), vars2, vars1);;
								fkconstraints.add(dep);
							}
						}
					}
					c++;
				}
			}
		}

		return fkconstraints;
	}

	private boolean foreignKeyConstraintCanBeMade(List<SQLCondition> con, OBDAMappingAxiom ax){

		if(con == null){
			return false;
		}
		SQLQuery q = axiomToParsedQueryMap.get(ax);
		if(q != null){
			List<SQLCondition> c = q.getConditions();
			if(c.size() == 0){
				return true;
			}else if(c.size() <= con.size() ){
				Iterator<SQLCondition> it1 = c.iterator();
				boolean isTrue = true;
				while(isTrue && it1.hasNext()){
					SQLCondition c1 = it1.next();
					isTrue = con.contains(c1);
				}
				return isTrue;
			}else{
				return false;
			}
		}
		return false;
	}

	private List<SQLCondition> getCondtionsForTable(SQLQuery q){

		return q.getConditions();
	}

	public List<RDBMSPrimaryKeyConstraint> minePirmaryKeyConstraints() throws Exception{

		Vector<RDBMSPrimaryKeyConstraint> pkconstraints = new Vector<RDBMSPrimaryKeyConstraint>();
		Connection conn = getConnection();
		DatabaseMetaData meta;
		meta = conn.getMetaData();
	    Iterator<String> it = tables.iterator();
	    while(it.hasNext()){
	    	String table = it.next();
		    ResultSet rs =meta.getPrimaryKeys(conn.getCatalog(), null, table);
		    Vector<String> var = new Vector<String>();
		    while(rs.next()){
		    	var.add(rs.getString("COLUMN_NAME"));
		    }

		    HashSet<OBDAMappingAxiom> ma = tableToMappingIndex.get(table);
		    if(ma != null){
			    Iterator<OBDAMappingAxiom> mit = ma.iterator();
			    while(mit.hasNext()){
			    	OBDAMappingAxiom axiom = mit.next();
			    	HashMap<String, String> aliases = tablesAliasMap.get(axiom);
			    	Vector<Variable> vars = new Vector<Variable>();
			    	Iterator<String> varit = var.iterator();
			    	while(varit.hasNext()){
			    		String name = varit.next();
			    		String alias = aliases.get(name);
			    		if(alias == null){
			    			vars.add(termFactory.createVariable(name));
			    		}else{
			    			vars.add(termFactory.createVariable(alias));
			    		}
			    	}
			    	if(vars.size()>0){
			    		pkconstraints.add(new RDBMSPrimaryKeyConstraint(axiom.getId(), (RDBMSSQLQuery)axiom.getSourceQuery(), vars));
			    	}
			   	}
		    }
	    }
		return pkconstraints;
	}

	private boolean isMoreGeneralThen(OBDAMappingAxiom ax1, OBDAMappingAxiom ax2){

		SQLQuery q1 = axiomToParsedQueryMap.get(ax1);
		SQLQuery q2 = axiomToParsedQueryMap.get(ax2);
		List<SQLCondition> q1_con = q1.getConditions();
		List<SQLCondition> q2_con = q2.getConditions();
		boolean isTrue = true;
		Iterator<SQLCondition> it1 = q1_con.iterator();
		while(isTrue && it1.hasNext()){
			SQLCondition c1 = it1.next();
			isTrue = q2_con.contains(c1);
		}

		return isTrue;
	}

	private List<OBDAMappingAxiom> findMappingWithIConditionsInSQL(HashSet<OBDAMappingAxiom> maps, int i){

		Vector<OBDAMappingAxiom> axioms = new Vector<OBDAMappingAxiom>();
		Iterator<OBDAMappingAxiom> it = maps.iterator();
		while(it.hasNext()){
			OBDAMappingAxiom ax = it.next();
			SQLQuery query = axiomToParsedQueryMap.get(ax);
			if(query !=null){
				List<SQLCondition> c = query.getConditions();
				if(c.size() == i){
					axioms.add(ax);
				}
			}
		}

		return axioms;

	}

	private List<OBDAMappingAxiom> findMappingsWichMoreThenIConditionsInSQL(HashSet<OBDAMappingAxiom> maps, int i){

		Vector<OBDAMappingAxiom> axioms = new Vector<OBDAMappingAxiom>();
		Iterator<OBDAMappingAxiom> it = maps.iterator();
		while(it.hasNext()){
			OBDAMappingAxiom ax = it.next();
			SQLQuery query = axiomToParsedQueryMap.get(ax);
			if(query !=null){
				List<SQLCondition> c = query.getConditions();
				if(c.size() > i){
					axioms.add(ax);
				}
			}
		}

		return axioms;

	}

	private Connection getConnection() throws Exception {
	    Class.forName(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
	    return DriverManager.getConnection(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_URL)+ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME), ds.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME),ds.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD));
	}

	private SQLQuery parseSQLQuery(String input){
		try {
			SQLParser parser = null;
			byte currentBytes[] = input.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
			ANTLRInputStream inputst = null;
			try {
				inputst = new ANTLRInputStream(byteArrayInputStream);

			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			SQLLexer lexer = new SQLLexer(inputst);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new SQLParser(tokens);
			parser.parse();
			return parser.getSQLQuery();
		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}
	}
}


