package it.unibz.inf.ontop.spec.pp;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertFalse;

public class SQLPPMappingConverterTest {

	private MetadataLookup getMetadataLookup() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
		DBTermType stringDBType = builder.getDBTypeFactory().getDBStringType();

		// Database schema
		NamedRelationDefinition table1 = builder.createDatabaseRelation("Student",
			"id", integerDBType, false,
			"first_name", stringDBType, false,
			"last_name", stringDBType, false,
			"year", integerDBType, false,
			"nationality", stringDBType, false);
		UniqueConstraint.primaryKeyOf(table1.getAttribute(1));

		NamedRelationDefinition table2 = builder.createDatabaseRelation( "Course",
			"cid", stringDBType, false,
			"title", stringDBType, false,
			"credits", integerDBType, false,
			"description", stringDBType, false);
		UniqueConstraint.primaryKeyOf(table2.getAttribute(1));

		NamedRelationDefinition table3 = builder.createDatabaseRelation("Enrollment",
			"student_id", integerDBType, false,
			"course_id", stringDBType, false);
		UniqueConstraint.primaryKeyOf(table3.getAttribute(1),
				table3.getAttribute(2));

		return builder.build();
	}

	private ImmutableList<IQ> getIQs(String source, String targetString) throws Exception {

		TargetQueryParser targetParser = TARGET_QUERY_PARSER_FACTORY.createParser(MAPPING_FACTORY.createPrefixManager(
				ImmutableMap.of(":", "http://www.example.org/university#")));

		SQLPPTriplesMap mapping = new OntopNativeSQLPPTriplesMap("MAPID-0",
				SOURCE_QUERY_FACTORY.createSourceQuery(source), targetParser.parse(targetString));

		ImmutableList<IQ> dp = SQLPP_MAPPING_CONVERTER.convert(ImmutableList.of(mapping), getMetadataLookup()).stream()
				.map(MappingAssertion::getQuery)
				.collect(ImmutableCollectors.toList());
		
		assertFalse(dp.isEmpty());
		return dp;
	}

	@Test
	public void testAnalysis_1() throws Exception {
		getIQs(
				"select id from Student",
				":S_{id} a :Student .");
	}

	@Test
	public void testAnalysis_2() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_3() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student where year=2010",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_4() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student where nationality='it'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_5() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student where year=2010 and nationality='it'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_6() throws Exception {
		getIQs(
				"select cid, title, credits, description from Course",
				":C_{cid} a :Course ; :title {title} ; :creditPoint {credits} ; :hasDescription {description} .");
	}

	@Test
	public void testAnalysis_7() throws Exception {
		getIQs(
				"select cid, title from Course where credits>=4",
				":C_{cid} a :Course ; :title {title} .");
	}

	@Test
	public void testAnalysis_8() throws Exception {
		getIQs(
				"select student_id, course_id from Enrollment",
				":S_{student_id} :hasCourse :C_{course_id}.");
	}

	@Test
	public void testAnalysis_9() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student, Enrollment where Student.id=Enrollment.student_id and Enrollment.course_id='BA002'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_10() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student, Enrollment where Student.id=Enrollment.student_id and Enrollment.course_id='BA002' and Student.year=2010",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}

	@Test
	public void testAnalysis_11() throws Exception {
		getIQs(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}

	@Test
	public void testAnalysis_12() throws Exception {
		getIQs(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student as TableStudent where TableStudent.year=2010",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}

	@Test
	public void testAnalysis_13() throws Exception {
		getIQs(
				"select cid as CourseCode, title as CourseTitle, credits as CreditPoints, description as CourseDescription from Course",
				":C_{CourseCode} a :Course ; :title {CourseTitle} ; :creditPoint {CreditPoints} ; :hasDescription {CourseDescription} .");
	}

	@Test
	public void testAnalysis_14() throws Exception {
		getIQs(
				"select cid as CourseCode, title as CourseTitle from Course as TableCourse where TableCourse.credits>=4",
				":C_{CourseCode} a :Course ; :title {CourseTitle} .");
	}

	@Test
	public void testAnalysis_15() throws Exception {
		getIQs(
				"select student_id as sid, course_id as cid from Enrollment",
				":S_{sid} :hasCourse :C_{cid} .");
	}

	// ROMAN (28.11.17): StudentNumber cannot be used in the WHERE clause
	@Test
	public void testAnalysis_16() throws Exception {
		getIQs(
				"select id as StudentNumber, first_name as Name, last_name as FamilyName from Student as t1, Enrollment as t2 where id=student_id and t2.course_id='BA002'",
				":S_{StudentNumber} a :Student ; :fname {Name} ; :lname {FamilyName} .");
	}

	@Test
	public void testAnalysis_17() throws Exception {
		getIQs(
				"select id, first_name, last_name from Student where last_name like '%lli'",
				":S_{id} a :Student ; :fname {first_name} ; :lname {last_name} .");
	}
//	public void testAnalysis_17() throws Exception {
//		runAnalysis(
	//RENAME STUDENT.ID TO STUDENT.STUDENT_ID SO THE COLUMN NAMES ARE THE SAME
//				"select Student.student_id as StudentId from Student JOIN Enrollment USING (student_id) ",
//				":S_{StudentId} a :Student .");
//	}

	// ROMAN (28.11.17): StudentId cannot be used in the WHERE clause
	// ROMAN (28.11.17): year was not visible outside the subsquery
	@Test
	public void testAnalysis_18() throws Exception {
		getIQs(
				"select id as StudentId from (select id, year from Student) as Sub JOIN Enrollment ON student_id = id where year> 2010 ",
				":S_{StudentId} a :Student .");
	}

	// ROMAN (28.11.17): StudentId cannot be used in the WHERE clause
	// ROMAN (28.11.17): first_name was not visible outside the subsquery
	@Test
	public void testAnalysis_19() throws Exception {
		getIQs(
				"select id as StudentId from (select id, first_name from Student) as Sub JOIN Enrollment ON student_id = id where first_name !~ 'foo' ",
				":S_{StudentId} a :Student .");
	}

	// ROMAN (28.11.17): StudentId cannot be used in the WHERE clause
	// ROMAN (28.11.17): first_name was not visible outside the subsquery
	@Test
	public void testAnalysis_20() throws Exception {
		getIQs(
				"select id as StudentId from (select id, first_name from Student) as Sub JOIN Enrollment ON student_id = id where regexp_like(first_name,'foo') ",
				":S_{StudentId} a :Student .");
	}

	// ROMAN (28.11.17): StudentId cannot be used in the WHERE clause
	// ROMAN (28.11.17): first_name was not visible outside the subsquery
	@Test
	public void testAnalysis_21() throws Exception {
		getIQs(
				"select id as StudentId from (select id, first_name from Student) as Sub JOIN Enrollment ON student_id = id where first_name regexp 'foo' ",
				":S_{StudentId} a :Student .");
	}

	@Test
    public void testAnalysis_22() throws Exception{
        getIQs("select id, first_name, last_name from Student where year in (2000, 2014)",
                ":S_{id} a :RecentStudent ; :fname {first_name} ; :lname {last_name} .");
    }

	@Test
    public void testAnalysis_23() throws Exception{
        getIQs("select id, first_name, last_name from Student where  (year between 2000 and 2014) and nationality='it'",
                ":S_{id} a :RecentStudent ; :fname {first_name} ; :lname {last_name} .");
    }

	// ROMAN (28.11.17): first_name was not visible outside the subsquery
	@Test
    public void testAnalysis_24() throws Exception {
        getIQs(
                "select id from (select id, first_name from Student) as Sub JOIN Enrollment ON student_id = id where regexp_like(first_name,'foo') ",
                ":S_{id} a :Student .");
    }

	@Test
	public void testAnalysis_25() throws Exception {
		getIQs(
				"select \"QINVESTIGACIONPUARTTMP0\".id \"t1_1\" from Student \"QINVESTIGACIONPUARTTMP0\"  where \"QINVESTIGACIONPUARTTMP0\".first_name IS NOT NULL ",
				":S_{t1_1} a :Student .");
	}

	// Black-box view extraction is not supported for ImmutableMetadataLookup
	@Test(expected = UnsupportedOperationException.class)
	public void testAnalysis_26() throws Exception {
		getIQs(
				"SELECT * FROM Student  WHERE (id,  year) IN (SELECT i_id, MAX(year) FROM Student GROUP BY id)",
				":S_{id} a :Student ; :year \"{year}\" .");
	}

	// Black-box view extraction is not supported for ImmutableMetadataLookup
	@Test(expected = UnsupportedOperationException.class)
	public void testAnalysis_27() throws Exception {
		getIQs(
				"SELECT id FROM Student  WHERE (id,  year) IN (SELECT i_id, MAX(year) FROM Student GROUP BY id) INTERSECT SELECT student_id FROM Enrollment",
				":S_{id} a :Student .");
	}

	@Test
    public void testAnalysis_28() throws Exception {
        getIQs(
                "select lower(id) as lid from Student",
                ":S_{lid} a :Student .");
    }
}
