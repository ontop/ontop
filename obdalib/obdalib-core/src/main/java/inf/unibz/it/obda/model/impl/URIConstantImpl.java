package inf.unibz.it.obda.model.impl;



import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.URIConstant;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl implements URIConstant {

	private URI uri = null;
	private int identifier = -1;

	/**
	 * The default constructor.
	 *
	 * @param uri URI from a term.
	 */
	protected URIConstantImpl(URI uri) {
		this.uri = uri;
		this.identifier = uri.hashCode();
	}

	@Override
	public boolean equals(Object obj){

		if(obj == null || !(obj instanceof URIConstantImpl))
			return false;

		URIConstantImpl uri2 = (URIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode(){
		return identifier;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public String getName() {
		return uri.toString();
	}

	@Override
	public Term copy() {
		try {
			return new URIConstantImpl(new URI(uri.toString()));
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
