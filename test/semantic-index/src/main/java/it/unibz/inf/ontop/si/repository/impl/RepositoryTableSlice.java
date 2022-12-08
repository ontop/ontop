package it.unibz.inf.ontop.si.repository.impl;

import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import javax.annotation.Nullable;
import java.util.*;

public class RepositoryTableSlice {

	static final class Identifier {
		private final ObjectRDFType type1;
		@Nullable
		private final RDFTermType type2;

		public Identifier(ObjectRDFType type1, RDFTermType type2) {
			this.type1 = type1;
			this.type2 = type2;
		}

		public Identifier(ObjectRDFType type1) {
			this.type1 = type1;
			this.type2 = null;
		}

		public ObjectRDFType getType1() {
			return type1;
		}

		public RDFTermType getType2() {
			return type2;
		}

		@Override
		public String toString() {
			return " T1: " + type1 + (type2 != null ? ", T2: " + type2 : "");
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Identifier) {
				Identifier other = (Identifier) obj;
				return Objects.equals(this.type1, other.type1) && Objects.equals(this.type2, other.type2);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(type1, type2);
		}
	}

	private final Identifier id;
	private final String selectCommand;
	private final String insertCommand;
	private final Set<Integer> indexes = new HashSet<>();
	
	public RepositoryTableSlice(ObjectRDFType type1, RDFTermType type2, String selectCommand, String insertCommand) {
		this.id = new Identifier(type1, type2);
		this.selectCommand = selectCommand;
		this.insertCommand = insertCommand;
	}

	public RepositoryTableSlice(ObjectRDFType type1, String selectCommand, String insertCommand) {
		this.id = new Identifier(type1);
		this.selectCommand = selectCommand;
		this.insertCommand = insertCommand;
	}

	public Identifier getId() {
		return id;
	}
	
	public String getSELECT(String filter) {
		return selectCommand + filter;
	}
	
	public String getINSERT() {
		return insertCommand;
	}
	
	public boolean isEmptyForIntervals(List<Interval> intervals) {
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++)
				if (indexes.contains(i)) 
					return false;
		return true;
	}

	public void addIndex(int idx) {
		indexes.add(idx);			
	}
}
