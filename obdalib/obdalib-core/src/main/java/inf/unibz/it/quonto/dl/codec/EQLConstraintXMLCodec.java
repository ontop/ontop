package inf.unibz.it.quonto.dl.codec;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.quonto.dl.assertion.EQLConstraint;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;

public class EQLConstraintXMLCodec extends AssertionXMLCodec<EQLConstraint> {

	private static final String	TAG	= "eqlConstraint";

	public EQLConstraintXMLCodec() {
		super();
	}

	@Override
	public Element encode(EQLConstraint input) {
		Element element = createElement(TAG);
		element.setAttribute("constraint", input.getConstraint());
		element.setAttribute("annotation", input.getAnnotation());
		return element;
	}

	@Override
	public EQLConstraint decode(Element input) {
		String constraint = input.getAttribute("constraint");
		String annotation = input.getAttribute("annotation");
		EQLConstraint dc = new EQLConstraint(constraint, annotation);
		return dc;
	}

	public String getElementTag() {
		return TAG;
	}

	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("constraint");
		attributes.add("annotation");
		return attributes;
	}

}
