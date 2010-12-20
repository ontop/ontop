package inf.unibz.it.obda.dl.codec.dependencies.dig11;

import inf.unibz.it.dl.codec.dig11.AssertionDIG11Codec;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.parser.DependencyAssertionRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RDBMSDisjointnessDependencyDIG11Codec extends
AssertionDIG11Codec<RDBMSDisjointnessDependency> {

	private final TermFactoryImpl termFactory = (TermFactoryImpl) TermFactory.getInstance();

	public RDBMSDisjointnessDependencyDIG11Codec() {
		super();
	}

	private static final String	TAG	= "RDBMSDisjointnessDependency";
	private static final String	PARAMETER1	= "parameter1";
	private static final String	PARAMETER2	= "parameter2";
	private static final String	VARIABLE	= "variable";

	@Override
	public RDBMSDisjointnessDependency decode(Element input) {
		NodeList nl1 = input.getElementsByTagName(PARAMETER1);
		if(nl1.getLength() < 1){
			return null;
		}
		Element p1 = (Element) nl1.item(0);
		String id1 = p1.getAttribute("mappingID");
		NodeList l1 =p1.getElementsByTagName("variable");
		Vector<Term> aux1 = new Vector<Term>();
		for(int i=0; i< l1.getLength();i++){
			Element el = (Element)l1.item(i);
			String name = el.getAttribute("name");
			aux1.add(termFactory.createVariable(name));
		}

		NodeList nl2 = input.getElementsByTagName(PARAMETER2);
		if(nl2.getLength() <1){
			return null;
		}
		Element p2 = (Element) nl2.item(0);
		String id2 = p2.getAttribute("mappingID");
		NodeList l2 =p2.getElementsByTagName("variable");
		Vector<Term> aux2 = new Vector<Term>();
		for(int j=0; j< l2.getLength();j++){
			Element el = (Element)l2.item(j);
			String name = el.getAttribute("name");
			aux2.add(termFactory.createVariable(name));
		}

		return DependencyAssertionRenderer.getInstance().createAndValidateRDBMSDisjointnessDependency(id1, id2, aux1, aux2);
	}

	@Override
	public Element encode(RDBMSDisjointnessDependency input) {
		Element element = createElement(TAG);
		Element parameter1 = createElement(PARAMETER1);
		parameter1.setAttribute("mappingID", input.getMappingOneId());
		List<Term> one = input.getTermsOfQueryOne();
		Iterator<Term> it = one.iterator();
		while(it.hasNext()){
			Element variable = createElement(VARIABLE);
			variable.setAttribute("name", it.next().getName());
			parameter1.appendChild(variable);
		}
		element.appendChild(parameter1);

		Element parameter2 = createElement(PARAMETER2);
		parameter2.setAttribute("mappingID", input.getMappingTwoId());
		List<Term> two = input.getTermsOfQueryTwo();
		Iterator<Term> it2 = two.iterator();
		while(it2.hasNext()){
			Element variable = createElement(VARIABLE);
			variable.setAttribute("name", it2.next().getName());
			parameter2.appendChild(variable);
		}
		element.appendChild(parameter2);
		return element;
	}

	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		return attributes;
	}

	public String getElementTag() {
		return TAG;
	}

}
