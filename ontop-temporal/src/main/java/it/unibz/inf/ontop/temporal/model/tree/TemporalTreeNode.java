package it.unibz.inf.ontop.temporal.model.tree;

import it.unibz.inf.ontop.temporal.model.TemporalExpression;

public class TreeNode {
        private TemporalExpression temporalExpression;
        private TreeNode leftNode;
        private TreeNode rightNode;

        public TreeNode(TemporalExpression _temporalExpression){
            temporalExpression = _temporalExpression;
        }

        public TemporalExpression getTemporalExpression() {
            return temporalExpression;
        }

        public void setTemporalExpression(TemporalExpression temporalExpression) {
            this.temporalExpression = temporalExpression;
        }

        public TreeNode getLeftNode() {
            return leftNode;
        }

        public void setLeftNode(TreeNode leftNode) {
            this.leftNode = leftNode;
        }

        public TreeNode getRightNode() {
            return rightNode;
        }

        public void setRightNode(TreeNode rightNode) {
            this.rightNode = rightNode;
        }
}