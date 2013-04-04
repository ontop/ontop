package it.unibz.krdb.obda.codec;

import java.util.Collection;

/**
 * Note: This is a legacy code. Do not use instances of this class. This code
 * is used by the old test cases which needed to be updated.
 */
public interface XMLEncodable {
	
	/**
	 * Returns the tag used to encode Assertions of specific kinds.
	 */
	public String getElementTag();
	
	public Collection<String> getAttributes();
}
