package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;

import java.util.Enumeration;

public abstract class MappingValidator {

	protected OBDAQuery sourceQuery = null;
	protected OBDAQuery targetQuery = null;
	protected OBDAModel apic = null;

	public MappingValidator (OBDAModel apic, OBDAQuery sq, OBDAQuery tg){
		this.apic = apic;
		sourceQuery = sq;
		targetQuery = tg;
	}

	/**
	 * Returns the set of errors found while validating this mapping if any.
	 *
	 * TODO fix api
	 * @return
	 */
	public abstract Enumeration<String> validate();
}
