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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.resultset.QuestResultset;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.BindingImpl;

public class SesameBindingSet implements BindingSet {

	private static final long serialVersionUID = -8455466574395305166L;
	private QuestResultset set = null;
	private int count = 0;
	private final ValueFactory fact = new ValueFactoryImpl();
	private final Set<String> bindingnames;
//	private List<String> signature;
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	public SesameBindingSet(TupleResultSet set, Set<String> bindingnames) {
		this.bindingnames = bindingnames;
//		this.signature = signature;
		this.set = (QuestResultset) set;
		this.count = bindingnames.size();
		
	}

	public Binding getBinding(String bindingName) {
		// return the Binding with bindingName
		return createBinding(bindingName);
	}

	
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
				} 
				else {
					if (c instanceof BNode) {
						value = fact.createBNode(c.getValue());
					} 
					else if (c instanceof URIConstant) {
						value = fact.createURI(c.getValue());
					} 
					else if (c instanceof ValueConstant) {
						ValueConstant literal = (ValueConstant)c;
						COL_TYPE col_type = literal.getType();
						
						if (col_type == COL_TYPE.LITERAL_LANG) {
							// special case
							value = fact.createLiteral(c.getValue(), literal.getLanguage());
						} 
						else if (col_type == COL_TYPE.LITERAL) {
							// also a special case!
							value = fact.createLiteral(c.getValue());
						} 
						else if (col_type == COL_TYPE.OBJECT) {
							// TODO: Replace this with object datatype in the future
							value = fact.createLiteral(c.getValue(), XMLSchema.STRING);
						} 
						else {
							String uri = dtfac.getDataTypeURI(col_type);
							if (uri == null)
								throw new RuntimeException("Found unknown TYPE for constant: " + c + " with COL_TYPE="+ col_type + " and variable=" + bindingName);
							
							URI datatype = fact.createURI(uri);
							value = fact.createLiteral(c.getValue(), datatype);							
						}
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

	public boolean hasBinding(String bindingName) {
		return bindingnames.contains(bindingName);
//		try {
//			return set.getSignature().contains(bindingName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
	}

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

	public int size() {
		return count;
	}

}
