package inf.unibz.it.ucq.renderer;

import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.UnionOfConjunctiveQueries;
import inf.unibz.it.utils.codec.ObjectToTextCodec;

import java.util.Iterator;
import java.util.List;

public class CQDatalogStringRenderer extends ObjectToTextCodec<UnionOfConjunctiveQueries> {

	@Override
	public UnionOfConjunctiveQueries decode(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encode(UnionOfConjunctiveQueries input) {
		
		StringBuffer str = new StringBuffer();
		
		Iterator<ConjunctiveQuery> it = input.getQueries().iterator();
		while(it.hasNext()){
			ConjunctiveQuery cq = it.next();
			String head = renderCQHead(cq.getHeadTerms());
			String body = renderCQBody(cq.getAtoms());
			str.append(head);
			str.append(" :- ");
			str.append(body);
			str.append(".\n");
		}
		
		return str.toString();
	}

	private String renderCQHead(List<QueryTerm> head){
		
		StringBuffer str = new StringBuffer();
		str.append("q(");
		StringBuffer str2 = new StringBuffer();
		Iterator<QueryTerm> it = head.iterator();
		while(it.hasNext()){
			if(str2.length()>0){
				str2.append(",");
			}
			str2.append(it.next().getName());
		}
		str.append(str2.toString());
		str.append(")");
		return str.toString();
	}
	
	private String renderCQBody(List<QueryAtom> body){
		
		StringBuffer str = new StringBuffer();
		Iterator<QueryAtom> it = body.iterator();
		while(it.hasNext()){
			if(str.length() >0){
				str.append(",");
			}
			str.append(renderQueryAtom(it.next()));
		}
		return str.toString();
	}
	
	private String renderQueryAtom(QueryAtom atom){
		
		StringBuffer str = new StringBuffer();
		str.append(atom.getName());
		str.append("(");
		StringBuffer str2 = new StringBuffer();
		Iterator<QueryTerm> it =atom.getTerms().iterator();
		while(it.hasNext()){
			QueryTerm t = it.next();
			if(str2.length()>0){
				str2.append(",");
			}
			if(t instanceof ConstantTerm){
				str2.append("'");
				str2.append(t.getName());
				str2.append("'");
			}else{
				str2.append(t.getName());
			}
		}
		str.append(str2.toString());
		str.append(")");
		return str.toString();
	}
	
	public String encodeAtom(QueryAtom atom){
		return renderQueryAtom(atom);
	}
}
