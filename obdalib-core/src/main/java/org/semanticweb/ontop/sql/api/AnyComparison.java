package org.semanticweb.ontop.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Auxiliary Class used to visualize AnyComparison in string format.
 * Any and Some are the same in SQL so we consider always the case of ANY
 *
 */

public class AnyComparison extends AnyComparisonExpression{

	public AnyComparison(SubSelect subSelect) {
		super(subSelect);
	}
	
	@Override
	public String toString(){
		return "ANY "+getSubSelect();
	}

}
