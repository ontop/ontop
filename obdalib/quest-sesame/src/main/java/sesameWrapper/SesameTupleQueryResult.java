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
	
	
	public SesameTupleQueryResult(OBDAResultSet res)
	{
		this.result = res;
		try {
			this.count = result.getFetchSize();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//tuplequeryresult intf
	public List<String> getBindingNames() {
		
		try {
			return result.getSignature();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void close() throws QueryEvaluationException {
		try {
			result.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else
		{
			//pointer did not advance, hasNext was called already
			return hasNext;
		}
		return false;
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
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		else {
			//processing now current row of solutions
			read= true;
			return new SesameBindingSet(result);
			
		}
					
	}

	public void remove() throws QueryEvaluationException {
		
	}
	//end tuplequeryresult intf

	
	
}
