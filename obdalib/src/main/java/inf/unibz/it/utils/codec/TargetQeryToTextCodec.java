package inf.unibz.it.utils.codec;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.query.Query;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

/**
 * This class should be used to create a target query from a String respectively 
 * to create the String representation of a target query.
 * 
 * @author obda
 *
 */
public class TargetQeryToTextCodec extends ObjectToTextCodec<TargetQuery> {

	/**
	 * The constructor. Create a new instance of the TargetQeryToTextCodec
	 * @param apic the current api controller
	 */
	public TargetQeryToTextCodec(APIController apic) {
		super(apic);
	}

	/**
	 * Transforms the given input into a target query, if the String is not a valid
	 * target query representation null is returned.
	 */
	@Override
	public TargetQuery decode(String input) {
		
		try {
			ConjunctiveQuery cq = new ConjunctiveQuery(input, apic);
			return cq;
		} catch (QueryParseException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	/**
	 * Create the String representation of the given target query.
	 */
	@Override
	public String encode(TargetQuery input) {
		
		StringBuffer sb = new StringBuffer();
		if(input instanceof ConjunctiveQuery){
			ArrayList<QueryAtom> list = ((ConjunctiveQuery)input).getAtoms();
			Iterator<QueryAtom> it = list.iterator();
			while(it.hasNext()){
				if(sb.length()>0){
					sb.append(",");
				}
				QueryAtom at = it.next();
				String name = apic.getEntityNameRenderer().getPredicateName(at);
				sb.append(name);
				sb.append("(");
				ArrayList<QueryTerm> t_list =at.getTerms();
				Iterator<QueryTerm> tit = t_list.iterator();
				StringBuffer term_sb = new StringBuffer();
				while(tit.hasNext()){
					QueryTerm qt = tit.next();
					if(term_sb.length()>0){
						term_sb.append(",");
					}
					if(qt instanceof FunctionTerm){
						FunctionTerm ft = (FunctionTerm) qt;
						String fname = apic.getEntityNameRenderer().getFunctionName(ft);
						term_sb.append(fname);
						term_sb.append("(");
						ArrayList<QueryTerm> t_list2 =ft.getParameters();
						Iterator<QueryTerm> tit2 = t_list2.iterator();
						StringBuffer para = new StringBuffer();
						while(tit2.hasNext()){
							if(para.length()>0){
								para.append(",");
							}
							QueryTerm qt2 = tit2.next();
							String n = qt2.getVariableName();
							para.append("$"+n);
						}
						term_sb.append(para);
						term_sb.append(")");
					}else{
						term_sb.append("$");
						term_sb.append(qt.getVariableName());
					}
				}
				sb.append(term_sb);
				sb.append(")");
			}
		}
		
		return sb.toString();
	}

}
