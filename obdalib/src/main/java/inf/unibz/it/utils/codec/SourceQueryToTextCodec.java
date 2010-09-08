package inf.unibz.it.utils.codec;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

/**
 * This class should be used to create a target query from a String respectively 
 * to create the String representation of a source query.
 * 
 * @author obda
 *
 */

public class SourceQueryToTextCodec extends ObjectToTextCodec<SourceQuery> {

	/**
	 * The constructor. Create a new instance of the SourceQueryToTextCodec
	 * @param apic the current api controller
	 */
	
	public SourceQueryToTextCodec(APIController apic) {
		super(apic);
	}

	/**
	 * Transforms the given input into a source query, if the String is not a valid
	 * target query representation null is returned.
	 */
	@Override
	public SourceQuery decode(String input) {
		RDBMSSQLQuery query;
		try {
			query = new RDBMSSQLQuery(input, apic);
			return query;
		} catch (QueryParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create the String representation of the given source query.
	 */
	@Override
	public String encode(SourceQuery input) {
		
		return input.getInputQuString();
	}


}
