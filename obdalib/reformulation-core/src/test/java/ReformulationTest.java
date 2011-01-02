import junit.framework.TestCase;
import org.obda.reformulation.tests.Tester;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReformulationTest extends TestCase {

	private Tester tester = null;
Logger										log				= LoggerFactory.getLogger(this.getClass());

	private String propfile = "src/test/resources/test.properties";
	public ReformulationTest(){
		tester = new Tester(propfile);
	}


	public void test_1_0_0() throws Exception{
		String ontoname = "test_1_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_2_0_0() throws Exception{
		String ontoname = "test_2_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_3_0_0() throws Exception{
		String ontoname = "test_3_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_4_1_1() throws Exception{
		String ontoname = "test_4_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_4_1_2() throws Exception{
		String ontoname = "test_4_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_5_1_1() throws Exception{
		String ontoname = "test_5_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_5_1_2() throws Exception{
		String ontoname = "test_5_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_6_1_1() throws Exception{
		String ontoname = "test_6_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_6_1_2() throws Exception{
		String ontoname = "test_6_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_7_1_1() throws Exception{
		String ontoname = "test_7_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_7_1_2() throws Exception{
		String ontoname = "test_7_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_8_1_1() throws Exception{
		String ontoname = "test_8_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_8_1_2() throws Exception{
		String ontoname = "test_8_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_9_1_1() throws Exception{
		String ontoname = "test_9_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_9_1_2() throws Exception{
		String ontoname = "test_9_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_10_0_0() throws Exception{
		String ontoname = "test_10_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_10_1_0() throws Exception{
		String ontoname = "test_10_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_10_0_3() throws Exception{
		String ontoname = "test_10_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_11_0_0() throws Exception{
		String ontoname = "test_11_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_11_1_0() throws Exception{
		String ontoname = "test_11_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_11_0_3() throws Exception{
		String ontoname = "test_11_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_12_0_0() throws Exception{
		String ontoname = "test_12_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_12_1_0() throws Exception{
		String ontoname = "test_12_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_12_0_3() throws Exception{
		String ontoname = "test_12_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_1_1() throws Exception{
		String ontoname = "test_13_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_1_2() throws Exception{
		String ontoname = "test_13_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_2_1() throws Exception{
		String ontoname = "test_13_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_2_2() throws Exception{
		String ontoname = "test_13_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_1_3() throws Exception{
		String ontoname = "test_13_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_13_2_3() throws Exception{
		String ontoname = "test_13_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_1_1() throws Exception{
		String ontoname = "test_14_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_1_2() throws Exception{
		String ontoname = "test_14_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_2_1() throws Exception{
		String ontoname = "test_14_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_2_2() throws Exception{
		String ontoname = "test_14_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_1_3() throws Exception{
		String ontoname = "test_14_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_14_2_3() throws Exception{
		String ontoname = "test_14_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_1_1() throws Exception{
		String ontoname = "test_15_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_1_2() throws Exception{
		String ontoname = "test_15_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_2_1() throws Exception{
		String ontoname = "test_15_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_2_2() throws Exception{
		String ontoname = "test_15_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_1_3() throws Exception{
		String ontoname = "test_15_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_15_2_3() throws Exception{
		String ontoname = "test_15_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_1_1() throws Exception{
		String ontoname = "test_16_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_1_2() throws Exception{
		String ontoname = "test_16_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_2_1() throws Exception{
		String ontoname = "test_16_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_2_2() throws Exception{
		String ontoname = "test_16_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_1_3() throws Exception{
		String ontoname = "test_16_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_16_2_3() throws Exception{
		String ontoname = "test_16_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_1_1() throws Exception{
		String ontoname = "test_17_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_1_2() throws Exception{
		String ontoname = "test_17_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_2_1() throws Exception{
		String ontoname = "test_17_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_2_2() throws Exception{
		String ontoname = "test_17_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_1_3() throws Exception{
		String ontoname = "test_17_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_17_2_3() throws Exception{
		String ontoname = "test_17_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_1_1() throws Exception{
		String ontoname = "test_18_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_1_2() throws Exception{
		String ontoname = "test_18_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_2_1() throws Exception{
		String ontoname = "test_18_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_2_2() throws Exception{
		String ontoname = "test_18_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_1_3() throws Exception{
		String ontoname = "test_18_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_18_2_3() throws Exception{
		String ontoname = "test_18_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_19_0_0() throws Exception{
		String ontoname = "test_19_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_19_1_0() throws Exception{
		String ontoname = "test_19_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_19_0_3() throws Exception{
		String ontoname = "test_19_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_20_0_0() throws Exception{
		String ontoname = "test_20_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_20_1_0() throws Exception{
		String ontoname = "test_20_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_20_0_3() throws Exception{
		String ontoname = "test_20_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_21_0_0() throws Exception{
		String ontoname = "test_21_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_21_1_0() throws Exception{
		String ontoname = "test_21_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_21_0_3() throws Exception{
		String ontoname = "test_21_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_1_1() throws Exception{
		String ontoname = "test_22_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_1_2() throws Exception{
		String ontoname = "test_22_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_2_1() throws Exception{
		String ontoname = "test_22_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_2_2() throws Exception{
		String ontoname = "test_22_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_1_3() throws Exception{
		String ontoname = "test_22_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_22_2_3() throws Exception{
		String ontoname = "test_22_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_1_1() throws Exception{
		String ontoname = "test_23_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_1_2() throws Exception{
		String ontoname = "test_23_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_2_1() throws Exception{
		String ontoname = "test_23_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_2_2() throws Exception{
		String ontoname = "test_23_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_1_3() throws Exception{
		String ontoname = "test_23_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_23_2_3() throws Exception{
		String ontoname = "test_23_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_1_1() throws Exception{
		String ontoname = "test_24_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_1_2() throws Exception{
		String ontoname = "test_24_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_2_1() throws Exception{
		String ontoname = "test_24_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_2_2() throws Exception{
		String ontoname = "test_24_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_1_3() throws Exception{
		String ontoname = "test_24_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_24_2_3() throws Exception{
		String ontoname = "test_24_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_1_1() throws Exception{
		String ontoname = "test_25_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_1_2() throws Exception{
		String ontoname = "test_25_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_2_1() throws Exception{
		String ontoname = "test_25_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_2_2() throws Exception{
		String ontoname = "test_25_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_1_3() throws Exception{
		String ontoname = "test_25_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_25_2_3() throws Exception{
		String ontoname = "test_25_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_1_1() throws Exception{
		String ontoname = "test_26_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_1_2() throws Exception{
		String ontoname = "test_26_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_2_1() throws Exception{
		String ontoname = "test_26_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_2_2() throws Exception{
		String ontoname = "test_26_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_1_3() throws Exception{
		String ontoname = "test_26_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_26_2_3() throws Exception{
		String ontoname = "test_26_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_1_1() throws Exception{
		String ontoname = "test_27_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_1_2() throws Exception{
		String ontoname = "test_27_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_2_1() throws Exception{
		String ontoname = "test_27_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_2_2() throws Exception{
		String ontoname = "test_27_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_1_3() throws Exception{
		String ontoname = "test_27_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_27_2_3() throws Exception{
		String ontoname = "test_27_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_28() throws Exception{
		String ontoname = "test_28";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_29() throws Exception{
		String ontoname = "test_29";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_1_1() throws Exception{
		String ontoname = "test_30_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_1_2() throws Exception{
		String ontoname = "test_30_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_2_1() throws Exception{
		String ontoname = "test_30_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_2_2() throws Exception{
		String ontoname = "test_30_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_1_3() throws Exception{
		String ontoname = "test_30_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_30_2_3() throws Exception{
		String ontoname = "test_30_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_31() throws Exception{
		String ontoname = "test_31";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_32() throws Exception{
		String ontoname = "test_32";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_33() throws Exception{
		String ontoname = "test_33";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_34() throws Exception{
		String ontoname = "test_34";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_35() throws Exception{
		String ontoname = "test_35";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_36() throws Exception{
		String ontoname = "test_36";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_37() throws Exception{
		String ontoname = "test_37";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_38() throws Exception{
		String ontoname = "test_38";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_39() throws Exception{
		String ontoname = "test_39";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_40() throws Exception{
		String ontoname = "test_40";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_41() throws Exception{
		String ontoname = "test_41";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_42() throws Exception{
		String ontoname = "test_42";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_43_0() throws Exception{
		String ontoname = "test_43_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_43_1() throws Exception{
		String ontoname = "test_43_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_44_0() throws Exception{
		String ontoname = "test_44_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_44_1() throws Exception{
		String ontoname = "test_44_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_45_0() throws Exception{
		String ontoname = "test_45_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_45_1() throws Exception{
		String ontoname = "test_45_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_46_0() throws Exception{
		String ontoname = "test_46_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_46_1() throws Exception{
		String ontoname = "test_46_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_47_0_0() throws Exception{
		String ontoname = "test_47_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_48_0_0() throws Exception{
		String ontoname = "test_48_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_49_0_0() throws Exception{
		String ontoname = "test_49_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_50_1_1() throws Exception{
		String ontoname = "test_50_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_50_1_2() throws Exception{
		String ontoname = "test_50_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_51_1_1() throws Exception{
		String ontoname = "test_51_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_51_1_2() throws Exception{
		String ontoname = "test_51_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_52_1_1() throws Exception{
		String ontoname = "test_52_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_52_1_2() throws Exception{
		String ontoname = "test_52_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_53_1_1() throws Exception{
		String ontoname = "test_53_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_53_1_2() throws Exception{
		String ontoname = "test_53_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_54_1_1() throws Exception{
		String ontoname = "test_54_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_54_1_2() throws Exception{
		String ontoname = "test_54_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_55_1_1() throws Exception{
		String ontoname = "test_55_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_55_1_2() throws Exception{
		String ontoname = "test_55_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_56_0_0() throws Exception{
		String ontoname = "test_56_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_56_1_0() throws Exception{
		String ontoname = "test_56_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_56_0_3() throws Exception{
		String ontoname = "test_56_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_57_0_0() throws Exception{
		String ontoname = "test_57_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_57_1_0() throws Exception{
		String ontoname = "test_57_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_57_0_3() throws Exception{
		String ontoname = "test_57_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_58_0_0() throws Exception{
		String ontoname = "test_58_0_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_58_1_0() throws Exception{
		String ontoname = "test_58_1_0";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_58_0_3() throws Exception{
		String ontoname = "test_58_0_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_1_1() throws Exception{
		String ontoname = "test_59_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_1_2() throws Exception{
		String ontoname = "test_59_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_2_1() throws Exception{
		String ontoname = "test_59_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_2_2() throws Exception{
		String ontoname = "test_59_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_1_3() throws Exception{
		String ontoname = "test_59_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_59_2_3() throws Exception{
		String ontoname = "test_59_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_1_1() throws Exception{
		String ontoname = "test_60_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_1_2() throws Exception{
		String ontoname = "test_60_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_2_1() throws Exception{
		String ontoname = "test_60_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_2_2() throws Exception{
		String ontoname = "test_60_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_1_3() throws Exception{
		String ontoname = "test_60_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_60_2_3() throws Exception{
		String ontoname = "test_60_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_1_1() throws Exception{
		String ontoname = "test_61_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_1_2() throws Exception{
		String ontoname = "test_61_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_2_1() throws Exception{
		String ontoname = "test_61_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_2_2() throws Exception{
		String ontoname = "test_61_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_1_3() throws Exception{
		String ontoname = "test_61_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_61_2_3() throws Exception{
		String ontoname = "test_61_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_1_1() throws Exception{
		String ontoname = "test_62_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_1_2() throws Exception{
		String ontoname = "test_62_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_2_1() throws Exception{
		String ontoname = "test_62_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_2_2() throws Exception{
		String ontoname = "test_62_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_1_3() throws Exception{
		String ontoname = "test_62_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_62_2_3() throws Exception{
		String ontoname = "test_62_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_1_1() throws Exception{
		String ontoname = "test_63_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_1_2() throws Exception{
		String ontoname = "test_63_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_2_1() throws Exception{
		String ontoname = "test_63_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_2_2() throws Exception{
		String ontoname = "test_63_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_1_3() throws Exception{
		String ontoname = "test_63_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_63_2_3() throws Exception{
		String ontoname = "test_63_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_1_1() throws Exception{
		String ontoname = "test_64_1_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_1_2() throws Exception{
		String ontoname = "test_64_1_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_2_1() throws Exception{
		String ontoname = "test_64_2_1";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_2_2() throws Exception{
		String ontoname = "test_64_2_2";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_1_3() throws Exception{
		String ontoname = "test_64_1_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

	public void test_64_2_3() throws Exception{
		String ontoname = "test_64_2_3";

		log.debug("Test case: {}", ontoname);
		log.debug("Testing in-memory db/direct-mappings");
		tester.load(ontoname, false, false);
		Set<String> queryids = tester.getQueryIds();
		log.debug("Testing {} queries", queryids.size());		Iterator<String> qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}

		log.debug("Testing in-memory db/complex-mappings");
		tester.load(ontoname, true, true);
		queryids = tester.getQueryIds();
		qit = queryids.iterator();
		while(qit.hasNext()){
			String id = qit.next();
		log.debug("Testing query: {}", id);
			Set<String> exp = tester.getExpectedResult(id);
			Set<String> res = tester.executeQuery(id);
			assertTrue(exp.size() == res.size());
			for (String realResult : res) {
				assertTrue(exp.contains(realResult));
			}
		}
	}

}
