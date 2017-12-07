package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuadrupleDefinition {
    private QuadrupleItem quadruple;
    private QuadrupleItem hasTime;
    private QuadrupleItem isBeginInclusive;
    private QuadrupleItem hasBeginning;
    private QuadrupleItem inXSDTimeBegin;
    private QuadrupleItem isEndInclusive;
    private QuadrupleItem hasEnd;
    private QuadrupleItem inXSDTimeEnd;

    public QuadrupleDefinition(QuadrupleItem quadruple, QuadrupleItem hasTime, QuadrupleItem isBeginInclusive,
                               QuadrupleItem hasBeginning, QuadrupleItem inXSDTimeBegin, QuadrupleItem isEndInclusive,
                               QuadrupleItem hasEnd, QuadrupleItem inXSDTimeEnd) {
        this.quadruple = quadruple;
        this.hasTime = hasTime;
        this.isBeginInclusive = isBeginInclusive;
        this.hasBeginning = hasBeginning;
        this.inXSDTimeBegin = inXSDTimeBegin;
        this.isEndInclusive = isEndInclusive;
        this.hasEnd = hasEnd;
        this.inXSDTimeEnd = inXSDTimeEnd;
    }

    public QuadrupleDefinition() {
    }

    public QuadrupleItem getQuadruple() {
        return quadruple;
    }

    public QuadrupleItem getHasTime() {
        return hasTime;
    }

    public QuadrupleItem getIsBeginInclusive() {
        return isBeginInclusive;
    }

    public QuadrupleItem getHasBeginning() {
        return hasBeginning;
    }

    public QuadrupleItem getInXSDTimeBegin() {
        return inXSDTimeBegin;
    }

    public QuadrupleItem getIsEndInclusive() {
        return isEndInclusive;
    }

    public QuadrupleItem getHasEnd() {
        return hasEnd;
    }

    public QuadrupleItem getInXSDTimeEnd() {
        return inXSDTimeEnd;
    }

    public void setQuadruple(QuadrupleItem quadruple) {
        this.quadruple = quadruple;
    }

    public void setHasTime(QuadrupleItem hasTime) {
        this.hasTime = hasTime;
    }

    public void setIsBeginInclusive(QuadrupleItem isBeginInclusive) {
        this.isBeginInclusive = isBeginInclusive;
    }

    public void setHasBeginning(QuadrupleItem hasBeginning) {
        this.hasBeginning = hasBeginning;
    }

    public void setInXSDTimeBegin(QuadrupleItem inXSDTimeBegin) {
        this.inXSDTimeBegin = inXSDTimeBegin;
    }

    public void setIsEndInclusive(QuadrupleItem isEndInclusive) {
        this.isEndInclusive = isEndInclusive;
    }

    public void setHasEnd(QuadrupleItem hasEnd) {
        this.hasEnd = hasEnd;
    }

    public void setInXSDTimeEnd(QuadrupleItem inXSDTimeEnd) {
        this.inXSDTimeEnd = inXSDTimeEnd;
    }

    public List<IntermediateQuery> getAll(){
        List<IntermediateQuery> list = new ArrayList<>();
        list.add(getQuadruple().getIntermediateQuery());
        list.add(getHasTime().getIntermediateQuery());
        list.add(getIsBeginInclusive().getIntermediateQuery());
        list.add(getHasBeginning().getIntermediateQuery());
        list.add(getInXSDTimeBegin().getIntermediateQuery());
        list.add(getIsEndInclusive().getIntermediateQuery());
        list.add(getHasEnd().getIntermediateQuery());
        list.add(getInXSDTimeEnd().getIntermediateQuery());

        return list.stream().collect(ImmutableCollectors.toList());
    }
}
