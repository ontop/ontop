package sesameWrapper;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;

import java.sql.SQLException;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class SesameTupleQueryResult implements TupleQueryResult{

	private OBDAResultSet result = null;
	private int count = 0;
	private boolean read = true, hasNext = false;
	
	
	public SesameTupleQueryResult(OBDAResultSet res) throws OBDAException
	{
		this.result = res;
		this.count = result.getFetchSize();
	}
	
	//tuplequeryresult intf
	public List<String> getBindingNames() {
		
		try {
			return result.getSignature();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void close() throws QueryEvaluationException {
		try {
			result.close();
		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e.getMessage());
		}		
	}

	public boolean hasNext() throws QueryEvaluationException {
		if (read)
		{
			//row was read, we can step forward the pointer in the result set
			try {
				hasNext = result.nextRow();
				//new row has not been read yet
				read = false;
				return hasNext;
				
			} catch (OBDAException e) {
				e.printStackTrace();
				throw new QueryEvaluationException(e.getMessage());
			}
		}
		else
		{
			//pointer did not advance, hasNext was called already
			return hasNext;
		}
	}

	public BindingSet next() throws QueryEvaluationException {
		
		if (read)
		{
			//current row has been read, we can advance the pointer
			try {
				if (result.nextRow())
					//process new row
					return new SesameBindingSet(result);
				else
					throw new QueryEvaluationException("End of result set!");
			} catch (OBDAException e) {
				e.printStackTrace();
				throw new QueryEvaluationException(e.getMessage());
			}
		}
		
		else {
			//processing now current row of solutions
			read= true;
			return new SesameBindingSet(result);
			
		}
					
	}

	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("Remove not supported!");
	}
	//end tuplequeryresult intf

	
	
}
