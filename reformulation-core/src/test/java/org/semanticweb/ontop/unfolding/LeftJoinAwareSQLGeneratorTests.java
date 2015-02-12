package org.semanticweb.ontop.unfolding;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import org.semanticweb.ontop.owlrefplatform.core.sql.SQLGenerator;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.JDBCConnectionManager;

import junit.framework.TestCase;

import com.mysql.jdbc.Statement;

/**
 * This is a new test case for the SQL generator that handle arbitrary programs.
 * @author mrezk
 *
 */
//@Ignore("check if we still need it or not")
//public class LeftJoinAwareSQLGeneratorTests extends TestCase {
//
//	private DBMetadata md = new DBMetadata();
//	private OBDADataFactory fac =  OBDADataFactoryImpl.getInstance();
//	private static Connection conn ;
//	int resultcount = 0;
//	List<String> signature = new LinkedList<String>();
//	SQLGenerator gen = null;
//
//	public void setUp() throws Exception {
//		// Database schema
//		/*
//		TableDefinition table1 = new TableDefinition("T1");
//		table1.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table1.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//
//		TableDefinition table2 = new TableDefinition("T2");
//		table2.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table2.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//
//		TableDefinition table3 = new TableDefinition("T3");
//		table3.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table3.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//
//		TableDefinition table4 = new TableDefinition("T4");
//		table4.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table4.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//		table4.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));
//
//		TableDefinition table5 = new TableDefinition("T5");
//		table5.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table5.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//		table5.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));
//
//		TableDefinition table6 = new TableDefinition("T6");
//		table6.setAttribute(1, new Attribute("c1", Types.INTEGER, true, null));
//		table6.setAttribute(2, new Attribute("c2", Types.INTEGER, false, null));
//		table6.setAttribute(3, new Attribute("c3", Types.INTEGER, false, null));
//
//
//		md.add(table1);
//		md.add(table2);
//		md.add(table3);
//		md.add(table4);
//		md.add(table5);
//		md.add(table6);
//		*/
//
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//		}
//		catch (ClassNotFoundException e) { /* NO-OP */ }
//
//		try {
//			conn = DriverManager.getConnection("jdbc:mysql://10.7.20.39/leftjoin", "fish", "fish");
//			md = JDBCConnectionManager.getMetaData(conn);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		signature.add("x");
//
//
//		SQLDialectAdapter sqladapter = SQLAdapterFactory.getSQLDialectAdapter("com.mysql.jdbc.Driver");
//
//		 //gen = new SQLGenerator(md, sqladapter);
//
//
//
//	}
//
//
//
//
//	public void testLeftJoin() throws Exception {
//
//		DatalogProgram program = fac.getDatalogProgram();
//		Function intvarx = fac.getIntegerVariable("x");
//		Function intvary = fac.getIntegerVariable("y");
//		Function intvarz = fac.getIntegerVariable("z");
//
//		List<Term> intterms = new LinkedList<Term>();
//		intterms.add(intvarx);
//		intterms.add(intvary);
//		intterms.add(intvarz);
//
//
//		/**
//		 * ANS Functions
//		 */
//		Function ans1Function = fac.getFunction(fac.getPredicate("ans1", 1),
//				intvarx);
//
//		Function ans2Function = fac.getFunction(fac.getPredicate("ans2", 2), intterms);
//		Function ans3Function = fac.getFunction(fac.getPredicate("ans3", 2),	intvarx, intvarz);
//
//
//		/**
//		 * Database Tables
//		 */
//		Variable varx = fac.getVariable("x");
//		Variable vary = fac.getVariable("y");
//		Variable varz = fac.getVariable("z");
//
//		List<Term> terms = new LinkedList<Term>();
//		terms.add(varx);
//		terms.add(vary);
//		terms.add(varz);
//
//		Function t1 = fac.getFunction(fac.getPredicate("T1", 2),varx , varz);
//		Function t2 = fac.getFunction(fac.getPredicate("T2", 2), varx,varz);
//		Function t3 = fac.getFunction(fac.getPredicate("T3", 2),varx,vary);
//		Function t4 = fac.getFunction(fac.getPredicate("T4", 3), terms);
//		Function t5 = fac.getFunction(fac.getPredicate("T5", 3), terms);
//		Function t6 = fac.getFunction(fac.getPredicate("T6", 3), terms);
//
//
//
//		Function lj1 = fac.getSPARQLLeftJoin(t6, ans2Function);
//		Function lj2 = fac.getSPARQLLeftJoin(t3, ans3Function);
//		Function jn = fac.getSPARQLJoin(t4, lj2);
//
//		//ans1(x, y)  :- LJ(T6(x,y,z), ans2(x,y,z))
//		//ans2(x,y,z) :- T5(x,y,z)
//		//ans2(x,y,z) :- T4(x,y,z), LJ(T3(x,y),ans3(x,z))
//		//ans3(x,z)   :- T1(x,z)
//		//ans3(x,z)   :- T2(x,z)
//
//		CQIE rule1 = fac.getCQIE(ans1Function, lj1);
//		CQIE rule2 = fac.getCQIE(ans2Function, t5);
//		CQIE rule3 = fac.getCQIE(ans2Function, jn);
//		CQIE rule4 = fac.getCQIE(ans3Function, t1);
//		CQIE rule5 = fac.getCQIE(ans3Function, t2);
//
//		program.appendRule(rule1);
//		program.appendRule(rule2);
//		program.appendRule(rule3);
//		program.appendRule(rule4);
//		program.appendRule(rule5);
//
//		String sql = gen.generateSourceQuery(program, signature);
//		System.out.println(sql);
//		System.err.println("Executing the Query");
//		viewTable(sql);
//		assertEquals(3, resultcount);
//	}
//
//
//	public  void viewTable( String query)
//		    throws SQLException {
//			resultcount=0;
//		    Statement stmt = null;
//		    try {
//		        stmt = (Statement) conn.createStatement();
//		        ResultSet rs = stmt.executeQuery(query);
//		        while (rs.next()) {
//		            String c1  = rs.getString("x");
//		            System.out.println("Result: "+c1+"\n");
//		            resultcount ++;
//		        }
//		    } catch (SQLException e ) {
//		        System.out.println(e);
//		    } finally {
//		        if (stmt != null) { stmt.close(); }
//		    }
//		}
//
//
//}
