package inf.unibz.it.quonto.dl.codec.dig11;

import inf.unibz.it.dl.codec.dig11.AssertionDIG11Codec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;

public class DenialConstraintDIG11Codec extends AssertionDIG11Codec<DenialConstraint> {

	private static final String	ATTRIBUTE_CONSTRAINT	= "constraint";
	private static final String	TAG	= "denialConstraint";

	public DenialConstraintDIG11Codec() {
		super();
	}
	
	@Override
	public Element encode(DenialConstraint input) {
		Element element = createElement(TAG);
		element.setAttribute(ATTRIBUTE_CONSTRAINT, input.getConstraint());
		return element;
	}

	@Override
	public DenialConstraint decode(Element input) {
		String constraint = input.getAttribute(ATTRIBUTE_CONSTRAINT);
		DenialConstraint dc = new DenialConstraint(constraint, "");
		return dc;
	}

	public String getElementTag() {
		return TAG;
	}

	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add(ATTRIBUTE_CONSTRAINT);
		return attributes;
	}

}
