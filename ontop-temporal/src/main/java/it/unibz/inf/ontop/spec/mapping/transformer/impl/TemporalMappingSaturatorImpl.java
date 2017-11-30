package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.impl.MappingImpl;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.mapping.OntopNativeTemporalMappingParser;
import it.unibz.inf.ontop.temporal.model.*;

import java.util.List;
import java.util.Stack;

@Singleton
public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator{

    TemporalIntermediateQueryFactory TIQFactory;

    @Inject
    private TemporalMappingSaturatorImpl(TemporalIntermediateQueryFactory TIQFactory) {
        this.TIQFactory = TIQFactory;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {
        return null;
    }

    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, Mapping temporalMapping, DBMetadata temporalDBMetadata, DatalogMTLProgram datalogMTLProgram){

        TreeTraverser treeTraverser = getTreeTraverser();

        List<TargetQueryParser> parsers = OntopNativeTemporalMappingParser.createParsers(datalogMTLProgram.getPrefixes());




        for (DatalogMTLRule rule : datalogMTLProgram.getRules()) {
           Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
           Stack<DatalogMTLExpression> teStack = new Stack<DatalogMTLExpression>();
           Stack<IntermediateQuery> mappingStack = new Stack<IntermediateQuery>();

            //TODO: merge dbMetadata and temporalDBMetadata
            //TemporalIntermediateQueryBuilder TIQBuilder = (TemporalIntermediateQueryBuilder) TIQFactory.createIQBuilder(temporalDBMetadata, temporalMapping.getExecutorRegistry());
            
            TargetAtom ta = DatalogMTLConversionTools.convertFromDatalogDataAtom(rule.getHead());
            //TIQBuilder.init(((TemporalAtomicExpression)rule.getHead()).getPredicate(), TIQFactory.createConstructionNode());
           it.forEach(te-> teStack.push(te));

           while(!teStack.isEmpty()){
               saturateRule(teStack, mappingStack);
           }

//            it.forEach(te -> System.out.println(te.render()));
//            System.out.println("---");
        }

        return null;
    }

    //TODO: move this function into where it will be used
    public TreeTraverser<? extends DatalogMTLExpression> getTreeTraverser(){
        return TreeTraverser.using(DatalogMTLExpression::getChildNodes);
    }

    private void saturateRule(Stack<DatalogMTLExpression> teStack, Stack<IntermediateQuery> mappingStack){
        DatalogMTLExpression currentExpression = teStack.pop();

        if(currentExpression instanceof TemporalAtomicExpression){
           // Mapping m = new MappingImpl();

        }else if(currentExpression instanceof StaticAtomicExpression){

        }
    }
}
