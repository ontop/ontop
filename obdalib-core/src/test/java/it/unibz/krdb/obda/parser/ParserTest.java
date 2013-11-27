/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.ParsedQuery;
import it.unibz.krdb.sql.api.QueryTree;
import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTest extends TestCase {
	final static Logger log = LoggerFactory.getLogger(ParserTest.class);

	// NO SUPPORT SQL
	public void test_1_1_1() {
		final boolean result = parseJSQL("SELECT * FROM student");
		printJSQL("test_1_1_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT * FROM student");
		printSQL("test_1_1_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_1_1_2() {
		final boolean result = parseJSQL("SELECT student.* FROM student");
		printJSQL("test_1_1_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT student.* FROM student");
		printJSQL("test_1_1_2", result2);
		assertFalse(result2);
	}

	public void test_1_2_1() {
		final boolean result = parseJSQL("SELECT id FROM student");
		printJSQL("test_1_2_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id FROM student");
		printSQL("test_1_2_1", result2);
		assertTrue(result2);
	}

	public void test_1_2_2() {
		final boolean result = parseJSQL("SELECT id, name FROM student");
		printJSQL("test_1_2_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name FROM student");
		printSQL("test_1_2_2", result2);
		assertTrue(result2);
	}

	public void test_1_3_1() {
		final boolean result = parseJSQL("SELECT DISTINCT name FROM student");
		printJSQL("test_1_3_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT DISTINCT name FROM student");
		printSQL("test_1_3_2", result2);
		assertTrue(result2);
	}

	public void test_1_3_2() {
		final boolean result = parseJSQL("SELECT ALL name FROM student");
		printJSQL("test_1_3_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT ALL name FROM student");
		printSQL("test_1_3_2", result2);
		assertTrue(result2);
	}
	
	public void test_1_3_3() {
		final boolean result = parseJSQL("select DISTINCT ON (name,age,year) name,age FROM student");
		printJSQL("test_1_3_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("select DISTINCT ON (name,age,year) name,age FROM student");
		printSQL("test_1_3_2", result2);
		assertFalse(result2);
	}

	public void test_1_4() {
		final boolean result = parseJSQL("SELECT student.id FROM student");
		printJSQL("test_1_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT student.id FROM student");
		printSQL("test_1_4", result2);
		assertTrue(result2);
	}

	public void test_1_5() {
		final boolean result = parseJSQL("SELECT student.id, student.name FROM student");
		printJSQL("test_1_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT student.id, student.name FROM student");
		printSQL("test_1_5", result2);
		assertTrue(result2);
	}

	// NO SUPPORT SQL
	public void test_1_6_1() {
		final boolean result = parseJSQL("SELECT undergraduate.* FROM student as undergraduate");
		printJSQL("test_1_6_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT undergraduate.* FROM student as undergraduate");
		printSQL("test_1_6_1", result2);
		assertFalse(result2);
	}

	public void test_1_6_2() {
		final boolean result = parseJSQL("SELECT undergraduate.id FROM student as undergraduate");
		printJSQL("test_1_6_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT undergraduate.id FROM student as undergraduate");
		printSQL("test_1_6_2", result2);
		assertTrue(result2);
	}

	public void test_1_7() {
		final boolean result = parseJSQL("SELECT alias.id, alias.name FROM student as alias");
		printJSQL("test_1_7", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT alias.id, alias.name FROM student as alias");
		printSQL("test_1_7", result2);
		assertTrue(result2);
	}

	public void test_1_8() {
		final boolean result = parseJSQL("SELECT id FROM \"student\"");
		printJSQL("test_1_8", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id FROM \"student\"");
		printSQL("test_1_8", result2);
		assertTrue(result2);
	}

	public void test_1_9() {
		final boolean result = parseJSQL("SELECT id FROM \"public\".\"student\"");
		printJSQL("test_1_9", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id FROM \"public\".\"student\"");
		printSQL("test_1_9", result2);
		assertTrue(result2);
	}

	public void test_1_10() {
		final boolean result = parseJSQL("SELECT t1.id, t2.name FROM \"public\".\"student\" as t1");
		printJSQL("test_1_10", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t2.name FROM \"public\".\"student\" as t1");
		printSQL("test_1_10", result2);
		assertTrue(result2);
	}

	public void test_2_1() {
		final boolean result = parseJSQL("SELECT id FROM student WHERE id=1");
		printJSQL("test_2_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id FROM student WHERE id=1");
		printSQL("test_2_1", result2);
		assertTrue(result2);
	}

	public void test_2_2() {
		final boolean result = parseJSQL("SELECT id, name FROM student WHERE id=1 AND name='John'");
		printJSQL("test_2_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name FROM student WHERE id=1 AND name='John'");
		printSQL("test_2_2", result2);
		assertTrue(result2);
	}

	public void test_2_3() {
		final boolean result = parseJSQL("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "nationality='IT' OR nationality='DE'");
		printJSQL("test_2_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "nationality='IT' OR nationality='DE'");
		printSQL("test_2_3", result2);
		assertTrue(result2);
	}

	public void test_2_4() {
		final boolean result = parseJSQL("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		printJSQL("test_2_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		printSQL("test_2_4", result2);
		assertTrue(result2);
	}

	public void test_2_5() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is null");
		printJSQL("test_2_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, grade FROM student WHERE grade is null");
		printSQL("test_2_5", result2);
		assertTrue(result2);
	}

	public void test_2_6() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is not null");
		printJSQL("test_2_6", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, grade FROM student WHERE grade is not null");
		printSQL("test_2_6", result2);
		assertTrue(result2);
	}

	public void test_2_7() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		printJSQL("test_2_7", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		printSQL("test_2_7", result2);
		assertTrue(result2);
		
	}

	public void test_2_8() {
		final boolean result = parseJSQL("SELECT id, name FROM \"public\".\"student\" WHERE name<>'John'");
		printJSQL("test_2_8", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name FROM \"public\".\"student\" WHERE name<>'John'");
		printSQL("test_2_8", result2);
		assertTrue(result2);
	}

	public void test_2_9() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.name<>'John'");
		printJSQL("test_2_9", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.name<>'John'");
		printSQL("test_2_9", result2);
		assertTrue(result2);
	}

	public void test_2_10() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.grede is not null AND t1.name<>'John'");
		printJSQL("test_2_10", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.grede is not null AND t1.name<>'John'");
		printSQL("test_2_10", result2);
		assertTrue(result2);
	}

	// NO SUPPORT SQL
	public void test_2_11() {
		final boolean result = parseJSQL("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		printJSQL("test_2_11", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		printSQL("test_2_11", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_2_12() {
		final boolean result = parseJSQL("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		printJSQL("test_2_12", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		printSQL("test_2_12", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_1() {
		final boolean result = parseJSQL("SELECT MAX(score) FROM grade");
		printJSQL("test_3_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT MAX(score) FROM grade");
		printSQL("test_3_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_2() {
		final boolean result = parseJSQL("SELECT MIN(score) FROM grade");
		printJSQL("test_3_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT MIN(score) FROM grade");
		printSQL("test_3_2", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_3() {
		final boolean result = parseJSQL("SELECT AVG(score) FROM grade");
		printJSQL("test_3_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT AVG(score) FROM grade");
		printSQL("test_3_3", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_4() {
		final boolean result = parseJSQL("SELECT SUM(amount) FROM tax");
		printJSQL("test_3_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT SUM(amount) FROM tax");
		printSQL("test_3_4", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_5() {
		final boolean result = parseJSQL("SELECT COUNT(*) FROM student");
		printJSQL("test_3_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT COUNT(*) FROM student");
		printSQL("test_3_5", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL 
	public void test_3_6() {
		final boolean result = parseJSQL("SELECT COUNT(id) FROM student");
		printJSQL("test_3_6", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT COUNT(id) FROM student");
		printSQL("test_3_6", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_3_7() {
		final boolean result = parseJSQL("SELECT EVERY(id) FROM student");
		printJSQL("test_3_7", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT EVERY(id) FROM student");
		printSQL("test_3_7", result2);
		assertFalse(result2);
	}

	// NO SUPPORT BY BOTH
	public void test_3_8() {
		final boolean result = parseJSQL("SELECT ANY(id) FROM student");
		printJSQL("test_3_8", result);
		assertFalse(result);
		final boolean result2 = parseSQL("SELECT ANY(id) FROM student");
		printSQL("test_3_8", result2);
		assertFalse(result2);
	}
	
	// NO SUPPORT SQL
	public void test_3_8_1(){ 
		//ANY AND SOME are the same
		final boolean result = parseJSQL("SELECT DISTINCT maker FROM Product "
				+"WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)");
		printJSQL("test_3_8_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT DISTINCT maker FROM Product "
				+"WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)");
		printSQL("test_3_8_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT BY BOTH
	public void test_3_9() {
		final boolean result = parseJSQL("SELECT SOME(id) FROM student");
		printJSQL("test_3_9", result);
		assertFalse(result);
		final boolean result2 = parseSQL("SELECT SOME(id) FROM student");
		printSQL("test_3_9", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_4_1() {
		final boolean result = parseJSQL("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
		printJSQL("test_4_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
		printSQL("test_4_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_4_2() {
		final boolean result = parseJSQL("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
		printJSQL("test_4_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
		printSQL("test_4_2", result2);
		assertFalse(result2);
		
	}

	public void test_4_3() {
		final boolean result = parseJSQL("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		printJSQL("test_4_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		printSQL("test_4_3", result2);
		assertTrue(result2);
	}

	// NO SUPPORT SQL
	public void test_4_4() {
		final boolean result = parseJSQL("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("  
				+"SELECT ord_amount FROM orders WHERE ord_amount=2000)");
		printJSQL("test_4_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("  
				+"SELECT ord_amount FROM orders WHERE ord_amount=2000)");
		printSQL("test_4_4", result2);
		assertFalse(result2);
	}
	
	public void test_5_1() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_1", result2);
		assertTrue(result2);
	}

	public void test_5_1_1() {
		final boolean result = parseJSQL("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		printJSQL("test_5_1_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		printSQL("test_5_1_1", result2);
		assertTrue(result2);
	}

	public void test_5_1_2() {
		final boolean result = parseJSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.score>=25");
		printJSQL("test_5_1_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.score>=25");
		printSQL("test_5_1_2", result2);
		assertTrue(result2);
	}

	public void test_5_1_3() {
		final boolean result = parseJSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.pass=true");
		printJSQL("test_5_1_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.pass=true");
		printSQL("test_5_1_3", result2);
		assertTrue(result2);
	}

	public void test_5_2() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_2", result2);
		assertTrue(result2);
	}

	public void test_5_3() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_3", result2);
		assertTrue(result2);
	}

	public void test_5_4() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_4", result2);
		assertTrue(result2);
	}

	public void test_5_5() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_5", result2);
		assertTrue(result2);
	}

	public void test_5_6() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_6", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_6", result2);
		assertTrue(result2);
	}

	public void test_5_7() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_7", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_7", result2);
		assertTrue(result2);
	}

	public void test_5_8() {
		final boolean result = parseJSQL("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printJSQL("test_5_8", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
		printSQL("test_5_8", result2);
		assertTrue(result2);
	}

	// NO SUPPORT SQL
	public void test_5_9() {
		final boolean result = parseJSQL("SELECT id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printJSQL("test_5_9", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printSQL("test_5_9", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_5_10() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		printJSQL("test_5_10", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		printSQL("test_5_10", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_5_11() {
		final boolean result = parseJSQL("SELECT id, name, score FROM student JOIN grade USING (id)");
		printJSQL("test_5_11", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, score FROM student JOIN grade USING (id)");
		printSQL("test_5_11", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_6_1() {
		final boolean result = parseJSQL("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id, grade FROM grade) t2 WHERE t1.id=t2.sid");
		printJSQL("test_6_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id, grade FROM grade) t2 WHERE t1.id=t2.sid");
		printSQL("test_6_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_6_2() {
		final boolean result = parseJSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		printJSQL("test_6_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		printSQL("test_6_2", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_6_3() {
		final boolean result = parseJSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		printJSQL("test_6_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		printSQL("test_6_3", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_7_1() {
		final boolean result = parseJSQL("SELECT ('ID-' || student.id) as sid FROM student");
		printJSQL("test_7_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT ('ID-' || student.id) as sid FROM student");
		printSQL("test_7_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_7_2() {
		final boolean result = parseJSQL("SELECT (grade.score * 30 / 100) as percentage from grade");
		printJSQL("test_7_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT (grade.score * 30 / 100) as percentage from grade");
		printSQL("test_7_2", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_8_1() {
		final boolean result = parseJSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
		printJSQL("test_8_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
		printSQL("test_8_1", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_8_2() {
		final boolean result = parseJSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
		printJSQL("test_8_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
		printSQL("test_8_2", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_8_3() {
		final boolean result = parseJSQL("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
		printJSQL("test_8_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
		printSQL("test_8_3", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_8_4() {
		final boolean result = parseJSQL("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
		printJSQL("test_8_4", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
		printSQL("test_8_4", result2);
		assertFalse(result2);
	}

	// NO SUPPORT SQL
	public void test_8_5() {
		final boolean result = parseJSQL("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printJSQL("test_8_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		printSQL("test_8_5", result2);
		assertFalse(result2);
	}

	public void test_9_1() {
		final boolean result = parseJSQL("SELECT id, name, address from student where name = 'John'");
		printJSQL("test_9_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, address from student where name = 'John'");
		printSQL("test_9_1", result2);
		assertTrue(result2);
	}

	public void test_9_2() {
		final boolean result = parseJSQL("SELECT id, name, address from student where id = 20");
		printJSQL("test_9_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, address from student where id = 20");
		printSQL("test_9_2", result2);
		assertTrue(result2);
	}

	public void test_9_3() {
		final boolean result = parseJSQL("SELECT payee, amount from tax where amount = 12.345");
		printJSQL("test_9_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT payee, amount from tax where amount = 12.345");
		printSQL("test_9_3", result2);
		assertTrue(result2);
	}

	public void test_9_4_1() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		printJSQL("test_9_4_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		printSQL("test_9_4_1", result2);
		assertTrue(result2);
	}

	public void test_9_4_2() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		printJSQL("test_9_4_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		printSQL("test_9_4_2", result2);
		assertTrue(result2);
	}

	public void test_9_4_3() {
		final boolean result = parseJSQL("SELECT id, name, address from student where birth_date = '1984-01-22'");
		printJSQL("test_9_4_3", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT id, name, address from student where birth_date = '1984-01-22'");
		printSQL("test_9_4_3", result2);
		assertTrue(result2);
	}

	public void test_9_5() {
		final boolean result = parseJSQL("SELECT st_id, course, score from student where passed = TRUE");
		printJSQL("test_9_5", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT st_id, course, score from student where passed = TRUE");
		printSQL("test_9_5", result2);
		assertTrue(result2);
	}
	
	public void test_10_1() {
		final boolean result = parseJSQL("SELECT name from grade, student where passed = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		printJSQL("test_10_1", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name from grade, student where passed = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		printSQL("test_10_1", result2);
		assertTrue(result2);
	}

	public void test_10_2() {
		final boolean result = parseJSQL("SELECT name from grade, student where passed = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		printJSQL("test_10_2", result);
		assertTrue(result);
		final boolean result2 = parseSQL("SELECT name from grade, student where passed = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		printSQL("test_10_2", result2);
		assertTrue(result2);
	}
	
	
	
	private String queryText;

	ParsedQuery queryP;
	
	private boolean parseJSQL(String input) {

		queryText = input;

		
		try {
			 queryP = new ParsedQuery(input);
		} catch (JSQLParserException e) {
			
			return false;
		}

		
		return true;
	}
	
	private void printJSQL(String title, boolean isSupported) {
		if (isSupported) {
			System.out.println(title + ": " + queryText);
			System.out.println("  Tables: " + queryP.getTableSet());
			System.out.println("  Projection: " + queryP.getProjection());
			System.out.println("  Selection: " + ((queryP.getSelection().isEmpty()) ? "--" : queryP.getSelection()));
			System.out.println("  Aliases: " + (queryP.getAliasMap().isEmpty() ? "--" : queryP.getAliasMap()));
			System.out.println("  GroupBy: " +  queryP.getGroupByClause());
			System.out.println("  Join conditions: " + (queryP.getJoinCondition().isEmpty() ? "--" : queryP.getJoinCondition()));
		} else {
			System.out.println("Parser JSQL doesn't support for query: " + queryText);
		}
		System.out.println();
	}
	
	private SQL99Parser parser;
	private QueryTree queryTree;
	
	private boolean parseSQL(String input) {

		queryText = input;
		


		ANTLRStringStream inputStream = new ANTLRStringStream(input);
		SQL99Lexer lexer = new SQL99Lexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		parser = new SQL99Parser(tokenStream);

		try {
			queryTree = parser.parse();
		} catch (RecognitionException e) {
			return false;
		}

		if (parser.getNumberOfSyntaxErrors() != 0) {
			return false;
		}
		
		return true;
	}
	
	private void printSQL(String title, boolean isSupported) {
		if (isSupported) {
			System.out.println(title + ": " + queryText);
			System.out.println("  Tables: " + queryTree.getTableSet());
			System.out.println("  Projection: " + queryTree.getProjection());
			System.out.println("  Selection: " + ((queryTree.getSelection() == null) ? "--" : queryTree.getSelection()));
			System.out.println("  Aliases: " + (queryTree.getAliasMap().isEmpty() ? "--" : queryTree.getAliasMap()));
			System.out.println("  Join conditions: " + (queryTree.getJoinCondition().isEmpty() ? "--" : queryTree.getJoinCondition()));
		} else {
			System.out.println("Parser SQL doesn't support for query: " + queryText);
		}
		System.out.println();
	}
}
