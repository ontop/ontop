package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import java.util.Arrays;

public enum DistanceUnit {
    METRE("metre", UOM.METRE),
    DEGREE("degree", UOM.DEGREE),
    RADIAN("radian", UOM.RADIAN);

    public IRI getIri() {
        return iri;
    }

    public String getName() {
        return name;
    }

    private final IRI iri;
    private final String name;

    DistanceUnit(String name, IRI iri) {
        this.iri = iri;
        this.name = name;
    }

    static DistanceUnit findByName(String name) {
        return Arrays.stream(DistanceUnit.values())
                .filter(u -> u.name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("unknown unit name: " + name));
    }

    static DistanceUnit findByIRI(String iri) {
        return Arrays.stream(DistanceUnit.values())
                .filter(u -> u.iri.getIRIString().equals(iri))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("unknown unit iri: " + iri));
    }
}
