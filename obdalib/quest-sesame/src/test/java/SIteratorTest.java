import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import sesameWrapper.SesameStatementIterator;
import junit.framework.TestCase;


public class SIteratorTest extends TestCase {

	public SIteratorTest(String name) {
		super(name);
	}
	
	public void test1()
	{
		SesameStatementIterator s = new SesameStatementIterator();
		System.out.println(s.size());
		System.out.println(s.hasNext());
		
		ValueFactory vf = new ValueFactoryImpl();
		Resource res = new URIImpl("http://person1");
		URI uri = new URIImpl("http://says");
		Value val = new LiteralImpl("hello");
		Statement st = vf.createStatement(res, uri, val);
		s.add(st);
		
		System.out.println(s.size());
		System.out.println(s.hasNext());
		System.out.println(s.next().toString());
	}

}
