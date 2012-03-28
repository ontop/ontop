package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.OWLOBDARefResultSet;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.BindingImpl;

public class SesameBindingSet implements BindingSet {

	private static final long serialVersionUID = -8455466574395305166L;
	private OWLOBDARefResultSet set = null;
	private int count = 0;
	private ValueFactory fact = new ValueFactoryImpl();

	public SesameBindingSet(OBDAResultSet set) {
		this.set = (OWLOBDARefResultSet) set;
		try {
			this.count = set.getColumCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public Binding getBinding(String bindingName) {
		// return the Binding with bindingName
		return createBinding(bindingName);
	}

	public Set<String> getBindingNames() {
		try {

			List<String> sign = set.getSignature();
			Set<String> bnames = new HashSet<String>(sign.size());
			for (String s : sign)
				bnames.add(s);
			return bnames;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Value getValue(String bindingName) {
		return createBinding(bindingName).getValue();
	}

	private Binding createBinding(String bindingName) {
		Value value = null;
		try {

			if (hasBinding(bindingName)) {
				int column = set.getSignature().indexOf(bindingName) + 1;
				COL_TYPE type = set.getType(column - 1);

				if (type == COL_TYPE.BOOLEAN) {
					boolean b = (set.getInt(column) == 1);
					value = fact.createLiteral(b);

				} else if (type == COL_TYPE.DATETIME) {
					XMLGregorianCalendar calendar;
					value = fact.createLiteral(set.getString(column));

				} else if (type == COL_TYPE.DECIMAL) {
					value = fact.createLiteral(set.getInt(column));

				} else if (type == COL_TYPE.DOUBLE) {
					value = fact.createLiteral(set.getDouble(column));

				} else if (type == COL_TYPE.INTEGER) {
					value = fact.createLiteral(set.getInt(column));

				} else if (type == COL_TYPE.STRING) {
					value = fact.createLiteral(set.getString(column));

				} else if (type == COL_TYPE.LITERAL) {
					value = fact.createLiteral(set.getLiteral(bindingName).getValue(), set.getLiteral(bindingName).getLanguage());

				} else if (type == COL_TYPE.OBJECT) {
					value = (Value) set.getObject(column);

				} else if (type == COL_TYPE.BNODE) {
					value = fact.createBNode(set.getBNode(bindingName).getName());

				} else if (type == COL_TYPE.LITERAL) { // URI!!!
					value = fact.createURI(set.getString(column));

				} else {
					value = fact.createURI(set.getLiteral(column).getValue());
				}
			}

			return new BindingImpl(bindingName, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean hasBinding(String bindingName) {

		try {
			return set.getSignature().contains(bindingName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Iterator<Binding> iterator() {

		List<Binding> allBindings = new LinkedList<Binding>();
		List<String> bindings;
		try {
			bindings = set.getSignature();
			for (String s : bindings)
				allBindings.add(createBinding(s));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allBindings.iterator();
	}

	public int size() {
		return count;
	}

}
