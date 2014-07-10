/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra;

public class ProjectionElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String sourceName;

	private String targetName;

	private boolean aggregateOperatorInExpression;
	
	private ExtensionElem sourceExpression;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public ProjectionElem() {
	}

	public ProjectionElem(String name) {
		this(name, name);
	}

	public ProjectionElem(String sourceName, String targetName) {
		setSourceName(sourceName);
		setTargetName(targetName);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		assert sourceName != null : "sourceName must not be null";
		this.sourceName = sourceName;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		assert targetName != null : "targetName must not be null";
		this.targetName = targetName;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(32);
		sb.append(super.getSignature());

		sb.append(" \"");
		sb.append(sourceName);
		sb.append("\"");

		if (!sourceName.equals(targetName)) {
			sb.append(" AS \"").append(targetName).append("\"");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ProjectionElem) {
			ProjectionElem o = (ProjectionElem)other;
			return sourceName.equals(o.getSourceName()) && targetName.equals(o.getTargetName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		// Note: don't xor source and target since they will often be equal
		return targetName.hashCode();
	}

	@Override
	public ProjectionElem clone() {
		return (ProjectionElem)super.clone();
	}

	/**
	 * @return Returns the aggregateOperatorInExpression.
	 */
	public boolean hasAggregateOperatorInExpression() {
		return aggregateOperatorInExpression;
	}

	/**
	 * @param aggregateOperatorInExpression The aggregateOperatorInExpression to set.
	 */
	public void setAggregateOperatorInExpression(boolean aggregateOperatorInExpression) {
		this.aggregateOperatorInExpression = aggregateOperatorInExpression;
	}

	/**
	 * @return Returns the sourceExpression.
	 */
	public ExtensionElem getSourceExpression() {
		return sourceExpression;
	}

	/**
	 * @param sourceExpression The sourceExpression to set.
	 */
	public void setSourceExpression(ExtensionElem sourceExpression) {
		this.sourceExpression = sourceExpression;
	}
	
	
}
