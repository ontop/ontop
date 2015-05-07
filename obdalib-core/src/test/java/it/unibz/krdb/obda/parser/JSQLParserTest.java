package it.unibz.krdb.obda.parser;

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

import it.unibz.krdb.sql.api.ParsedSQLQuery;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSQLParserTest extends TestCase {
	final static Logger log = LoggerFactory.getLogger(JSQLParserTest.class);

	
	public void test_1_1_1() {
		final boolean result = parseJSQL("SELECT * FROM student");
		printJSQL("test_1_1_1", result);
		assertTrue(result);

	}

	
	public void test_1_1_2() {
		final boolean result = parseJSQL("SELECT student.* FROM student");
		printJSQL("test_1_1_2", result);
		assertTrue(result);

	}

	public void test_1_2_1() {
		final boolean result = parseJSQL("SELECT id FROM student");
		printJSQL("test_1_2_1", result);
		assertTrue(result);

	}

	public void test_1_2_2() {
		final boolean result = parseJSQL("SELECT id, name FROM student");
		printJSQL("test_1_2_2", result);
		assertTrue(result);

	}

	public void test_1_3_1() {
		final boolean result = parseJSQL("SELECT DISTINCT name FROM student");
		printJSQL("test_1_3_1", result);
		assertTrue(result);

	}

	public void test_1_3_2() {
		final boolean result = parseJSQL("SELECT ALL name FROM student");
		printJSQL("test_1_3_2", result);
		assertTrue(result);

	}

	public void test_1_3_3() {
		final boolean result = parseJSQL("select DISTINCT ON (name,age,year) name,age FROM student");
		printJSQL("test_1_3_1", result);
		assertTrue(result);

	}

	public void test_1_4() {
		final boolean result = parseJSQL("SELECT student.id FROM student");
		printJSQL("test_1_4", result);
		assertTrue(result);

	}

	public void test_1_5() {
		final boolean result = parseJSQL("SELECT student.id, student.name FROM student");
		printJSQL("test_1_5", result);
		assertTrue(result);

	}

	
	public void test_1_5_extra() {

		final boolean result = parseJSQL("SELECT \"URI\" as X, VALUE as Y, LANG as Z FROM QUEST_DATA_PROPERTY_LITERAL_ASSERTION WHERE ISBNODE = FALSE AND LANG IS NULL AND IDX = 1");
		printJSQL("test_1_5_extra", result);
		assertTrue(result);

	}

	
	public void test_1_5_extra_2() {
		final boolean result = parseJSQL("SELECT id, name as alias1, value as alias2 FROM table1");
		printJSQL("test_1_5_extra_2", result);
		assertTrue(result);

	}
	
	public void test_1_5_extra_3() {
		final boolean result = parseJSQL("select to_char(REGION_ID) as RID FROM HR.REGIONS");
		printJSQL("test_1_5_extra_3", result);
		assertTrue(result);

	}

	public void test_1_5_extra_4() {
		final boolean result = parseJSQL("SELECT \"URI1\" as X, \"URI2\" as Y FROM QUEST_OBJECT_PROPERTY_ASSERTION WHERE ISBNODE = FALSE AND ISBNODE2 = FALSE AND IDX = 2");
		printJSQL("test_1_5_extra_4", result);
		assertTrue(result);

	}

	// all of these are legal in SQL server; 'row' and 'rows' are not legal on
	// Oracle, though;
	public void test_1_5_extra_5() {
		final boolean result = parseJSQL("SELECT cast, do, extract, first, following, last, materialized, nulls, partition, range, row, rows, siblings, value, xml FROM tableName");
		printJSQL("test_1_5_extra_5", result);
		assertTrue(result);
	}
	
	public void test_1_6_1() {
		final boolean result = parseJSQL("SELECT undergraduate.* FROM student as undergraduate");
		printJSQL("test_1_6_1", result);
		assertTrue(result);

	}

	public void test_1_6_2() {
		final boolean result = parseJSQL("SELECT undergraduate.id FROM student as undergraduate");
		printJSQL("test_1_6_2", result);
		assertTrue(result);

	}

	public void test_1_7() {
		final boolean result = parseJSQL("SELECT alias.id, alias.name FROM student as alias");
		printJSQL("test_1_7", result);
		assertTrue(result);

	}

	public void test_1_7_1() {
		final boolean result = parseJSQL("SELECT alias.id, alias.name FROM student");
		printJSQL("test_1_7_1", result);
		assertTrue(result);

	}

	public void test_1_8() {
		final boolean result = parseJSQL("SELECT id FROM \"student\"");
		printJSQL("test_1_8", result);
		assertTrue(result);

	}

	public void test_1_9() {
		final boolean result = parseJSQL("SELECT id FROM \"public\".\"student\"");
		printJSQL("test_1_9", result);
		assertTrue(result);

	}

	public void test_1_10() {
		final boolean result = parseJSQL("SELECT t1.id, t2.name FROM \"public\".\"student\" as t1");
		printJSQL("test_1_10", result);
		assertTrue(result);

	}

	public void test_2_1() {
		final boolean result = parseJSQL("SELECT id FROM student WHERE id=1");
		printJSQL("test_2_1", result);
		assertTrue(result);

	}

	public void test_2_2() {
		final boolean result = parseJSQL("SELECT id, name FROM student WHERE id=1 AND name='John'");
		printJSQL("test_2_2", result);
		assertTrue(result);

	}

	public void test_2_3() {
		final boolean result = parseJSQL("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "nationality='IT' OR nationality='DE'");
		printJSQL("test_2_3", result);
		assertTrue(result);

	}

	public void test_2_4() {
		final boolean result = parseJSQL("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		printJSQL("test_2_4", result);
		assertTrue(result);

	}

	public void test_2_5() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is null");
		printJSQL("test_2_5", result);
		assertTrue(result);

	}

	public void test_2_6() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is not null");
		printJSQL("test_2_6", result);
		assertTrue(result);

	}

	public void test_2_7() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		printJSQL("test_2_7", result);
		assertTrue(result);

	}

	public void test_2_8() {
		final boolean result = parseJSQL("SELECT id, name FROM \"public\".\"student\" WHERE name<>'John'");
		printJSQL("test_2_8", result);
		assertTrue(result);
	}

	public void test_2_9() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.name<>'John'");
		printJSQL("test_2_9", result);
		assertTrue(result);

	}

	public void test_2_10() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.grade is not null AND t1.name<>'John'");
		printJSQL("test_2_10", result);
		assertTrue(result);

	}

	
	public void test_2_11() {
		final boolean result = parseJSQL("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		printJSQL("test_2_11", result);
		assertTrue(result);

	}

	
	public void test_2_12() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		printJSQL("test_2_12", result);
		assertTrue(result);

	}

	
	public void test_3_1() {
		final boolean result = parseJSQL("SELECT MAX(score) FROM grade");
		printJSQL("test_3_1", result);
		assertTrue(result);

	}

	
	public void test_3_2() {
		final boolean result = parseJSQL("SELECT MIN(score) FROM grade");
		printJSQL("test_3_2", result);
		assertTrue(result);

	}


	public void test_3_3() {
		final boolean result = parseJSQL("SELECT AVG(score) FROM grade");
		printJSQL("test_3_3", result);
		assertTrue(result);

	}


	public void test_3_4() {
		final boolean result = parseJSQL("SELECT SUM(amount) FROM tax");
		printJSQL("test_3_4", result);
		assertTrue(result);

	}

	
	public void test_3_5() {
		final boolean result = parseJSQL("SELECT COUNT(*) FROM student");
		printJSQL("test_3_5", result);
		assertTrue(result);

	}


	public void test_3_6() {
		final boolean result = parseJSQL("SELECT COUNT(id) FROM student");
		printJSQL("test_3_6", result);
		assertTrue(result);

	}

	
	public void test_3_7() {
		final boolean result = parseJSQL("SELECT EVERY(id) FROM student");
		printJSQL("test_3_7", result);
		assertTrue(result);

	}

	// NO SUPPORT BY JSQL Parser
	public void test_3_8() {
		final boolean result = parseJSQL("SELECT ANY(id) FROM student");
		printJSQL("test_3_8", result);
		assertFalse(result);

	}

	
	public void test_3_8_1() {
		// ANY AND SOME are the same
		final boolean result = parseJSQL("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)");
		printJSQL("test_3_8_1", result);
		assertTrue(result);

	}

	// NO SUPPORT BY JSQL
	public void test_3_9() {
		final boolean result = parseJSQL("SELECT SOME(id) FROM student");
		printJSQL("test_3_9", result);
		assertFalse(result);

	}


	public void test_3_9_1() {
		// ANY AND SOME are the same 
		final boolean result = parseJSQL("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = SOME (SELECT model FROM PC)");
		printJSQL("test_3_9_1", result);
		assertTrue(result);

	}


	public void test_4_1() {
		final boolean result = parseJSQL("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
		printJSQL("test_4_1", result);
		assertTrue(result);

	}

	
	public void test_4_2() {
		final boolean result = parseJSQL("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
		printJSQL("test_4_2", result);
		assertTrue(result);

	}

	public void test_4_3() {
		final boolean result = parseJSQL("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		printJSQL("test_4_3", result);
		assertTrue(result);

	}

	
	public void test_4_4() {
		final boolean result = parseJSQL("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("
				+ "SELECT ord_amount FROM orders WHERE ord_amount=2000)");
		printJSQL("test_4_4", result);
		assertTrue(result);

	}

	public void test_5_1() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_1", result);
		assertTrue(result);

	}

	public void test_5_1_1() {
		final boolean result = parseJSQL("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		printJSQL("test_5_1_1", result);
		assertTrue(result);

	}

	public void test_5_1_2() {
		final boolean result = parseJSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.\"score\">=25");
		printJSQL("test_5_1_2", result);
		assertTrue(result);

	}

	public void test_5_1_3() {
		final boolean result = parseJSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.pass=true");
		printJSQL("test_5_1_3", result);
		assertTrue(result);

	}

	public void test_5_2() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_2", result);
		assertTrue(result);
	}

	public void test_5_3() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_3", result);
		assertTrue(result);

	}

	public void test_5_4() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_4", result);
		assertTrue(result);

	}

	public void test_5_5() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_5", result);
		assertTrue(result);

	}

	public void test_5_6() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_6", result);
		assertTrue(result);

	}

	public void test_5_7() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_7", result);
		assertTrue(result);

	}

	public void test_5_8() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_8", result);
		assertTrue(result);

	}

	
	public void test_5_9() {
		final boolean result = parseJSQL("SELECT id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printJSQL("test_5_9", result);
		assertTrue(result);
	}

	
	public void test_5_10() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		printJSQL("test_5_10", result);
		assertTrue(result);

	}

	
	public void test_5_11() {
		final boolean result = parseJSQL("SELECT id, name, score FROM student JOIN grade USING (id)");
		printJSQL("test_5_11", result);
		assertTrue(result);

	}

	
	public void test_6_1() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id, grade FROM grade) t2 WHERE t1.id=t2.sid");
		printJSQL("test_6_1", result);
		assertTrue(result);

	}

	
	public void test_6_2() {
		final boolean result = parseJSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		printJSQL("test_6_2", result);
		assertTrue(result);

	}

	
	public void test_6_3() {
		final boolean result = parseJSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		printJSQL("test_6_3", result);
		assertTrue(result);

	}

	
	public void test_7_1() {
		final boolean result = parseJSQL("SELECT ('ID-' || student.id) as sid FROM student");
		printJSQL("test_7_1", result);
		assertTrue(result);

	}

	
	public void test_7_2() {
		final boolean result = parseJSQL("SELECT (grade.score * 30 / 100) as percentage from grade");
		printJSQL("test_7_2", result);
		assertTrue(result);

	}

	//don't work over union
	public void test_8_1() {
		final boolean result = parseJSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
		printJSQL("test_8_1", result);
		assertTrue(result);

	}

	//don't work over union
	public void test_8_2() {
		final boolean result = parseJSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
		printJSQL("test_8_2", result);
		assertTrue(result);

	}

	//don't work over union
	public void test_8_3() {
		final boolean result = parseJSQL("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
		printJSQL("test_8_3", result);
		assertTrue(result);

	}

	
	public void test_8_4() {
		final boolean result = parseJSQL("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
		printJSQL("test_8_4", result);
		assertTrue(result);

	}

	
	public void test_8_5() {
		final boolean result = parseJSQL("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printJSQL("test_8_5", result);
		assertTrue(result);

	}

	public void test_9_1() {
		final boolean result = parseJSQL("SELECT id, name, address from student where name = 'John'");
		printJSQL("test_9_1", result);
		assertTrue(result);

	}

	public void test_9_2() {
		final boolean result = parseJSQL("SELECT id, name, address from student where id = 20");
		printJSQL("test_9_2", result);
		assertTrue(result);

	}

	public void test_9_3() {
		final boolean result = parseJSQL("SELECT payee, amount from tax where amount = 12.345");
		printJSQL("test_9_3", result);
		assertTrue(result);

	}

	public void test_9_4_1() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		printJSQL("test_9_4_1", result);
		assertTrue(result);

	}

	public void test_9_4_2() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		printJSQL("test_9_4_2", result);
		assertTrue(result);

	}

	public void test_9_4_3() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22'");
		printJSQL("test_9_4_3", result);
		assertTrue(result);

	}

	public void test_9_5() {
		final boolean result = parseJSQL("SELECT st_id, course, score from student where passed = TRUE");
		printJSQL("test_9_5", result);
		assertTrue(result);

	}

	public void test_10_1() {
		final boolean result = parseJSQL("SELECT name from grade, student where passed = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		printJSQL("test_10_1", result);
		assertTrue(result);

	}

	public void test_10_2() {
		final boolean result = parseJSQL("SELECT name from grade, student where passed = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		printJSQL("test_10_2", result);
		assertTrue(result);

	}

	public void test_11() {
		final boolean result = parseJSQL("SELECT \"Name\" from grade, student where passed = FALSE AND ( \"course\" = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		printJSQL("test_11", result);
		assertTrue(result);

	}

	public void test_11_1() {
		final boolean result = parseJSQL("select t1.owner NAME from all_tables t1, all_tables t2, ALL_VIEWS where t1.table_name = t2.table_name and t1.owner = t2.owner and t1.owner = ALL_VIEWS.OWNER");
		printJSQL("test_11_1", result);
		assertTrue(result);

	}

	public void test_12() {
		final boolean result = parseJSQL("select name from grade, student where score BETWEEN 6 AND 8");
		printJSQL("test_12", result);
		assertTrue(result);

	}
	
	public void test_13() {
		final boolean result = parseJSQL("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
		printJSQL("test_13", result);
		assertTrue(result);

	}
	
	//add support for CAST also in unquoted visited query
	public void testUnquoted1(){
		final boolean result = parseUnquotedJSQL("SELECT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
		printJSQL("test_Unquoted1", result);
		assertTrue(result);
	}

	// Does not parse SELECT DISTINCT (on purpose)
	public void testUnquoted2(){
		final boolean result = parseUnquotedJSQL("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
		printJSQL("test_Unquoted1", result);
		assertFalse(result);
	}

	public void testCast1(){
		final boolean result = parseUnquotedJSQL("SELECT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL");
		printJSQL("testCast", result);
		assertTrue(result);
	}

	// Does not parse SELECT DISTINCT (on purpose)
	public void testCast2(){
		final boolean result = parseUnquotedJSQL("SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL");
		printJSQL("testCast", result);
		assertFalse(result);
	}

	/* Regex in MySQL, Oracle and Postgres*/

	public void testRegexMySQL(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE name REGEXP '^b'");
		printJSQL("testRegexMySQL", result);
		assertTrue(result);
	}
	
	public void testRegexBinaryMySQL(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE name REGEXP BINARY '^b'");
		printJSQL("testRegexBinaryMySQL", result);
		assertTrue(result);
	}
	
	public void testRegexPostgres(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE name ~ 'foo'");
		printJSQL("testRegexPostgres", result);
		assertTrue(result);
	}
	
	//no support for similar to in postgres
	public void testRegexPostgresSimilarTo(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE 'abc' SIMILAR TO 'abc'");
		printJSQL("testRegexPostgresSimilarTo", result);
		assertFalse(result);
	}
	
	public void testRegexOracle(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE REGEXP_LIKE(testcol, '[[:alpha:]]')");
		printJSQL("testRegexMySQL", result);
		assertTrue(result);
	}
	
	//no support for not without parenthesis
	public void testRegexNotOracle(){
		final boolean result = parseUnquotedJSQL("SELECT * FROM pet WHERE NOT REGEXP_LIKE(testcol, '[[:alpha:]]')");
		printJSQL("testRegexNotMySQL", result);
		assertFalse(result);
	}

    public void test_md5() {
        final boolean result = parseJSQL("SELECT MD5(CONCAT(COALESCE(Address, RAND()), COALESCE(City, RAND()),\n" +
                "COALESCE(Region, RAND()), COALESCE(PostalCode, RAND()), COALESCE(Country,\n" +
                "RAND()) )) AS locationID FROM northwind.Suppliers");
        printJSQL("test_13", result);
        assertTrue(result);
    }

    public void test_concatOracle() {
        final boolean result = parseJSQL("SELECT ('ID-' || student.id || 'type1') \"sid\" FROM student");
        printJSQL("test_concatOracle()", result);
        assertTrue(result);
    }

    public void test_RegexpReplace() {
        final boolean result = parseJSQL("SELECT REGEXP_REPLACE('Hello World', ' +', ' ') as reg FROM student");
        printJSQL("test_RegexpReplace()", result);
        assertTrue(result);
    }

	private String queryText;

	ParsedSQLQuery queryP;

	private boolean parseJSQL(String input) {

		queryText = input;

		try {
			queryP = new ParsedSQLQuery(input,false);
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void printJSQL(String title, boolean isSupported) {
		if (isSupported) {
			System.out.println(title + ": " + queryP.toString());
			
			try {
				System.out.println("  Tables: " + queryP.getTables());
				System.out.println("  Projection: " + queryP.getProjection());

				System.out.println("  Selection: "
						+ ((queryP.getWhereClause() == null) ? "--" : queryP
								.getWhereClause()));

				System.out.println("  Aliases: "
						+ (queryP.getAliasMap().isEmpty() ? "--" : queryP
								.getAliasMap()));
				System.out.println("  GroupBy: " + queryP.getGroupByClause());
				System.out.println("  SubSelect: "
						+ (queryP.getSubSelects().isEmpty() ? "--" : queryP
								.getSubSelects()));
				System.out.println("  Join conditions: "
						+ (queryP.getJoinConditions().isEmpty() ? "--" : queryP
								.getJoinConditions()));
				System.out.println("  Columns: "
						+ (queryP.getColumns().isEmpty() ? "--" : queryP
								.getColumns()));
			} catch (Exception e) {

				e.printStackTrace();
			}
		} else {
			System.out.println("Parser JSQL doesn't support for query: "
					+ queryText);
		}
		System.out.println();
	}

	
	private boolean parseUnquotedJSQL(String input) {

		queryText = input;

		try {
			queryP = new ParsedSQLQuery(input,true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
