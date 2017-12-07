package it.unibz.inf.ontop.spec.mapping;

public enum QuadrupleElements {
    quadruple("quadruple"),
    hasTime("http://www.w3.org/2006/time#hasTime"),
    isBeginInclusive("https://w3id.org/tobda/vocabulary#isBeginInclusive"),
    hasBeginning("http://www.w3.org/2006/time#hasBeginning"),
    inXSDTimeBegin("http://www.w3.org/2006/time#inXSDDateTime"),
    isEndInclusive("https://w3id.org/tobda/vocabulary#isEndInclusive"),
    hasEnd("http://www.w3.org/2006/time#hasEnd"),
    inXSDTimeEnd("http://www.w3.org/2006/time#inXSDDateTime");

    private final String uri;

    QuadrupleElements(String uri){
        this.uri = uri;
    }


    @Override
    public String toString() {
        return uri;
    }
}
