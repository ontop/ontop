package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl extends AbstractLiteral implements URIConstant {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1263974895010238519L;

	private final IRI iri;

	private final int identifier;

	private final String iristr;

	/**
	 * The default constructor.
	 * 
	 * @param uri
	 *            URI from a term.
	 */
	protected URIConstantImpl(IRI uri) {
		this.iri = uri;
		this.iristr = uri.toString();
		this.identifier = uri.hashCode();
	}

	protected URIConstantImpl(String uri) {
		this.iri = OBDADataFactoryImpl.getIRI(uri);
		this.iristr = uri.toString();
		this.identifier = uri.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof URIConstantImpl))
			return false;

		URIConstantImpl uri2 = (URIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public IRI getURI() {
		return iri;
	}

	// @Override
	// public String getName() {
	// return uri.toString();
	// }

	@Override
	public URIConstant clone() {
		return this;
		// URIConstantImpl clone = new URIConstantImpl(uri);
		// clone.identifier = identifier;
		// return clone;
	}

	@Override
	public String toString() {
		return iristr;
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}

	@Override
	public Map<Variable, Integer> getVariableCount() {
		return new HashMap<Variable, Integer>();
	}

	@Override
	public Atom asAtom() {
		throw new RuntimeException("Impossible to cast as atom: "
				+ this.getClass());
	}

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.OBJECT;
	}

	@Override
	public String getValue() {
		return iristr;
	}

	@Override
	public String getLanguage() {
		return null;
	}

}
