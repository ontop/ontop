package it.unibz.krdb.obda.unfolding;

import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;
import it.unibz.krdb.obda.utils.DatalogDependencyGraphGenerator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Statement;

/**
 * This is a new test case for the SQL generator that handle arbitrary programs.
 * @author mrezk
 *
 */
public class LeftJoinAwareSQLGeneratorTests extends TestCase {

	private DBMetadata md = new DBMetadata();
	private OBDADataFactory fac =  OBDADataFactoryImpl.getInstance();
	private static Connection conn ;
	int resultcount = 0;
	List<String> signature = new LinkedList<String>();
	SQLGenerator gen = null;

	public void setUp() throws Exception {
		// Database schema
		/*
		TableDefinition table1 = new TableDefinition("T1");
		table1.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table1.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
	
		TableDefinition table2 = new TableDefinition("T2");
		table2.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table2.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
	
		TableDefinition table3 = new TableDefinition("T3");
		table3.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table3.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
	
		TableDefinition table4 = new TableDefinition("T4");
		table4.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table4.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
		table4.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));

		TableDefinition table5 = new TableDefinition("T5");
		table5.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table5.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
		table5.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));

		TableDefinition table6 = new TableDefinition("T6");
		table6.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
		table6.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
		table6.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));
	
	
		md.add(table1);
		md.add(table2);
		md.add(table3);
		md.add(table4);
		md.add(table5);
		md.add(table6);
		*/
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) { /* NO-OP */ }
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://10.7.20.39/leftjoin", "fish", "fish");
			md = JDBCConnectionManager.getMetaData(conn);
		} catch (SQLException e) { 
			e.printStackTrace();
		}		
		signature.add("x");

		
		SQLDialectAdapter sqladapter = SQLAdapterFactory.getSQLDialectAdapter("com.mysql.jdbc.Driver");
		JDBCUtility jdbcutil = new JDBCUtility("com.mysql.jdbc.Driver");

		 gen = new SQLGenerator(md, jdbcutil, sqladapter);

		

	}
	
	


	public void testLeftJoin() throws Exception {
		
		DatalogProgram program = fac.getDatalogProgram();
		Function intvarx = fac.getIntegerVariable("x");
		Function intvary = fac.getIntegerVariable("y");
		Function intvarz = fac.getIntegerVariable("z");
		
		List<NewLiteral> intterms = new LinkedList<NewLiteral>();
		intterms.add(intvarx);
		intterms.add(intvary);
		intterms.add(intvarz);


		/**
		 * ANS Atoms 
		 */
		Atom ans1Atom = fac.getAtom(fac.getPredicate("ans1", 1),
				intvarx);

		Atom ans2Atom = fac.getAtom(fac.getPredicate("ans2", 2), intterms);
		Atom ans3Atom = fac.getAtom(fac.getPredicate("ans3", 2),	intvarx, intvarz);
		
		
	
		
		/**
		 * Database Tables
		 */
		
		Variable varx = fac.getVariable("x");
		Variable vary = fac.getVariable("y");
		Variable varz = fac.getVariable("z");
		
		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(varx);
		terms.add(vary);
		terms.add(varz);
		
		Atom t1 = fac.getAtom(fac.getPredicate("T1", 2),varx , varz);
		Atom t2 = fac.getAtom(fac.getPredicate("T2", 2), varx,varz);
		Atom t3 = fac.getAtom(fac.getPredicate("T3", 2),varx,vary);
		Atom t4 = fac.getAtom(fac.getPredicate("T4", 3), terms);
		Atom t5 = fac.getAtom(fac.getPredicate("T5", 3), terms);
		Atom t6 = fac.getAtom(fac.getPredicate("T6", 3), terms);
		

		
		Atom lj1 = fac.getAtom(fac.getLeftJoinPredicate(), t6, ans2Atom);
		Atom lj2 = fac.getAtom(fac.getLeftJoinPredicate(), t3, ans3Atom);
		Atom jn = fac.getAtom(fac.getJoinPredicate(), t4, lj2);

		//ans1(x, y)  :- LJ(T6(x,y,z), ans2(x,y,z))
		//ans2(x,y,z) :- T5(x,y,z)
		//ans2(x,y,z) :- T4(x,y,z), LJ(T3(x,y),ans3(x,z))
		//ans3(x,z)   :- T1(x,z)
		//ans3(x,z)   :- T2(x,z)

		CQIE rule1 = fac.getCQIE(ans1Atom, lj1);
		CQIE rule2 = fac.getCQIE(ans2Atom, t5);
		CQIE rule3 = fac.getCQIE(ans2Atom, jn);
		CQIE rule4 = fac.getCQIE(ans3Atom, t1);
		CQIE rule5 = fac.getCQIE(ans3Atom, t2);
		
		program.appendRule(rule1);
		program.appendRule(rule2);
		program.appendRule(rule3);
		program.appendRule(rule4);
		program.appendRule(rule5);

		String sql = gen.generateSourceQuery(program, signature);	
		System.out.println(sql);
		System.err.println("Executing the Query");
		viewTable(sql);
		assertEquals(3, resultcount);
	}
	
	
	public  void viewTable( String query)
		    throws SQLException {
			resultcount=0;
		    Statement stmt = null;
		    try {
		        stmt = (Statement) conn.createStatement();
		        ResultSet rs = stmt.executeQuery(query);
		        while (rs.next()) {
		            String c1  = rs.getString("x");
		            System.out.println("Result: "+c1+"\n");
		            resultcount ++;
		        }
		    } catch (SQLException e ) {
		        System.out.println(e);
		    } finally {
		        if (stmt != null) { stmt.close(); }
		    }
		}
	
	
}
