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
  public void test_1_10() {
    final boolean result = parse("SELECT t1.id, t2.name FROM \"schema\".\"student\" as t1");
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
    		"FROM student as undergraduate WHERE undergraduate.name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_5() {
    final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is null");
    assertTrue(result);
  }

  @Test
  public void test_2_6() {
    final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is not null");
    assertTrue(result);
  }

  @Test
  public void test_2_7() {
    final boolean result = parse("SELECT id, name, grade FROM student WHERE grade is null AND name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_8() {
    final boolean result = parse("SELECT id, name FROM \"Public\".\"student\" WHERE name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_9() {
    final boolean result = parse("SELECT t1.id, t1.name FROM \"Public\".\"student\" as t1 " +
    		"WHERE t1.name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_2_10() {
    final boolean result = parse("SELECT t1.id, t1.name, t1.grade FROM \"Public\".\"student\" as t1 " +
        "WHERE t1.grede is not null AND t1.name<>\"John\"");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_1() {
    final boolean result = parse("select id, street, number, city, state, country from address");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_2() {
    final boolean result = parse("select id, name, lastname, dateofbirth, ssn from broker");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_3() {
    final boolean result = parse("select id, addressid from broker");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_4() {
    final boolean result = parse("select id, name, lastname, dateofbirth, ssn from client");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_5() {
    final boolean result = parse("select id, name, lastname, addressid from client");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_6() {
    final boolean result = parse("select id, name, marketshares, networth from company");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_7() {
    final boolean result = parse("select id, addressid from company");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_8() {
    final boolean result = parse("select id, numberofshares, sharetype from stockinformation");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_9() {
    final boolean result = parse("select distinct date from stockbooklist");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_10() {
    final boolean result = parse("select brokerid, clientid from brokerworksfor where clientid IS NOT NULL");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_11() {
    final boolean result = parse("select brokerid, companyid from brokerworksfor where companyid IS NOT NULL");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_12() {
    final boolean result = parse("select id, date from transaction");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_13() {
    final boolean result = parse("select id, brokerid, forclientid, stockid from transaction where forclientid IS NOT NULL");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_14() {
    final boolean result = parse("select id, brokerid, forcompanyid, stockid from transaction where forcompanyid IS NOT NULL");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_15() {
    final boolean result = parse("select id, companyid from stockinformation");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_16() {
    final boolean result = parse("select date, stockid from stockbooklist");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_17() {
    final boolean result = parse("select clientid from broker,client,brokerworksfor where client.id = broker.id and brokerid=broker.id and client.id=clientid");
    assertTrue(result);
  }

  @Test
  public void test_stockexchange_18() {
    final boolean result = parse("SELECT id FROM transaction WHERE type=true");
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
