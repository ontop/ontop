package inf.unibz.ontop.sesame.tests.general;
import org.junit.Test;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SesameRepoTest {
Logger log = LoggerFactory.getLogger("newlogger");
	@Test
	public void test() {
		RemoteRepositoryManager m = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
		log.info("New message");
	}

}
