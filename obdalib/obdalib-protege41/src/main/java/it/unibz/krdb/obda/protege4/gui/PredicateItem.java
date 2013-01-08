package it.unibz.krdb.obda.protege4.gui;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Predicate;

public class PredicateItem {
	
	private Predicate predicate;
	private PrefixManager prefixManager;
	
	public PredicateItem(Predicate target) {
		this(target, null);
	}
	
	public PredicateItem(Predicate target, PrefixManager pm) {
		predicate = target;
		prefixManager = pm;
	}
	
	/**
	 * Obtains the short name from the full predicate URI. The method will omit the
	 * default prefix ":".
	 */
	public String getQualifiedName() {
		if (prefixManager != null) {
			String shortName = prefixManager.getShortForm(getFullName());
			if (shortName.startsWith(":")) {
				return shortName.substring(1, shortName.length());
			} else {
				return shortName;
			}
		} else {
			return getFullName();
		}
	}
	
	public Predicate getSource() {
		return predicate;
	}
	
	public String getFullName() {
		return predicate.getName().toString();
	}
	
	public boolean isClassPredicate() {
		return predicate.isClass();
	}
	
	public boolean isDataPropertyPredicate() {
		return predicate.isDataProperty();
	}
	
	public boolean isObjectPropertyPredicate() {
		return predicate.isObjectProperty();
	}
	
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}
	
	@Override
	public String toString() {
		return getQualifiedName();
	}
}