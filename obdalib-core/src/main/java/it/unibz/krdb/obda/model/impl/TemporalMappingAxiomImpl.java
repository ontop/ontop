package it.unibz.krdb.obda.model.impl;


import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;

import java.util.ArrayList;
import java.util.List;

public class TemporalMappingAxiomImpl extends RDBMSMappingAxiomImpl implements OBDAMappingAxiom {

    private String Tfrom;
    private String Tto;

    protected TemporalMappingAxiomImpl(String id, OBDASQLQuery sourceQuery, List<Function> targetQuery, String tfrom, String tto) {
        super(id, sourceQuery, targetQuery);
        this.setTfrom(tfrom);
        this.setTto(tto);
    }

    @Override
    public OBDAMappingAxiom clone() {
        List<Function> newbody = new ArrayList<>(this.getTargetQuery().size());
        for (Function f : this.getTargetQuery())
            newbody.add((Function)f.clone());

        OBDAMappingAxiom clone = new TemporalMappingAxiomImpl(this.getId(), this.getSourceQuery().clone(), newbody,this.Tfrom, this.Tto);
        return clone;
    }

    public String getTfrom() {
        return Tfrom;
    }

    public void setTfrom(String tfrom) {
        Tfrom = tfrom;
    }

    public String getTto() {
        return Tto;
    }

    public void setTto(String tto) {
        Tto = tto;
    }
}
