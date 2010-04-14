package inf.unibz.it.quonto.dl.codec;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;

public class DenialConstraintXMLCodec extends AssertionXMLCodec<DenialConstraint> {

	private static final String	TAG	= "denialConstraint";

	public DenialConstraintXMLCodec() {
		super();
	}

	@Override
	public Element encode(DenialConstraint input) {
		Element element = createElement(TAG);
		element.setAttribute("constraint", input.getConstraint());
		element.setAttribute("annotation", input.getAnnotation());
		return element;
	}

	@Override
	public DenialConstraint decode(Element input) {
		String constraint = input.getAttribute("constraint");
		String annotation = input.getAttribute("annotation");
		DenialConstraint dc = new DenialConstraint(constraint, annotation);
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
