package sesameWrapper;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestResultset;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;

public class SesameTupleQuery implements TupleQuery {

	private static final long serialVersionUID = 1L;

	private String queryString, baseURI;
	private QuestDBStatement stm;

	private ValueFactory fact = new ValueFactoryImpl();

	public SesameTupleQuery(String queryString, String baseURI,
			QuestDBStatement statement) throws MalformedQueryException {
		if (queryString.toLowerCase().contains("select")) {
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.stm = statement;
		} else
			throw new MalformedQueryException("Tuple query expected!");
	}

	// needed by TupleQuery interface
	public TupleQueryResult evaluate() throws QueryEvaluationException {

		try {
			// execute query and return new type of result
			OBDAResultSet res = stm.execute(queryString);
			List<String> signature = res.getSignature();

			List<BindingSet> results = new LinkedList<BindingSet>();
			while (res.nextRow()) {
				MapBindingSet set = new MapBindingSet(signature.size() * 2);
				for (String name : signature) {
					Binding binding = createBinding(name, res);
					if (binding != null) {
						set.addBinding(binding);
					}
				}
				results.add(set);
			}

			return new TupleQueryResultImpl(signature, results);

		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e.getMessage());
		}
		finally{
			try {
				stm.close();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
		}
	}

	private Binding createBinding(String bindingName, OBDAResultSet set) {
		
		SesameBindingSet bset = new SesameBindingSet(set);
		return bset.getBinding(bindingName);
	}

	// needed by TupleQuery interface
	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {

		TupleQueryResult result = evaluate();
		handler.startQueryResult(result.getBindingNames());
		while (result.hasNext())
			handler.handleSolution(result.next());
		handler.endQueryResult();

	}

	public int getMaxQueryTime() {
		return -1;
	}

	public void setMaxQueryTime(int maxQueryTime) {
	}

	public void clearBindings() {
		// TODO Auto-generated method stub

	}

	public BindingSet getBindings() {
		try {
			return evaluate().next();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Dataset getDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getIncludeInferred() {
		return true;
	}

	public void removeBinding(String name) {
		// TODO Auto-generated method stub

	}

	public void setBinding(String name, Value value) {
		// TODO Auto-generated method stub

	}

	public void setDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	public void setIncludeInferred(boolean includeInferred) {
		// always true

	}

}
