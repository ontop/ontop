package inf.unibz.it.obda.codec;

import inf.unibz.it.obda.model.OBDAModel;
import inf.unibz.it.obda.model.Query;
import inf.unibz.it.obda.model.impl.RDBMSSQLQuery;

/*
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

	public SourceQueryToTextCodec(OBDAModel apic) {
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
