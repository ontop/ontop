package inf.unibz.it.obda.dl.codec.constraints.xml;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public class RDBMSForeignKeyConstraintXMLCodec extends AssertionXMLCodec<RDBMSForeignKeyConstraint>{

	public RDBMSForeignKeyConstraintXMLCodec() {
		super();
	}

	private static final String	TAG	= "RDBMSForeignKeyConstraint";
	private static final String	V1	= "variable";
	private static final String	MAPPING	= "mapping";
	
	@Override
	public RDBMSForeignKeyConstraint decode(Element input) {
		NodeList nl = input.getElementsByTagName(MAPPING);
		if(nl.getLength() == 2){
			Element map1 = (Element) nl.item(0);
			Element map2 = (Element) nl.item(1);
			String id1 = map1.getAttribute("id");
			String id2 = map2.getAttribute("id");
			NodeList l1 = map1.getElementsByTagName(V1);
			NodeList l2 = map2.getElementsByTagName(V1);
			Vector<QueryTerm> v1 = new Vector<QueryTerm>();
			for(int i=0;i<l1.getLength();i++){
				Element e = (Element) l1.item(i);
				String name = e.getAttribute("name");
				v1.add(new VariableTerm(name));
			}
			Vector<QueryTerm> v2 = new Vector<QueryTerm>();
			for(int i=0;i<l2.getLength();i++){
				Element e = (Element) l2.item(i);
				String name = e.getAttribute("name");
				v2.add(new VariableTerm(name));
			}
			
			try {
				return ConstraintsRenderer.getInstance().createRDBMSForeignKeyConstraint(id1, id2, v1, v2);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}else{
			return null;
		}
	}

	@Override
	public Element encode(RDBMSForeignKeyConstraint input) {
		
		Element element = createElement(TAG);
		Element map1 = createElement(MAPPING);
		Element map2 = createElement(MAPPING);
		map1.setAttribute("id", input.getIDForMappingOne());
		map2.setAttribute("id", input.getIDForMappingTwo());
		
		List<QueryTerm> l1 =input.getTermsOfQueryOne();
		Iterator<QueryTerm> it1 = l1.iterator();
		while(it1.hasNext()){
			QueryTerm t = it1.next();
			Element e = createElement(V1);
			e.setAttribute("name", t.getVariableName());
			map1.appendChild(e);
		}
		List<QueryTerm> l2 =input.getTermsOfQueryTwo();
		Iterator<QueryTerm> it2 = l2.iterator();
		while(it2.hasNext()){
			QueryTerm t = it2.next();
			Element e = createElement(V1);
			e.setAttribute("name",t.getVariableName());
			map2.appendChild(e);
		}
		element.appendChild(map1);
		element.appendChild(map2);
		return element;
		
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		return attributes;
	}

	@Override
	public String getElementTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

}
