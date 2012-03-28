package sesameWrapper;

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.*;
import java.util.concurrent.Semaphore;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.*;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class SesameRDFHandler extends RDFHandlerBase {
	
	private List<Statement> buffer;
	private SesameStatementIterator sesameIterator;
	private ListIterator<Statement> stIterator;
	private Semaphore empty, full;
	private int size = 1;
	
	public void startRDF()
		throws RDFHandlerException
	{
		sesameIterator.startRDF();
	}

	public void endRDF()
		throws RDFHandlerException
	{
		System.out.println("Buffer size: "+buffer.size());
		sesameIterator.endRDF();
	}

	
	public SesameRDFHandler(Semaphore empty, Semaphore full) {
		
		buffer = new LinkedList<Statement>();
		stIterator = buffer.listIterator();
		sesameIterator =  new SesameStatementIterator(stIterator, empty, full);
		this.empty = empty;
		this.full = full;
		
	}
	
	
	
	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		//wait until not full
		if (buffer.size() >= size)
			try {
				//System.out.println("Wait for buffer");
				full.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			//add statement to buffer
		synchronized (stIterator) {
			stIterator.add(st);
		}
			

		//add to buffer and step back
		System.out.print("Handle statement: "+st.getSubject().toString().split("#")[1] + " " 
				+st.getPredicate().getLocalName() + " ");
		if (st.getObject().toString().split("#").length > 1)
			System.out.println(st.getObject().toString().split("#")[1]);
		else
			System.out.println(st.getObject().toString().split("#")[0]);

		
		//step pointer to the list back
		stIterator.previous();
		
		//not empty anymore
		empty.release();

		//System.out.println("Buffer "+buffer.size());
		
	}
	
	
	public ListIterator<Statement> getStatementIterator()
	{
		return stIterator;
	}
	
	public SesameStatementIterator getAssertionIterator()
	{
		return sesameIterator;
	}

}
