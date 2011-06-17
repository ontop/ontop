package it.unibz.krdb.obda.parser;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQL99ParserTest extends TestCase
{
  final static Logger log = LoggerFactory.getLogger(SQL99ParserTest.class);

  @Test
  public void test_1_1() {
    final boolean result = parse("SELECT * FROM student");
    assertTrue(result);
  }

  @Test
  public void test_1_2() {
    final boolean result = parse("SELECT id FROM student");
    assertTrue(result);
  }

  @Test
  public void test_1_3() {
    final boolean result = parse("SELECT id, name FROM student");
    assertTrue(result);
  }

  @Test
  public void test_1_4() {
    final boolean result = parse("SELECT student.id FROM student");
    assertTrue(result);
  }

  @Test
  public void test_1_5() {
    final boolean result = parse("SELECT student.id, student.name FROM student");
    assertTrue(result);
  }

  @Test
  public void test_1_6() {
    final boolean result = parse("SELECT undergraduate.id FROM student as undergraduate");
    assertTrue(result);
  }

  @Test
  public void test_1_7() {
    final boolean result = parse("SELECT alias.id, alias.name FROM student as alias");
    assertTrue(result);
  }

  @Test
  public void test_1_8() {
    final boolean result = parse("SELECT id FROM \"student\"");
    assertTrue(result);
  }

  @Test
  public void test_1_9() {
    final boolean result = parse("SELECT id FROM \"schema\".\"student\"");
    assertTrue(result);
  }

  @Test
  public void test_2_1() {
    final boolean result = parse("SELECT id FROM student WHERE id=1");
    assertTrue(result);
  }

  @Test
  public void test_2_2() {
    final boolean result = parse("SELECT id, name FROM student WHERE id=1 AND name=\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_3() {
    final boolean result = parse("SELECT id, name, semester, birth_year, nationality " +
    		"FROM student " +
    		"WHERE name<>\"John\" AND semester>2 AND semester<7 AND " +
    		"birth_year>=1984 AND birth_year<=1990 AND " +
    		"nationality=\"IT\" OR nationality=\"DE\"");
    assertTrue(result);
  }

  @Test
  public void test_2_4() {
    final boolean result = parse("SELECT undergraduate.id, undergraduate.name " +
    		"FROM student as undergraduate " +
    		"WHERE undergraduate.name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_5() {
    final boolean result = parse("SELECT id, name FROM \"Public\".\"student\" WHERE name<>\"John\"");
    assertTrue(result);
  }

  private boolean parse(String input) {
    ANTLRStringStream inputStream = new ANTLRStringStream(input);
    SQL99Lexer lexer = new SQL99Lexer(inputStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQL99Parser parser = new SQL99Parser(tokenStream);

    try {
      parser.parse();
    }
    catch (RecognitionException e) {
      log.debug(e.getMessage());
    }

    if (parser.getNumberOfSyntaxErrors() != 0) {
      return false;
    }
    return true;
  }
}
