package it.unibz.inf.ontop.answering.reformulation.input.translation.impl;

import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparqlAlgebraTreePruner {
    private Map<String, IntervalVariables> timeIntvVariableMap;
    private List <String> variablesRemovedFromProjection;

    public SparqlAlgebraTreePruner(){
        timeIntvVariableMap = new HashMap();
        variablesRemovedFromProjection = new ArrayList<>();
    }

    public Map<String, IntervalVariables> getTimeIntvVariableMap() {
        return timeIntvVariableMap;
    }

    public List<String> getVariablesRemovedFromProjection() {
        return variablesRemovedFromProjection;
    }

    TupleExpr updateProjectionElemList(TupleExpr node) {
        if (node instanceof Projection) {
            List<ProjectionElem> newProjectionList = new ArrayList<>();
            for (ProjectionElem elem : ((Projection) node).getProjectionElemList().getElements()) {
                if(timeIntvVariableMap.values().stream()
                        .allMatch(iv -> !iv.getBegin().equals(elem.getSourceName()) && !iv.getEnd().equals(elem.getSourceName()))){
                    newProjectionList.add(elem);
                } else {
                    variablesRemovedFromProjection.add(elem.getSourceName());
                }
            }
            ((Projection) node).setProjectionElemList(new ProjectionElemList(newProjectionList));
        }
        return node;
    }

    private TupleExpr getRootNode(TupleExpr te){
        while(te.getParentNode() != null)
            te = (TupleExpr) te.getParentNode();
        return te;
    }

    TupleExpr pruneTimeRelatedSubTrees(TupleExpr node) throws OntopUnsupportedInputQueryException {
        TupleExpr prevNode;
        do{
            prevNode = node.clone();
        } while(!prevNode.equals(pruneSubTrees(node)));
        return node;
    }


    private TupleExpr pruneSubTrees(TupleExpr node) throws OntopUnsupportedInputQueryException {

        if (node instanceof Slice) {   // SLICE algebra operation
            return pruneSubTrees(((Slice) node).getArg());
        }
        else if (node instanceof Distinct) { // DISTINCT algebra operation
            return pruneSubTrees(((Distinct) node).getArg());
        }
        else if (node instanceof Reduced) {  // REDUCED algebra operation
            return pruneSubTrees(((Reduced) node).getArg());
        }
        else if (node instanceof Order) {   // ORDER algebra operation
            return pruneSubTrees(((Order) node).getArg());
        }
        else if (node instanceof StatementPattern) { // triple pattern
            return getRootNode(node);
        }
        else if (node instanceof SingletonSet) {
            return getRootNode(node);
        }
        else if (node instanceof Join) {     // JOIN algebra operation

            TupleExpr newTe = getSubTree(node);
            if (newTe == null){
                if (node.getParentNode() != null){
                    if (node.getParentNode() instanceof Join) {
                        if (node.getParentNode().getParentNode() != null) {
                            QueryModelNode grandparent = node.getParentNode().getParentNode();
                            grandparent.replaceChildNode(node.getParentNode(), ((Join)node.getParentNode()).getRightArg());
                            return getRootNode((TupleExpr) grandparent);
                        } else {
                            return ((Join)node.getParentNode()).getRightArg();
                        }
                    }
                } else throw new OntopUnsupportedInputQueryException("Invalid temporal query tree: " + node);

            } else if (newTe.equals(node)){
                return pruneSubTrees(((Join) node).getLeftArg());

            } else {
                QueryModelNode parent = node.getParentNode();
                parent.replaceChildNode(node, newTe);
                return getRootNode((TupleExpr) parent);
            }
        }
        else if (node instanceof LeftJoin) {  // OPTIONAL algebra operation
            pruneSubTrees(((LeftJoin) node).getLeftArg());
            pruneSubTrees(((LeftJoin) node).getRightArg());
        }
        else if (node instanceof Union) {   // UNION algebra operation
            pruneSubTrees(((Union) node).getLeftArg());
            pruneSubTrees(((Union) node).getRightArg());
        }
        else if (node instanceof Filter) {   // FILTER algebra operation
            return pruneSubTrees(((Filter) node).getArg());
        }
        else if (node instanceof Projection) {  // PROJECT algebra operation
            return pruneSubTrees(((Projection) node).getArg());
        }
        else if (node instanceof Extension) {     // EXTEND algebra operation
            return pruneSubTrees(((Extension) node).getArg());
        }
        else if (node instanceof BindingSetAssignment) { // VALUES in SPARQL
        }
        else if (node instanceof Group) {
            throw new OntopUnsupportedInputQueryException("GROUP BY is not supported yet");
        }
        throw new OntopUnsupportedInputQueryException("Not supported: " + node);
    }

    private TupleExpr getSubTree(TupleExpr rootJoinNode){
        TupleExpr left = ((Join)rootJoinNode).getRightArg();
        TupleExpr right;
        String begin = null;
        String end = null;
        String graphVar = null;
        if(left instanceof StatementPattern){
            if(isInXSDTimeLike((StatementPattern) left)){
                right = ((Join)rootJoinNode).getLeftArg();
                end = ((StatementPattern) left).getObjectVar().getName();
                if (right instanceof Join){
                    left = ((Join) right).getRightArg();
                    if(left instanceof StatementPattern){
                        if(isInstant((StatementPattern) left)){
                            right = ((Join)right).getLeftArg();
                            if (right instanceof Join){
                                left = ((Join) right).getRightArg();
                                if(left instanceof StatementPattern){
                                    if(isHasEnd((StatementPattern)left)){
                                        right = ((Join)right).getLeftArg();
                                        if (right instanceof Join){
                                            left = ((Join) right).getRightArg();
                                            if(left instanceof StatementPattern){
                                                if(isInXSDTimeLike((StatementPattern)left)){
                                                    right = ((Join)right).getLeftArg();
                                                    begin = ((StatementPattern) left).getObjectVar().getName();
                                                    if (right instanceof Join){
                                                        left = ((Join) right).getRightArg();
                                                        if(left instanceof StatementPattern){
                                                            if(isInstant((StatementPattern)left)){
                                                                right = ((Join)right).getLeftArg();
                                                                if (right instanceof Join){
                                                                    left = ((Join) right).getRightArg();
                                                                    if(left instanceof StatementPattern){
                                                                        if(isHasBeginning((StatementPattern)left)){
                                                                            right = ((Join)right).getLeftArg();
                                                                            if (right instanceof StatementPattern){
                                                                                if(isHasTime((StatementPattern)right)){
                                                                                    graphVar = ((StatementPattern) right).getSubjectVar().getName();
                                                                                    timeIntvVariableMap.put(graphVar, new IntervalVariables(begin, end));
                                                                                    return null;
                                                                                }
                                                                            } else if (right instanceof Join){
                                                                                left = ((Join) right).getRightArg();
                                                                                if(left instanceof StatementPattern){
                                                                                    if(isHasTime((StatementPattern)left)){
                                                                                        graphVar = ((StatementPattern) left).getSubjectVar().getName();
                                                                                        timeIntvVariableMap.put(graphVar, new IntervalVariables(begin, end));
                                                                                        return ((Join) right).getLeftArg();
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return rootJoinNode;
    }

    private Boolean isInXSDTimeLike(StatementPattern statementPattern){
        if(statementPattern.getSubjectVar().isAnonymous() && !statementPattern.getObjectVar().isAnonymous()){
            Value value = statementPattern.getPredicateVar().getValue();
            if (value.stringValue().equals("http://www.w3.org/2006/time#inXSDDateTimeStamp") ||
                    value.stringValue().equals("http://www.w3.org/2006/time#inXSDDateTime") ||
                    value.stringValue().equals("http://www.w3.org/2006/time#inXSDDate") ||
                    value.stringValue().equals("http://www.w3.org/2006/time#inXSDgYear") ||
                    value.stringValue().equals("http://www.w3.org/2006/time#inXSDgYearMonth")){
                return true;
            }
        }
        return false;
    }

    private Boolean isInstant(StatementPattern statementPattern){
        if (statementPattern.getSubjectVar().isAnonymous() &&
                statementPattern.getPredicateVar().getValue().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
                statementPattern.getObjectVar().getValue().stringValue().equals("http://www.w3.org/2006/time#Instant")){
            return true;
        }
        return false;
    }

    private Boolean isHasEnd(StatementPattern statementPattern){
        if (statementPattern.getSubjectVar().isAnonymous() &&
                statementPattern.getPredicateVar().getValue().stringValue().equals("http://www.w3.org/2006/time#hasEnd") &&
                statementPattern.getObjectVar().isAnonymous()){
            return true;
        }
        return false;
    }

    private Boolean isHasBeginning(StatementPattern statementPattern){
        if (statementPattern.getSubjectVar().isAnonymous() &&
                statementPattern.getPredicateVar().getValue().stringValue().equals("http://www.w3.org/2006/time#hasBeginning") &&
                statementPattern.getObjectVar().isAnonymous()){
            return true;
        }
        return false;
    }

    private Boolean isHasTime(StatementPattern statementPattern){
        if (!statementPattern.getSubjectVar().isAnonymous() &&
                statementPattern.getPredicateVar().getValue().stringValue().equals("http://www.w3.org/2006/time#hasTime") &&
                statementPattern.getObjectVar().isAnonymous()){
            return true;
        }
        return false;
    }

    public class IntervalVariables{
        private String begin;
        private String end;

        private IntervalVariables(String begin, String end){
            this.begin = begin;
            this.end = end;
        }

        String getBegin(){
            return this.begin;
        }

        String getEnd(){
            return this.end;
        }

    }
}


