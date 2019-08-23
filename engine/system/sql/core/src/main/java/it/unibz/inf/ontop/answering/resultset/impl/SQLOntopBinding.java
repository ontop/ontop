package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.model.term.Constant;

public class SQLOntopBinding implements OntopBinding {

    private final String name;
    private final JDBC2ConstantConverter constantRetriever;
    private final MainTypeLangValues cell;

    public SQLOntopBinding(String name, MainTypeLangValues cell, JDBC2ConstantConverter constantRetriever){
        this.name = name;
        this.cell = cell;
        this.constantRetriever = constantRetriever;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Constant getValue() throws OntopResultConversionException {
        return constantRetriever.getConstantFromJDBC(cell);
    }

    @Override
    public String toString() {
        try {
            return getName() + "=" + getValue();
        } catch (OntopResultConversionException e) {
            return getName() + "=";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLOntopBinding that = (SQLOntopBinding) o;

        if (!getName().equals(that.getName())) return false;
        if (!constantRetriever.equals(that.constantRetriever)) return false;
        return cell.equals(that.cell);
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + cell.hashCode();
        return result;
    }
}
