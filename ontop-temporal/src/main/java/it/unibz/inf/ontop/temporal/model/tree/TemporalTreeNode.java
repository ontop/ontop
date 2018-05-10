package it.unibz.inf.ontop.temporal.model.tree;

import com.google.common.collect.TreeTraverser;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemporalTreeNode {
    private DatalogMTLExpression datalogMTLExpression;
    private List<TemporalTreeNode> childNodesList;

    public TemporalTreeNode(DatalogMTLExpression _datalogMTLExpression){
            datalogMTLExpression = _datalogMTLExpression;
        }

    public DatalogMTLExpression getDatalogMTLExpression() {
            return datalogMTLExpression;
        }

    public void setDatalogMTLExpression(DatalogMTLExpression datalogMTLExpression) {
        this.datalogMTLExpression = datalogMTLExpression;
    }

    public void addChildNodes(TemporalTreeNode ... childNodes) {
        if(childNodesList == null) {
            childNodesList = new ArrayList<TemporalTreeNode>();
        }
        childNodesList.addAll(Arrays.asList(childNodes));
    }

    public void addChildNode(TemporalTreeNode childNode) {
        if(childNodesList == null) {
            childNodesList = new ArrayList<TemporalTreeNode>();
        }
        childNodesList.add(childNode);
    }

    public Iterable<TemporalTreeNode> getChildNodes(){
            return childNodesList;
        }

        //TODO: move this function into where it will be used
        public TreeTraverser<TemporalTreeNode> getTreeTraverser(){
            return CustomTreeTraverser.using(TemporalTreeNode::getChildNodes);
            /*return new TreeTraverser<TemporalTreeNode>() {
                @Override
                public Iterable<TemporalTreeNode> children(TemporalTreeNode o) {
                    return () -> Iterators.forArray(o.leftNode, o.rightNode);
                }
            };*/
        }
}