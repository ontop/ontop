package it.unibz.krdb.obda.ontology;

public enum LanguageProfile {
	RDFS(1), OWL2QL(2), DLLITEA(3);

	private final int	order;

	LanguageProfile(int order) {
		this.order = order;
	}

	public int order() {
		return this.order;
	}
}
