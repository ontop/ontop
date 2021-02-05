package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

/**
 * Implementation for BNodes.
 */
public class BNodeConstantImpl extends AbstractNonNullConstant implements BNode {

	private final String name;
	private final ObjectRDFType type;

	/**
	 * The default constructor.
	 */
	protected BNodeConstantImpl(String name, TypeFactory typeFactory) {
		this.name = name;
		this.type = typeFactory.getBlankNodeType();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof BNodeConstantImpl &&
				this.name.equals(((BNodeConstantImpl) other).name));
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String getInternalLabel() {
		return name;
	}

	@Override
	public String getAnonymizedLabel(byte[] salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(salt);
			md.update(name.getBytes());
			return Hex.encodeHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new MinorOntopInternalBugException(e.getMessage());
		}
	}

	@Override
	public String getValue() {
		return name;
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Stream<Variable> getVariableStream() {
		return Stream.of();
	}

	@Override
	public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
		if (otherTerm instanceof Constant) {
			if (((Constant) otherTerm).isNull())
				return IncrementalEvaluation.declareIsNull();
			return equals(otherTerm)
					? IncrementalEvaluation.declareIsTrue()
					: IncrementalEvaluation.declareIsFalse();
		}
		else
			return otherTerm.evaluateStrictEq(this, variableNullability);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public ObjectRDFType getType() {
		return type;
	}

}
