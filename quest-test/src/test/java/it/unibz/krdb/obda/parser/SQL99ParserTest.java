package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.QueryTree;

import java.sql.Types;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQL99ParserTest extends TestCase {
	final static Logger log = LoggerFactory.getLogger(SQL99ParserTest.class);

	// NO SUPPORT
	public void test_1_1_1() {
		final boolean result = parse("SELECT * FROM student");
		print("test_1_1_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_1_1_2() {
		final boolean result = parse("SELECT student.* FROM student");
		print("test_1_1_2", result);
		assertFalse(result);
	}

	public void test_1_2_1() {
		final boolean result = parse("SELECT id FROM student");
		print("test_1_2_1", result);
		assertTrue(result);
	}

	public void test_1_2_2() {
		final boolean result = parse("SELECT id, name FROM student");
		print("test_1_2_2", result);
		assertTrue(result);
	}

	public void test_1_3_1() {
		final boolean result = parse("SELECT DISTINCT name FROM student");
		print("test_1_3_1", result);
		assertTrue(result);
	}

	public void test_1_3_2() {
		final boolean result = parse("SELECT ALL name FROM student");
		print("test_1_3_2", result);
		assertTrue(result);
	}

	public void test_1_4() {
		final boolean result = parse("SELECT student.id FROM student");
		print("test_1_4", result);
		assertTrue(result);
	}

	public void test_1_5() {
		final boolean result = parse("SELECT student.id, student.name FROM student");
		print("test_1_5", result);
		assertTrue(result);
	}

	// NO SUPPORT
	public void test_1_6_1() {
		final boolean result = parse("SELECT undergraduate.* FROM student as undergraduate");
		print("test_1_6_1", result);
		assertFalse(result);
	}

	public void test_1_6_2() {
		final boolean result = parse("SELECT undergraduate.id FROM student as undergraduate");
		print("test_1_6_2", result);
		assertTrue(result);
	}

	public void test_1_7() {
		final boolean result = parse("SELECT alias.id, alias.name FROM student as alias");
		print("test_1_7", result);
		assertTrue(result);
	}

	public void test_1_8() {
		final boolean result = parse("SELECT id FROM \"student\"");
		print("test_1_8", result);
		assertTrue(result);
	}

	public void test_1_9() {
		final boolean result = parse("SELECT id FROM \"public\".\"student\"");
		print("test_1_9", result);
		assertTrue(result);
	}

	public void test_1_10() {
		final boolean result = parse("SELECT t1.id, t2.name FROM \"public\".\"student\" as t1");
		print("test_1_10", result);
		assertTrue(result);
	}

	public void test_2_1() {
		final boolean result = parse("SELECT id FROM student WHERE id=1");
		print("test_2_1", result);
		assertTrue(result);
	}

	public void test_2_2() {
		final boolean result = parse("SELECT id, name FROM student WHERE id=1 AND name='John'");
		print("test_2_2", result);
		assertTrue(result);
	}

	public void test_2_3() {
		final boolean result = parse("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "nationality='IT' OR nationality='DE'");
		print("test_2_3", result);
		assertTrue(result);
	}

	public void test_2_4() {
		final boolean result = parse("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		print("test_2_4", result);
		assertTrue(result);
	}

	public void test_2_5() {
		final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is null");
		print("test_2_5", result);
		assertTrue(result);
	}

	public void test_2_6() {
		final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is not null");
		print("test_2_6", result);
		assertTrue(result);
	}

	public void test_2_7() {
		final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		print("test_2_7", result);
		assertTrue(result);
	}

	public void test_2_8() {
		final boolean result = parse("SELECT id, name FROM \"public\".\"student\" WHERE name<>'John'");
		print("test_2_8", result);
		assertTrue(result);
	}

	public void test_2_9() {
		final boolean result = parse("SELECT t1.id, t1.name FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.name<>'John'");
		print("test_2_9", result);
		assertTrue(result);
	}

	public void test_2_10() {
		final boolean result = parse("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"student\" as t1 "
				+ "WHERE t1.grede is not null AND t1.name<>'John'");
		print("test_2_10", result);
		assertTrue(result);
	}

	// NO SUPPORT
	public void test_2_11() {
		final boolean result = parse("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		print("test_2_11", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_2_12() {
		final boolean result = parse("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		print("test_2_12", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_1() {
		final boolean result = parse("SELECT MAX(score) FROM grade");
		print("test_3_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_2() {
		final boolean result = parse("SELECT MIN(score) FROM grade");
		print("test_3_2", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_3() {
		final boolean result = parse("SELECT AVG(score) FROM grade");
		print("test_3_3", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_4() {
		final boolean result = parse("SELECT SUM(amount) FROM tax");
		print("test_3_4", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_5() {
		final boolean result = parse("SELECT COUNT(*) FROM student");
		print("test_3_5", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_6() {
		final boolean result = parse("SELECT COUNT(id) FROM student");
		print("test_3_6", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_7() {
		final boolean result = parse("SELECT EVERY(id) FROM student");
		print("test_3_7", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_8() {
		final boolean result = parse("SELECT ANY(id) FROM student");
		print("test_3_8", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_3_9() {
		final boolean result = parse("SELECT SOME(id) FROM student");
		print("test_3_9", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_4_1() {
		final boolean result = parse("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
		print("test_4_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_4_2() {
		final boolean result = parse("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
		print("test_4_2", result);
		assertFalse(result);
	}

	public void test_4_3() {
		final boolean result = parse("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		print("test_4_3", result);
		assertTrue(result);
	}

	public void test_5_1() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_1", result);
		assertTrue(result);
	}

	public void test_5_1_1() {
		final boolean result = parse("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		print("test_5_1_1", result);
		assertTrue(result);
	}

	public void test_5_1_2() {
		final boolean result = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.score>=25");
		print("test_5_1_2", result);
		assertTrue(result);
	}

	public void test_5_1_3() {
		final boolean result = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.id AND t2.pass=true");
		print("test_5_1_3", result);
		assertTrue(result);
	}

	public void test_5_2() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_2", result);
		assertTrue(result);
	}

	public void test_5_3() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_3", result);
		assertTrue(result);
	}

	public void test_5_4() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_4", result);
		assertTrue(result);
	}

	public void test_5_5() {
		final boolean result = parse("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_5", result);
		assertTrue(result);
	}

	public void test_5_6() {
		final boolean result = parse("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_6", result);
		assertTrue(result);
	}

	public void test_5_7() {
		final boolean result = parse("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_7", result);
		assertTrue(result);
	}

	public void test_5_8() {
		final boolean result = parse("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
		print("test_5_8", result);
		assertTrue(result);
	}

	// NO SUPPORT
	public void test_5_9() {
		final boolean result = parse("SELECT id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		print("test_5_9", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_5_10() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		print("test_5_10", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_5_11() {
		final boolean result = parse("SELECT id, name, score FROM student JOIN grade USING (id)");
		print("test_5_11", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_6_1() {
		final boolean result = parse("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id, grade FROM grade) t2 WHERE t1.id=t2.sid");
		print("test_6_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_6_2() {
		final boolean result = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		print("test_6_2", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_6_3() {
		final boolean result = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		print("test_6_3", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_7_1() {
		final boolean result = parse("SELECT ('ID-' || student.id) as sid FROM student");
		print("test_7_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_7_2() {
		final boolean result = parse("SELECT (grade.score * 30 / 100) as percentage from grade");
		print("test_7_2", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_8_1() {
		final boolean result = parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
		print("test_8_1", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_8_2() {
		final boolean result = parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
		print("test_8_2", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_8_3() {
		final boolean result = parse("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
		print("test_8_3", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_8_4() {
		final boolean result = parse("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
		print("test_8_4", result);
		assertFalse(result);
	}

	// NO SUPPORT
	public void test_8_5() {
		final boolean result = parse("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		print("test_8_5", result);
		assertFalse(result);
	}

	public void test_9_1() {
		final boolean result = parse("SELECT id, name, address from student where name = 'John'");
		print("test_9_1", result);
		assertTrue(result);
	}

	public void test_9_2() {
		final boolean result = parse("SELECT id, name, address from student where id = 20");
		print("test_9_2", result);
		assertTrue(result);
	}

	public void test_9_3() {
		final boolean result = parse("SELECT payee, amount from tax where amount = 12.345");
		print("test_9_3", result);
		assertTrue(result);
	}

	public void test_9_4_1() {
		final boolean result = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		print("test_9_4_1", result);
		assertTrue(result);
	}

	public void test_9_4_2() {
		final boolean result = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		print("test_9_4_2", result);
		assertTrue(result);
	}

	public void test_9_4_3() {
		final boolean result = parse("SELECT id, name, address from student where birth_date = '1984-01-22'");
		print("test_9_4_3", result);
		assertTrue(result);
	}

	public void test_9_5() {
		final boolean result = parse("SELECT st_id, course, score from student where passed = TRUE");
		print("test_9_5", result);
		assertTrue(result);
	}
	
	public void test_10_1() {
		final boolean result = parse("SELECT name from grade, student where passed = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		print("test_9_5", result);
		assertTrue(result);
	}

	public void test_10_2() {
		final boolean result = parse("SELECT name from grade, student where passed = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		print("test_9_5", result);
		assertTrue(result);
	}
	
	private SQL99Parser parser;
	private QueryTree queryTree;
	private String queryText;
	
	private boolean parse(String input) {

		queryText = input;
		
		DBMetadata metadata = new DBMetadata();

		TableDefinition tableStudent = new TableDefinition("student");
		tableStudent.setAttribute(1, new Attribute("id", Types.INTEGER, true, null, 0));
		tableStudent.setAttribute(2, new Attribute("name", Types.VARCHAR, false, null, 0));
		tableStudent.setAttribute(3, new Attribute("address", Types.VARCHAR, false, null, 0));
		tableStudent.setAttribute(4, new Attribute("birth_date", Types.TIMESTAMP, false, null, 0));
		metadata.add(tableStudent);

		TableDefinition tableGrade = new TableDefinition("grade");
		tableGrade.setAttribute(1, new Attribute("sm_id", Types.INTEGER, false, null, 0));
		tableGrade.setAttribute(2, new Attribute("st_id", Types.INTEGER, false, null, 0));
		tableGrade.setAttribute(3, new Attribute("course", Types.VARCHAR, false, null, 0));
		tableGrade.setAttribute(4, new Attribute("score", Types.DECIMAL, false, null, 0));
		tableGrade.setAttribute(5, new Attribute("mark", Types.CHAR, false, null, 0));
		tableGrade.setAttribute(6, new Attribute("passed", Types.BOOLEAN, false, null, 0));
		metadata.add(tableGrade);

		TableDefinition tableSemester = new TableDefinition("semester");
		tableSemester.setAttribute(1, new Attribute("id", Types.INTEGER, true, null, 0));
		tableSemester.setAttribute(2, new Attribute("semester", Types.INTEGER, true, null, 0));
		metadata.add(tableSemester);

		TableDefinition tableErasmus = new TableDefinition("erasmus");
		tableErasmus.setAttribute(1, new Attribute("id", Types.INTEGER, true, null, 0));
		tableErasmus.setAttribute(2, new Attribute("name", Types.VARCHAR, false, null, 0));
		metadata.add(tableErasmus);

		TableDefinition tableTax = new TableDefinition("tax");
		tableTax.setAttribute(1, new Attribute("payee", Types.VARCHAR, false, null, 0));
		tableTax.setAttribute(2, new Attribute("amount", Types.DOUBLE, false, null, 0));
		metadata.add(tableTax);

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
	
	private void print(String title, boolean isSupported) {
		if (isSupported) {
			System.out.println(title + ": " + queryText);
			System.out.println("  Tables: " + queryTree.getTableSet());
			System.out.println("  Projection: " + queryTree.getProjection());
			System.out.println("  Selection: " + ((queryTree.getSelection() == null) ? "--" : queryTree.getSelection()));
			System.out.println("  Aliases: " + (queryTree.getAliasMap().isEmpty() ? "--" : queryTree.getAliasMap()));
			System.out.println("  Join conditions: " + (queryTree.getJoinCondition().isEmpty() ? "--" : queryTree.getJoinCondition()));
		} else {
			System.out.println("Parser doesn't support for query: " + queryText);
		}
		System.out.println();
	}
}
