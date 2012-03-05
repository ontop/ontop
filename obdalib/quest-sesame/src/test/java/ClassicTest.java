import sesameWrapper.SesameClassicRepo;
import junit.framework.TestCase;


public class ClassicTest extends TestCase {

	public ClassicTest(String name) {
		super(name);
	}


	public void testSesameClassicRepo() {
		String file =  "/home/timi/ontologies/helloworld/helloworld.owl";
		try {
			SesameClassicRepo rep = new SesameClassicRepo("myname", file);
			
			rep.initialize();
			
			System.out.println(rep.getConnection().toString());
			
		} catch (Exception e) {
			System.out.println("EXCEPTION!!");
			e.printStackTrace();
		}
		
		System.out.println("Done");
	}

}
