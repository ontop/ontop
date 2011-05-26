package inf.unibz.it.utils.codec;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSSQLQuery;


/**
 * This class should be used to create a target query from a String respectively
 * to create the String representation of a source query.
 *
 * @author obda
 *
 */
public class SourceQueryToTextCodec extends ObjectToTextCodec<Query> {

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
	public Query decode(String input) {
		RDBMSSQLQuery query;
		query = new RDBMSSQLQuery(input);
		return query;
	}

	/**
	 * Create the String representation of the given source query.
	 */
	@Override
	public String encode(Query input) {
		if (input == null)
			return "";

		return input.toString();
	}
}
