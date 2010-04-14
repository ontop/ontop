package inf.unibz.it.utils.codec;

import java.util.Collection;

public interface XMLEncodable {
	/***************************************************************************
	 * Returns the tag used to encode Assertions of specific kinds.
	 * 
	 * @return
	 */
	public String getElementTag();
	
	public Collection<String> getAttributes();
}
