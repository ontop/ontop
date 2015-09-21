package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.TurtleOBDASyntaxParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.UniqueConstraint;
import junit.framework.TestCase;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class Mapping2DatalogConverterTest extends TestCase {

	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private DBMetadata md = new DBMetadata("dummy class", null, null, DBMetadata.IdentityIdNormalizer);
	private SimplePrefixManager pm = new SimplePrefixManager();
	
	public void setUp() {
		// Database schema
		TableDefinition table1 = new TableDefinition("Student");
		table1.addAttribute("id", Types.INTEGER, null, false);
		table1.addAttribute("first_name", Types.VARCHAR, null, false);
		table1.addAttribute("last_name", Types.VARCHAR, null, false);
		table1.addAttribute("year", Types.INTEGER, null, false);
		table1.addAttribute("nationality", Types.VARCHAR, null, false);
		table1.setPrimaryKey(UniqueConstraint.of(table1.getAttribute("id")));
		
		TableDefinition table2 = new TableDefinition("Course");
		table2.addAttribute("cid", Types.VARCHAR, null, false);
		table2.addAttribute("title", Types.VARCHAR, null, false);
		table2.addAttribute("credits", Types.INTEGER, null, false);
		table2.addAttribute("description", Types.VARCHAR, null, false);
		table2.setPrimaryKey(UniqueConstraint.of(table2.getAttribute("cid")));
		
		TableDefinition table3 = new TableDefinition("Enrollment");
		table3.addAttribute("student_id", Types.INTEGER, null, false);
		table3.addAttribute("course_id", Types.VARCHAR, null, false);
		table3.setPrimaryKey(UniqueConstraint.of(table3.getAttribute("student_id"), table3.getAttribute("course_id")));
		
		md.add(table1);
		md.add(table2);
		md.add(table3);
		
		// Prefix manager
		pm.addPrefix(":", "http://www.example.org/university#");
	}
	
	private void runAnalysis(String source, String targetString) throws Exception {
		TurtleOBDASyntaxParser targetParser = new TurtleOBDASyntaxParser(pm);
		CQIE target = targetParser.parse(targetString);
		
		OBDAMappingAxiom mappingAxiom = ofac.getRDBMSMappingAxiom(source, target);
		ArrayList<OBDAMappingAxiom> mappingList = new ArrayList<OBDAMappingAxiom>();
		mappingList.add(mappingAxiom);
		
		List<CQIE> dp = Mapping2DatalogConverter.constructDatalogProgram(mappingList, md);
		
		assertNotNull(dp);
		System.out.println(dp.toString());
	}
	
	public void testAnalysis_1() throws Exception {
		runAnalysis(
				"select id from Student",
				":S_{id} a :Student .");
	}
	
	public void testAnalysis_2() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_3() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student where year=2010",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_4() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student where nationality='it'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_5() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student where year=2010 and nationality='it'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_6() throws Exception {
		runAnalysis(
				"select cid, title, credits, description from Course",
				":C_{cid} a :Course ; :title {title} ; :creditPoint {credits} ; :hasDescription {description} .");
	}
	
	public void testAnalysis_7() throws Exception {
		runAnalysis(
				"select cid, title from Course where credits>=4",
				":C_{cid} a :Course ; :title {title} .");
	}
	
	public void testAnalysis_8() throws Exception {
		runAnalysis(
				"select student_id, course_id from Enrollment",
				":S_{student_id} :hasCourse :C_{course_id}.");
	}
	
	public void testAnalysis_9() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student, Enrollment where Student.id=Enrollment.student_id and Enrollment.course_id='BA002'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_10() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student, Enrollment where Student.id=Enrollment.student_id and Enrollment.course_id='BA002' and Student.year=2010",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
	
	public void testAnalysis_11() throws Exception {
		runAnalysis(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}
	
	public void testAnalysis_12() throws Exception {
		runAnalysis(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student as TableStudent where TableStudent.year=2010",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}
	
	public void testAnalysis_13() throws Exception {
		runAnalysis(
				"select cid as CourseCode, title as CourseTitle, credits as CreditPoints, description as CourseDescription from Course",
				":C_{CourseCode} a :Course ; :title {CourseTitle} ; :creditPoint {CreditPoints} ; :hasDescription {CourseDescription} .");
	}
	
	public void testAnalysis_14() throws Exception {
		runAnalysis(
				"select cid as CourseCode, title as CourseTitle from Course as TableCourse where TableCourse.credits>=4",
				":C_{cid} a :Course ; :title {title} .");
	}
	
	public void testAnalysis_15() throws Exception {
		runAnalysis(
				"select student_id as sid, course_id as cid from Enrollment",
				":S_{sid} :hasCourse :C_{cid} .");
	}
	
	public void testAnalysis_16() throws Exception {
		runAnalysis(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student as t1, Enrollment as t2 where StudentNumber=student_id and t2.course_id='BA002'",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}

	public void testAnalysis_17() throws Exception {
		runAnalysis(
				"select id, first_name, last_name from Student where last_name like '%lli'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
//	public void testAnalysis_17() throws Exception {
//		runAnalysis(
	//RENAME STUDENT.ID TO STUDENT.STUDENT_ID SO THE COLUMN NAMES ARE THE SAME
//				"select Student.student_id as StudentId from Student JOIN Enrollment USING (student_id) ",
//				":S_{StudentId} a :Student .");
//	}
	
	public void testAnalysis_18() throws Exception {
		runAnalysis(
				"select id as StudentId from (select id from Student) JOIN Enrollment ON student_id = StudentId where year> 2010 ",
				":S_{StudentId} a :Student .");
	}
	
	public void testAnalysis_19() throws Exception {
		runAnalysis(
				"select id as StudentId from (select id from Student) JOIN Enrollment ON student_id = StudentId where first_name !~ 'foo' ",
				":S_{StudentId} a :Student .");
	}
	
	public void testAnalysis_20() throws Exception {
		runAnalysis(
				"select id as StudentId from (select id from Student) JOIN Enrollment ON student_id = StudentId where regexp_like(first_name,'foo') ",
				":S_{StudentId} a :Student .");
	}
	
	public void testAnalysis_21() throws Exception {
		runAnalysis(
				"select id as StudentId from (select id from Student) JOIN Enrollment ON student_id = StudentId where first_name regexp 'foo' ",
				":S_{StudentId} a :Student .");
	}



    public void testAnalysis_22() throws Exception{
        runAnalysis("select id, first_name, last_name from Student where year in (2000, 2014)",
                ":S_{id} a :RecentStudent ; :fname {first_name} ; :lname {last_name} .");
    }

    public void testAnalysis_23() throws Exception{
        runAnalysis("select id, first_name, last_name from Student where  (year between 2000 and 2014) and nationality='it'",
                ":S_{id} a :RecentStudent ; :fname {first_name} ; :lname {last_name} .");
    }

    public void testAnalysis_24() throws Exception {
        runAnalysis(
                "select id from (select id from Student) JOIN Enrollment ON student_id = id where regexp_like(first_name,'foo') ",
                ":S_{id} a :Student .");
    }

}
