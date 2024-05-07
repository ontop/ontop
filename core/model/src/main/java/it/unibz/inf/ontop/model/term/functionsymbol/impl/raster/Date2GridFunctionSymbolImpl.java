package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class Date2GridFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public Date2GridFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdStringDatatype, RDFDatatype xsdIntegerDatatype) {
        super("RAS_DATE_TO_GRID", functionIRI, ImmutableList.of(xsdStringDatatype, xsdIntegerDatatype),
                xsdIntegerDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getDate2Grid(subLexicalTerms.get(0), subLexicalTerms.get(1));

    }

}
