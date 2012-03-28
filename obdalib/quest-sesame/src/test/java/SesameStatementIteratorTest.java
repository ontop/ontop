import junit.framework.TestCase;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;

import sesameWrapper.SesameRDFHandler;
import sesameWrapper.SesameStatementIterator;


public class SesameStatementIteratorTest extends TestCase {

	public SesameStatementIteratorTest(String name) {
		super(name);
	}
	
	public void test1() throws RDFHandlerException
	{
		SesameRDFHandler rdfHandler = new SesameRDFHandler();
		SesameStatementIterator stIterator;
		
		ValueFactory vf = new ValueFactoryImpl();
		Resource res = new URIImpl("http://person1");
		URI uri = new URIImpl("http://says");
		Value val = new LiteralImpl("hello");
		Statement st = vf.createStatement(res, uri, val);
		
		System.out.println("Handle statement: "+st.toString());
		rdfHandler.handleStatement(st);
		
		stIterator = rdfHandler.getAssertionIterator();
		
		System.out.println("HasNext: "+stIterator.hasNext());
		System.out.println("Next: "+stIterator.next().toString());
	}

}
