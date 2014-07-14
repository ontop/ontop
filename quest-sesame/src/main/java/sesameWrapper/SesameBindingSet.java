package sesameWrapper;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestResultset;
import it.unibz.krdb.obda.sesame.SesameHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.BindingImpl;

public class SesameBindingSet implements BindingSet {

	private static final long serialVersionUID = -8455466574395305166L;
	private QuestResultset set = null;
	private int count = 0;
	private final Set<String> bindingnames;
//	private List<String> signature;

	private final SesameHelper helper = new SesameHelper();
	
	public SesameBindingSet(TupleResultSet set, Set<String> bindingnames) {
		this.bindingnames = bindingnames;
//		this.signature = signature;
		this.set = (QuestResultset) set;
		this.count = bindingnames.size();
		
	}

	@Override
	public Binding getBinding(String bindingName) {
		// return the Binding with bindingName
		return createBinding(bindingName);
	}

	@Override	
	public Set<String> getBindingNames() {
		return bindingnames;
//		try {
//
//			List<String> sign = set.getSignature();
//			Set<String> bnames = new HashSet<String>(sign.size());
//			for (String s : sign)
//				bnames.add(s);
//			return bnames;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
	}

	@Override
	public Value getValue(String bindingName) {
		return createBinding(bindingName).getValue();
	}

	private Binding createBinding(String bindingName) {
		Value value = null;
		try {
			if (hasBinding(bindingName)) {
//				int column = set.getSignature().indexOf(bindingName) + 1;
				Constant c = set.getConstant(bindingName);
				if (c == null) {
					return null;
						} else if (col_type == COL_TYPE.GEOMETRY) {
							URI datatype = fact.createURI(OBDAVocabulary.GEOSPARQL_WKT_LITERAL_DATATYPE);
							value = fact.createLiteral(c.getValue(), datatype);
						} 							
				else {
					if (c instanceof ValueConstant) {
						value = helper.getLiteral((ValueConstant)c);
					}
					else {
						value = helper.getResource((ObjectConstant)c);
					}
				}
			}
			return new BindingImpl(bindingName, value);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
//		return null;
	}

	@Override
	public boolean hasBinding(String bindingName) {
		return bindingnames.contains(bindingName);
//		try {
//			return set.getSignature().contains(bindingName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
	}

	@Override
	public Iterator<Binding> iterator() {

		List<Binding> allBindings = new LinkedList<Binding>();
		List<String> bindings;
		try {
			bindings = set.getSignature();
			for (String s : bindings)
				allBindings.add(createBinding(s));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return allBindings.iterator();
	}

	@Override
	public int size() {
		return count;
	}
}
