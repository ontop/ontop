package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;

import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be used to create a target query from a String respectively
 * to create the String representation of a target query.
 * 
 * @author obda
 * 
 */
public class TargetQeryToTextCodec extends ObjectToTextCodec<OBDAQuery> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -369873315771847935L;

	private final DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The constructor. Create a new instance of the TargetQeryToTextCodec
	 * 
	 * @param apic
	 *            the current api controller
	 */
	public TargetQeryToTextCodec(OBDAModel apic) {
		super(apic);
	}

	/**
	 * Transforms the given input into a target query, if the String is not a
	 * valid target query representation null is returned.
	 */
	@Override
	public OBDAQuery decode(String input) {
		return parse(input);
	}

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		} catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(apic.getPrefixManager());

		String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) // if no head
			query = queryHelper.getDefaultHead() + " " + OBDALibConstants.DATALOG_IMPLY_SYMBOL + " " + input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}

	/**
	 * Create the String representation of the given target query.
	 */
	@Override
	public String encode(OBDAQuery input) {
		PrefixManager man = apic.getPrefixManager();

		StringBuffer sb = new StringBuffer();
		if (input instanceof CQIE) {
			List<Atom> list = ((CQIE) input).getBody();
			Iterator<Atom> it = list.iterator();
			boolean atomComma = false;
			while (it.hasNext()) {
				if (atomComma == true) {
					sb.append(", ");
				}
				
				Atom at = (Atom) it.next();
				String name = man.getShortForm(at.getPredicate().toString());
				sb.append(name);
				sb.append("(");
				List<Term> t_list = at.getTerms();
				Iterator<Term> tit = t_list.iterator();
				StringBuffer term_sb = new StringBuffer();
				boolean comma = false;
				while (tit.hasNext()) {
					Term qt = tit.next();
					if (comma == true) {
						term_sb.append(",");
					}
					term_sb.append(render(qt));
					comma = true;

				}
				sb.append(term_sb);
				sb.append(")");
				atomComma = true;
			}
			
		}

		return sb.toString();
	}

	private String render(Term term) {
		PrefixManager man = apic.getPrefixManager();

		StringBuffer term_sb = new StringBuffer();
		if (term instanceof FunctionalTermImpl) {
			FunctionalTermImpl ft = (FunctionalTermImpl) term;
			String fname = man.getShortForm(ft.getFunctionSymbol().toString());
			term_sb.append(fname);
			term_sb.append("(");
			List<Term> t_list2 = ft.getTerms();
			Iterator<Term> tit2 = t_list2.iterator();
			boolean comma = false;
			while (tit2.hasNext()) {
				if (comma == true) {
					term_sb.append(",");
				}

				Term qt2 = tit2.next();
				term_sb.append(render(qt2));
				comma = true;
			}
			term_sb.append(")");
		} else if (term instanceof Variable) {
			term_sb.append("$");
			term_sb.append(term.toString());
		} else if (term instanceof ValueConstant) {
			term_sb.append("\"");
			term_sb.append(man.getShortForm(term.toString(),true,true));
			term_sb.append("\"");
		}
		return term_sb.toString();
	}

}
