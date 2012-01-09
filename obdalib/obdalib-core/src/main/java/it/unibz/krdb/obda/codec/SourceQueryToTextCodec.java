package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

/*
 * This class should be used to create a target query from a String respectively
 * to create the String representation of a source query.
 *
 * @author obda
 *
 */
public class SourceQueryToTextCodec extends ObjectToTextCodec<OBDAQuery> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4202155625234871648L;
	OBDADataFactory fac= OBDADataFactoryImpl.getInstance();
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
	public OBDAQuery decode(String input) {
		OBDASQLQuery query = fac.getSQLQuery(input);
		return query;
	}

	/**
	 * Create the String representation of the given source query.
	 */
	@Override
	public String encode(OBDAQuery input) {
		if (input == null)
			return "";

		return input.toString();
	}
}
