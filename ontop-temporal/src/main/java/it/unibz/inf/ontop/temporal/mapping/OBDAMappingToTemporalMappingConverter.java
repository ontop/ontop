//package it.unibz.inf.ontop.temporal.mapping;
//
//import it.unibz.inf.ontop.model.OBDAMappingAxiom;
//import it.unibz.inf.ontop.sql.RDBMSMappingAxiom;
//
//import java.util.Collection;
//
//import static it.unibz.inf.ontop.utils.ImmutableCollectors.toList;
//
//public class OBDAMappingToTemporalMappingConverter {
//
//    public TemporalMappingAxiom convert(RDBMSMappingAxiom rdbmsMappingAxiom){
//        // TODO:
//
//        // quad(s, p, o, g), quad(g, :interval, interval, _) -> p(s,o) @ {interval}
//
//        return null;
//    }
//
//    public Collection<TemporalMappingAxiom> convert(Collection<RDBMSMappingAxiom> rdbmsMappingAxiom){
//        return rdbmsMappingAxiom.stream().map(this::convert).collect(toList());
//    }
//
//}
