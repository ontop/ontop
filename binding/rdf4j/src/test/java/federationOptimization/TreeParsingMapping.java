package federationOptimization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.IRIStringTemplateFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.TemporaryDBTypeConversionToStringFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.RDFTermFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.impl.IRIConstantImpl;
import it.unibz.inf.ontop.model.term.impl.NonGroundFunctionalTermImpl;
import it.unibz.inf.ontop.model.term.impl.RDFTermTypeConstantImpl;
import it.unibz.inf.ontop.model.term.impl.VariableImpl;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.impl.TargetAtomImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@Category(ObdfTest.class)
public class TreeParsingMapping {

    /**
     * This test is used to generate a tree of the mapping atoms
     * It generates a dot graph taken from the graphviz gallery (https://graphviz.org/Gallery/directed/psg.html)
     * @param mapping
     * @return
     * @throws Exception
     */
    public String generateTreeOfMappingAtoms(SQLPPMapping mapping) throws Exception {
        String treeString = "digraph g {\n" +
                "  fontname=\"Helvetica,Arial,sans-serif\"\n" +
                "  node [fontname=\"Helvetica,Arial,sans-serif\"]\n" +
                "  edge [fontname=\"Helvetica,Arial,sans-serif\"]\n" +
                "  graph [fontsize=30 labelloc=\"t\" label=\"\" splines=true overlap=false rankdir = \"TD\"];\n" +
                "  ratio = auto;\n";
        return treeString + getTreeNodes(mapping, "") + "}";
    }

    public String getNodeName(String type, String name){
        return (type + " " + name).trim();
    }

    public String generateNodeAndEdgeString(String currentType, String currentName, String nextType, String nextName, String label, ArrayList<String> nextNodeProperties){
        String nodeString = generateNodeString(nextType, nextName, nextNodeProperties);
        String edgeString = generateEdgeString(currentType, currentName, nextType, nextName, label);
        return nodeString + edgeString;
    }

    public String generateEdgeString(String currentType, String currentName, String nextType, String nextName, String label){
        return "\"" + getNodeName(currentType, currentName) + "\" -> \"" + getNodeName(nextType, nextName) + "\" [label = \"" + label + "\"];\n";
    }

    /** Generate only the last arrow from the String node to the leave node
     * @param currentType
     * @param currentName
     * @param leave
     * @return
     */
    public String generateEdgeToLeaveNodeString(String currentType, String currentName, String leave){
        String leaveNodeName = getNodeName(currentType, currentName) + "String";
        return "\"" + leaveNodeName + "\" [ label = \"\\\"" + escapeGraphVizString(leave) + "\\\"\"];\n"
                + "\"" + getNodeName(currentType, currentName) + "\" -> \"" + leaveNodeName + "\";\n";
    }

    /** Generate a node with the String element and an arrow to the leave node startNode -> String xyz -> leave
     * @param currentType
     * @param currentName
     * @param label
     * @param leave
     * @return
     */
    public String generateEdgeToLeaveNodeString(String currentType, String currentName, String label, String leave){
        String nextType = "String";
        String nextName = currentName + "_string";
        return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>())
                + generateEdgeToLeaveNodeString(nextType, nextName, leave);
    }

    public String escapeGraphVizString(String string){
        return StringEscapeUtils.escapeHtml4(string).replace("{", "\\{").replace("}", "\\}").replace("|", "\\|").replace("[", "\\[").replace("]", "\\]");
    }

    public String generateNodeString(String type, String name, ArrayList<String> properties){
        String propertiesString = "[ style = \"filled\" penwidth = 1 fillcolor = \"white\" fontname = \"Courier New\" shape = \"Mrecord\" label =<<table border=\"0\" cellborder=\"0\" cellpadding=\"3\" bgcolor=\"white\"><tr><td bgcolor=\"grey96\" align=\"center\" colspan=\"2\"><font color=\"black\"><B>" + escapeGraphVizString(getNodeName(type, name)) + "</B></font></td></tr>";
        for (String property : properties) {
            propertiesString += "<tr><td align=\"left\"> &bull; " + escapeGraphVizString(property) + " </td></tr>";
        }
        propertiesString += "</table>> ]";
        return "\"" + getNodeName(type, name) + "\" " + propertiesString + ";\n";
    }

    public String generateNodePropertyString(String name, String value){
        return name + ": " + value;
    }

    public String getTreeNodes(SQLPPMapping sqlppMapping, String name) throws Exception {
        String currentName = (name.equals("")) ? "mapping" : name + "_";
        String currentType = "SQLPPMapping";
        ArrayList<String> mappingProperties = new ArrayList<String>();
        mappingProperties.add(generateNodePropertyString(".toString()", sqlppMapping.getTripleMaps().stream().map(x -> x.toString()).collect(Collectors.joining("\"\\n\""))));
        String nodeString = generateNodeString(currentType, currentName, mappingProperties);

        ImmutableList<SQLPPTriplesMap> mappingList = sqlppMapping.getTripleMaps();

        String nextType = "ImmutableList<SQLPPTriplesMap>";
        String nextName = (name.equals("")) ? "mappings" : name + "_";
        String label = ".getTripleMaps()";
        nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());

        currentName = nextName;
        currentType = nextType;

        for (int i=0; i<mappingList.size(); i++) {

            SQLPPTriplesMap sqlPPTriplesMap = mappingList.get(i);

            nextType = "SQLPPTriplesMap";
            nextName = (name.equals("")) ? "mapping" + i : name + "_mapping" + i;
            label = ".get(" + i + ")";
            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());

            nodeString += getTreeNodes(sqlPPTriplesMap, nextName);
            //TO DELETE
//            break;
        }
        return nodeString;
    }

    public String getTreeNodes(SQLPPTriplesMap sqlPPTriplesMap, String name) throws Exception {
        String currentType = "SQLPPTriplesMap";
        String currentName = name;

        ImmutableList<TargetAtom> targetAtoms = sqlPPTriplesMap.getTargetAtoms();

        String nextType = "ImmutableList<TargetAtom>";
        String nextName = name + "_targetAtoms";
        String label = ".getTargetAtoms()";
        String nodeString = generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());

        currentType = nextType;
        currentName = nextName;

        for (int i=0; i<targetAtoms.size(); i++) {


            TargetAtom targetAtom = targetAtoms.get(i);

            nextType = "TargetAtom";
            nextName = name + "_targetAtom" + i;
            label = ".get(" + i + ")";

            //add properties of targetAtom
            ArrayList<String> targetAtomProperties = new ArrayList<String>();
            targetAtomProperties.add(generateNodePropertyString(".getPredicateIRI().get().getIRIString()", targetAtom.getPredicateIRI().get().getIRIString()));
            targetAtomProperties.add(generateNodePropertyString(".getProjectionAtom().toString()", targetAtom.getProjectionAtom().toString()));

            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, targetAtomProperties);

            //starting nodes of targetAtom
            currentType = nextType;
            currentName = nextName;

            //add subject
            nextType = "ImmutableTerm";
            nextName = currentName + "_subject_raw";
            label = ".getSubstitutedTerm(0)";
            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());
            nodeString += getTreeNodes(targetAtom.getSubstitutedTerm(0), nextName);

            //add predicate
            nextType = "ImmutableTerm";
            nextName = currentName + "_predicate_raw";
            label = ".getSubstitutedTerm(1)";
            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());
            nodeString += getTreeNodes(targetAtom.getSubstitutedTerm(1), nextName);

            //add object
            nextType = "ImmutableTerm";
            nextName = currentName + "_object_raw";
            label = ".getSubstitutedTerm(2)";
            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());
            nodeString += getTreeNodes(targetAtom.getSubstitutedTerm(2), nextName);

//            //TO DELETE
//            break;
        }
        return nodeString;

    }

    public String getTreeNodes(ImmutableTerm immutableTerm, String name) throws Exception {
        String currentType = "ImmutableTerm";
        String currentName = name;
        String nextName = name.substring(0, name.length() - 4);
        if (immutableTerm instanceof NonGroundFunctionalTermImpl){
            NonGroundFunctionalTermImpl nonGroundFunctionalTerm = (NonGroundFunctionalTermImpl) immutableTerm;
            String nextType = "NonGroundFunctionalTermImpl";
            String label = "cast to NonGroundFunctionalTermImpl";
            //add properties of targetAtom
            ArrayList<String> nonGroundFunctionalTermProperties = new ArrayList<String>();
            nonGroundFunctionalTermProperties.add(generateNodePropertyString(".getArity()", "" + nonGroundFunctionalTerm.getArity()));
            nonGroundFunctionalTermProperties.add(generateNodePropertyString(".isGround()", "" + nonGroundFunctionalTerm.isGround()));
            nonGroundFunctionalTermProperties.add(generateNodePropertyString(".getFunctionSymbol().toString()", "" + nonGroundFunctionalTerm.getFunctionSymbol().toString()));
            nonGroundFunctionalTermProperties.add(generateNodePropertyString(".getVariableStream().count()", "" + nonGroundFunctionalTerm.getVariableStream().count()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, nonGroundFunctionalTermProperties)
                    + getTreeNodes(nonGroundFunctionalTerm, nextName);
        } else if (immutableTerm instanceof RDFTermTypeConstantImpl){
            RDFTermTypeConstantImpl rdfTermTypeConstant = (RDFTermTypeConstantImpl) immutableTerm;
            String nextType = "RDFTermTypeConstantImpl";
            String label = "cast to RDFTermTypeConstantImpl";
            //add properties of targetAtom
            ArrayList<String> rdfTermTypeConstantProperties = new ArrayList<String>();
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".getValue()", rdfTermTypeConstant.getValue()));
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".getType().toString()", rdfTermTypeConstant.getType().toString()));
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".getRDFTermType().toString()", rdfTermTypeConstant.getRDFTermType().toString()));
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".isGround()", "" + rdfTermTypeConstant.isGround()));
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".isDeterministic()", "" + rdfTermTypeConstant.isDeterministic()));
            rdfTermTypeConstantProperties.add(generateNodePropertyString(".getVariableStream().count()", "" + rdfTermTypeConstant.getVariableStream().count()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, rdfTermTypeConstantProperties)
                    + getTreeNodes(rdfTermTypeConstant, nextName);
        } else if (immutableTerm instanceof VariableImpl){
            VariableImpl variable = (VariableImpl) immutableTerm;
            String nextType = "VariableImpl";
            String label = "cast to VariableImpl";
            //add properties of targetAtom
            ArrayList<String> variableProperties = new ArrayList<String>();
            variableProperties.add(generateNodePropertyString(".getName()", "" + variable.getName()));
            variableProperties.add(generateNodePropertyString(".isGround()", "" + variable.isGround()));
            variableProperties.add(generateNodePropertyString(".isNull", "" + variable.isNull()));
            variableProperties.add(generateNodePropertyString(".getVariableStream().count()", "" + variable.getVariableStream().count()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, variableProperties)
                    + getTreeNodes(variable, nextName);
        } else if (immutableTerm instanceof IRIConstantImpl) {
            IRIConstantImpl iriConstant = (IRIConstantImpl) immutableTerm;
            String nextType = "IRIConstantImpl";
            String label = "cast to IRIConstantImpl";
            //add properties of targetAtom
            ArrayList<String> iriConstantProperties = new ArrayList<String>();
            iriConstantProperties.add(generateNodePropertyString(".getValue()", iriConstant.getValue()));
            iriConstantProperties.add(generateNodePropertyString(".getType().toString()", iriConstant.getType().toString()));
            iriConstantProperties.add(generateNodePropertyString(".getIRI().getIRIString()", iriConstant.getIRI().getIRIString()));
            iriConstantProperties.add(generateNodePropertyString(".isGround()", "" + iriConstant.isGround()));
            iriConstantProperties.add(generateNodePropertyString(".isDeterministic()", "" + iriConstant.isDeterministic()));
            iriConstantProperties.add(generateNodePropertyString(".getVariableStream().count()", "" + iriConstant.getVariableStream().count()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, iriConstantProperties)
                    + getTreeNodes(iriConstant, nextName);
        }
        throw new Exception("ImmutableTerm can not be mapped to a mapping atom. Class of object is " + immutableTerm.getClass());
    }

    public String getTreeNodes(NonGroundFunctionalTermImpl nonGroundFunctionalTerm, String name) throws Exception {
        String currentType = "NonGroundFunctionalTermImpl";
        String currentName = name;
//            nonGroundFunctionalTermProperties.add(generatePropertyString(".getFunctionSymbol()", "" + nonGroundFunctionalTerm.getFunctionSymbol()));

        // add function symbol
        FunctionSymbol functionSymbol = nonGroundFunctionalTerm.getFunctionSymbol();

        String nextType = "FunctionSymbol";
        String nextName = name + "_functionSymbol_raw";
        String label = ".getFunctionSymbol()";
        String nodeString = generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>())
                + getTreeNodes(functionSymbol, nextName);

        // add terms
        ImmutableList<? extends ImmutableTerm> terms  = nonGroundFunctionalTerm.getTerms();

        nextType = "ImmutableList<? extends ImmutableTerm>";
        nextName = name + "_terms";
        label = ".getTerms()";
        nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());

        currentType = nextType;
        currentName = nextName;

        for (int i=0; i<terms.size(); i++) {

            ImmutableTerm term = terms.get(i);

            nextType = "ImmutableTerm";
            nextName = name + "_term" + i +"_raw";
            label = ".get(" + i + ")";

            nodeString += generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, new ArrayList<String>());
            nodeString += getTreeNodes(term, nextName);

            //TO DELETE
//            break;
        }
        return nodeString;
    }

    private String getTreeNodes(FunctionSymbol functionSymbol, String name) throws Exception {
        String currentType = "FunctionSymbol";
        String currentName = name;
        String nextName = name.substring(0, name.length() - 4);

        if (functionSymbol instanceof IRIStringTemplateFunctionSymbolImpl){
            IRIStringTemplateFunctionSymbolImpl iriStringTemplateFunctionSymbol = (IRIStringTemplateFunctionSymbolImpl) functionSymbol;
            String nextType = "IRIStringTemplateFunctionSymbolImpl";
            String label = "cast to IRIStringTemplateFunctionSymbolImpl";
            //add properties
            ArrayList<String> iriStringTemplateFunctionSymbolProperties = new ArrayList<String>();
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".getArity()", "" + iriStringTemplateFunctionSymbol.getArity()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".getTemplate()", iriStringTemplateFunctionSymbol.getTemplate()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".getName()", iriStringTemplateFunctionSymbol.getName()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".getTemplateComponents().toString()", iriStringTemplateFunctionSymbol.getTemplateComponents().toString()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".isAggregation()", "" + iriStringTemplateFunctionSymbol.isAggregation()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".isDeterministic()", "" + iriStringTemplateFunctionSymbol.isDeterministic()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".isPreferringToBePostProcessedOverBeingBlocked()", "" + iriStringTemplateFunctionSymbol.isPreferringToBePostProcessedOverBeingBlocked()));
            iriStringTemplateFunctionSymbolProperties.add(generateNodePropertyString(".shouldBeDecomposedInUnion()", "" + iriStringTemplateFunctionSymbol.shouldBeDecomposedInUnion()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, iriStringTemplateFunctionSymbolProperties)
                    + generateEdgeToLeaveNodeString(nextType, nextName, ".getTemplate()", iriStringTemplateFunctionSymbol.getTemplate());
        } else if (functionSymbol instanceof TemporaryDBTypeConversionToStringFunctionSymbolImpl){
            TemporaryDBTypeConversionToStringFunctionSymbolImpl temporaryDBTypeConversionToStringFunctionSymbol = (TemporaryDBTypeConversionToStringFunctionSymbolImpl) functionSymbol;
            String nextType = "TemporaryDBTypeConversionToStringFunctionSymbolImpl";
            String label = "cast to TemporaryDBTypeConversionToStringFunctionSymbolImpl";
            //add properties
            ArrayList<String> temporaryDBTypeConversionToStringFunctionSymbolProperties = new ArrayList<String>();
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".getName()", temporaryDBTypeConversionToStringFunctionSymbol.getName()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".isTemporary()", "" + temporaryDBTypeConversionToStringFunctionSymbol.isTemporary()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".getInputType().toString()", temporaryDBTypeConversionToStringFunctionSymbol.getInputType().toString()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms()", "" + temporaryDBTypeConversionToStringFunctionSymbol.isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".isDeterministic()", "" + temporaryDBTypeConversionToStringFunctionSymbol.isDeterministic()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".isSimple()", "" + temporaryDBTypeConversionToStringFunctionSymbol.isSimple()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".getTargetType().toString()", "" + temporaryDBTypeConversionToStringFunctionSymbol.getTargetType().toString()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".isPreferringToBePostProcessedOverBeingBlocked()", "" + temporaryDBTypeConversionToStringFunctionSymbol.isPreferringToBePostProcessedOverBeingBlocked()));
            temporaryDBTypeConversionToStringFunctionSymbolProperties.add(generateNodePropertyString(".shouldBeDecomposedInUnion()", "" + temporaryDBTypeConversionToStringFunctionSymbol.shouldBeDecomposedInUnion()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, temporaryDBTypeConversionToStringFunctionSymbolProperties)
                    + generateEdgeToLeaveNodeString(nextType, nextName, ".getName()", temporaryDBTypeConversionToStringFunctionSymbol.getName());
        } else if (functionSymbol instanceof RDFTermFunctionSymbolImpl) {
            RDFTermFunctionSymbolImpl rdfTermFunctionSymbol = (RDFTermFunctionSymbolImpl) functionSymbol;
            String nextType = "RDFTermFunctionSymbolImpl";
            String label = "cast to RDFTermFunctionSymbolImpl";
            //add properties
            ArrayList<String> rdfTermFunctionSymbolProperties = new ArrayList<String>();
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".getName()", rdfTermFunctionSymbol.getName()));
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms()", "" + rdfTermFunctionSymbol.isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms()));
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".isDeterministic()", "" + rdfTermFunctionSymbol.isDeterministic()));
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".getVariableStream().getArity()", "" + rdfTermFunctionSymbol.getArity()));
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".shouldBeDecomposedInUnion()", "" + rdfTermFunctionSymbol.shouldBeDecomposedInUnion()));
            rdfTermFunctionSymbolProperties.add(generateNodePropertyString(".isAggregation()", "" + rdfTermFunctionSymbol.isAggregation()));
            return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, rdfTermFunctionSymbolProperties)
                    + generateEdgeToLeaveNodeString(nextType, nextName, ".getName()", rdfTermFunctionSymbol.getName());
        }
        throw new Exception("FunctionSymbol can not be mapped to a mapping atom. Class of object is " + functionSymbol.getClass());
    }

    public String getTreeNodes(RDFTermTypeConstantImpl rdfTermTypeConstant, String name) {
        return generateEdgeToLeaveNodeString("RDFTermTypeConstantImpl", name, ".getValue()", rdfTermTypeConstant.getValue());
    }

    public String getTreeNodes(VariableImpl variableImpl, String name) {
        return generateEdgeToLeaveNodeString("VariableImpl", name, ".getVariableStream().collect(Collectors.toList()).stream().map(x->x.toString()).collect(Collectors.joining(\\\"\\\\n\\\"))", variableImpl.getVariableStream().collect(Collectors.toList()).stream().map(x->x.toString()).collect(Collectors.joining("\n")));
    }

    public String getTreeNodes(IRIConstantImpl iriConstant, String name) {
        String currentType = "IRIConstantImpl";
        String currentName = name;
        IRI iri = iriConstant.getIRI();
        String nextType = "IRI";
        String nextName = name + "_iri";
        String label = ".getIRI()";
        ArrayList<String> iriProperties = new ArrayList<String>();
        iriProperties.add(generateNodePropertyString(".getIRIString()", iri.getIRIString()));
        iriProperties.add(generateNodePropertyString(".ntriplesString()", iri.ntriplesString()));
        return generateNodeAndEdgeString(currentType, currentName, nextType, nextName, label, iriProperties)
                + generateEdgeToLeaveNodeString(nextType, nextName, ".getIRIString()", iri.getIRIString());
    }


    public void generateMappingTree(String urlOrPath, String mappingFilename, String propertyFilePath, String graphFile) throws Exception {
        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(urlOrPath)
                .nativeOntopMappingFile(mappingFilename)
                .propertyFile(propertyFilePath)
                .enableTestMode()
                .build();

        SQLPPMapping mapping = configure.loadProvidedPPMapping();
        String mappingTree = generateTreeOfMappingAtoms(mapping);
        System.out.println(mappingTree);
        BufferedWriter writer = new BufferedWriter(new FileWriter(graphFile));
        writer.write(mappingTree);
        writer.close();
        return;
    }


    @Test
    public void testGenerateMappingTree() throws Exception {
        generateMappingTree("src/test/resources/compareIRI/boot-multiple-inheritance.owl",
                "src/test/resources/compareIRI/boot-multiple-inheritance.obda",
                "src/test/resources/compareIRI/boot-multiple-inheritance.properties",
                "src/test/resources/compareIRI/boot-multiple-inheritance.gv");
        generateMappingTree("src/test/resources/compareIRI/boot-multiple-inheritance.owl",
                "src/test/resources/compareIRI/boot-multiple-inheritance-v1.obda",
                "src/test/resources/compareIRI/boot-multiple-inheritance.properties",
                "src/test/resources/compareIRI/boot-multiple-inheritance-v1.gv");
        generateMappingTree("src/test/resources/compareIRI/boot-multiple-inheritance.owl",
                "src/test/resources/compareIRI/boot-multiple-inheritance-v2.obda",
                "src/test/resources/compareIRI/boot-multiple-inheritance.properties",
                "src/test/resources/compareIRI/boot-multiple-inheritance-v2.gv");
    }


    public String printNonGroundFunctionalTermImpl(NonGroundFunctionalTermImpl term, String name){
        String res = "******************************* " + name +" *******************************\n";
        res += "Casting: NonGroundFunctionalTermImpl " + name + "\n";
        res += name + ": " + term + "\n";
        res += name + ".getClass(): " + term.getClass() + "\n";
        res += name + ".getArity(): " + term.getArity() + "\n";
        res += name + ".isGround(): " + term.isGround() + "\n";
        res += name + ".getFunctionSymbol(): " + term.getFunctionSymbol() + "\n";
        res += name + ".getTerms() size: " + term.getTerms().size() + "\n";
        res += name + ".getTerms() values:\n" + term.getTerms().stream().map(x -> x.toString()).collect(Collectors.joining("\n")) + "\n";
        res += "***************************************************************************************\n";
//        System.out.println(res);
        ImmutableList<? extends ImmutableTerm> terms = term.getTerms();
        for(int i = 0; i < terms.size(); i++){
            res += printMappingAtoms(terms.get(i), name + "_term_" + i);
        }
        return res;
    }

    public String printRDFTermTypeConstantImpl(RDFTermTypeConstantImpl term, String name){
        String res = "******************************* " + name +" *******************************\n";
        res += "Casting: RDFTermTypeConstantImpl " + name + "\n";
        res += name + ": " + term + "\n";
        res += name + ".getClass(): " + term.getClass() + "\n";
        res += name + ".getValue(): " + term.getValue() + "\n";
        res += name + ".getType(): " + term.getType() + "\n";
        res += name + ".getRDFTermType(): " + term.getRDFTermType() + "\n";
        res += name + ".isGround(): " + term.isGround() + "\n";
        res += name + ".isDeterministic(): " + term.isDeterministic() + "\n";
        res += name + ".getVariableStream() size: " + term.getVariableStream().count() + "\n";
        res += name + ".getVariableStream() values: " + term.getVariableStream().collect(Collectors.toList()).stream().map(x -> x.toString()).collect(Collectors.joining("\n")) + "\n";
        res += "***************************************************************************************\n";
//        System.out.println(res);
        return res;
    }

    public String printVariableImpl(VariableImpl term, String name){
        String res = "******************************* " + name +" *******************************\n";
        res += "Casting: VariableImpl " + name + "\n";
        res += name + ": " + term + "\n";
        res += name + ".getClass(): " + term.getClass() + "\n";
        res += name + ".getName(): " + term.getName() + "\n";
        res += name + ".isGround(): " + term.isGround() + "\n";
        res += name + ".isNull(): " + term.isNull() + "\n";
        res += name + ".getVariableStream() size: " + term.getVariableStream().count() + "\n";
        res += name + ".getVariableStream() values: " + term.getVariableStream().collect(Collectors.toList()).stream().map(x -> x.toString()).collect(Collectors.joining("\n")) + "\n";
        res += "***************************************************************************************\n";
//        System.out.println(res);
        return res;
    }

    public String printIRIConstantImpl(IRIConstantImpl term, String name){
        String res = "******************************* " + name +" *******************************\n";
        res += "Casting: IRIConstantImpl " + name + "\n";
        res += name + ": " + term + "\n";
        res += name + ".getClass(): " + term.getClass() + "\n";
        res += name + ".getIRI(): " + term.getIRI() + "\n";
        res += name + ".getValue(): " + term.getValue() + "\n";
        res += name + ".getType(): " + term.getType() + "\n";
        res += name + ".isGround(): " + term.isGround() + "\n";
        res += name + ".isDeterministic(): " + term.isDeterministic() + "\n";
        res += name + ".getVariableStream() size: " + term.getVariableStream().count() + "\n";
        res += name + ".getVariableStream() values: " + term.getVariableStream().collect(Collectors.toList()).stream().map(x -> x.toString()).collect(Collectors.joining("\n")) + "\n";
        res += "***************************************************************************************\n";
//        System.out.println(res);
        return res;
    }

    public String printSQLPPTriplesMap(SQLPPTriplesMap sqlppTriplesMap, String name) {
        String res = "SQLPPTriplesMap " + name + ":\n";
        res += sqlppTriplesMap + "\n";
        res += "---------------------------------" + "\n";
        res += name + ".getId(): " + sqlppTriplesMap.getId() + "\n";
        res += name + ".getSourceQuery(): " + sqlppTriplesMap.getSourceQuery() + "\n";
        res += "---------------------------------" + "\n";
        name = sqlppTriplesMap.getId();
        List<TargetAtom> atoms = sqlppTriplesMap.getTargetAtoms();
        for (int i = 0; i < atoms.size(); i++) {
            TargetAtom targetAtom = atoms.get(i);
            String res_atom = ("---------------------------------NEW TARGET ATOM (index: " + i + ")---------------------------------\n");
            res_atom += "targetAtom: " + targetAtom + "\n";
            res_atom += "targetAtom.getPredicateIRI" + targetAtom.getPredicateIRI();
            res_atom += "targetAtom.getProjectionAtom" + targetAtom.getProjectionAtom();
            res_atom += "---------------------------------subject---------------------------------" + "\n";
            ImmutableTerm subject = targetAtom.getSubstitutedTerm(0);
            res_atom += printMappingAtoms(subject, name + "_target_atom_" + i + "_subject");
            res_atom += "---------------------------------predicate---------------------------------" + "\n";
            ImmutableTerm predicate = targetAtom.getSubstitutedTerm(1);
            res_atom += printMappingAtoms(predicate, name + "_target_atom_" + i + "_predicate");
            res_atom += "---------------------------------object---------------------------------" + "\n";
            ImmutableTerm object = targetAtom.getSubstitutedTerm(2);
            res_atom += printMappingAtoms(object, name + "target_atom_" + i + "_object");
            res += res_atom;
        }
        return res;
    }

    public String printMappingAtoms(Object object, String name){
        String res = "";
        if(object instanceof SQLPPTriplesMap){
            res += printSQLPPTriplesMap((SQLPPTriplesMap)object, name);
        } else if (object instanceof NonGroundFunctionalTermImpl){
            res += printNonGroundFunctionalTermImpl((NonGroundFunctionalTermImpl)object, name);
        } else if (object instanceof RDFTermTypeConstantImpl){
            res += printRDFTermTypeConstantImpl((RDFTermTypeConstantImpl)object, name);
        } else if (object instanceof VariableImpl){
            res += printVariableImpl((VariableImpl)object, name);
        } else if (object instanceof IRIConstantImpl) {
            res += printIRIConstantImpl((IRIConstantImpl) object, name);
        }
        return res;
    }

    public String generateLogOfMappingAtoms(SQLPPMapping mapping) {
        String res = "";
        ImmutableList mappingList = mapping.getTripleMaps();
        for (int i=0; i<mappingList.size(); i++) {
            SQLPPTriplesMap tripleMap = (SQLPPTriplesMap) mappingList.get(i);
            res += printMappingAtoms(tripleMap, "tripleMap_" + i);
        }
        return res;
    }

    @Test
    /**Mapping test*********************************/
    public void testGenerateLogOfMappings() throws Exception {
        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/compareIRI/boot-multiple-inheritance.owl")
                .nativeOntopMappingFile("src/test/resources/compareIRI/boot-multiple-inheritance.obda")
                .propertyFile("src/test/resources/compareIRI/boot-multiple-inheritance.properties")
                .enableTestMode()
                .build();

        SQLPPMapping mapping = configure.loadProvidedPPMapping();

        System.out.println(generateLogOfMappingAtoms(mapping));
        return;
    }

    public boolean checkEqualityNonGroundFunctionalTermImplIRI(NonGroundFunctionalTermImpl iri0, NonGroundFunctionalTermImpl iri1) throws Exception {
        if (((RDFTermTypeConstantImpl) iri0.getTerm(1)).getValue().compareTo(((RDFTermTypeConstantImpl) iri1.getTerm(1)).getValue()) == 0 & ((RDFTermTypeConstantImpl) iri0.getTerm(1)).getValue().compareTo("IRI") != 0) {
            throw new Exception("Target atom is not an IRI");
        } else if (((IRIStringTemplateFunctionSymbolImpl) ((NonGroundFunctionalTermImpl) iri0.getTerm(0)).getFunctionSymbol()).getTemplate().compareTo(((IRIStringTemplateFunctionSymbolImpl) ((NonGroundFunctionalTermImpl) iri1.getTerm(0)).getFunctionSymbol()).getTemplate()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void printExtractIRISteps(OntopSQLOWLAPIConfiguration configure) throws MappingException {
        SQLPPMapping mapping = configure.loadProvidedPPMapping();

        ImmutableList mappings = mapping.getTripleMaps();
        System.out.println("*********************************");
        System.out.println("mappings: " + mappings);
        System.out.println("*********************************");
        Object mapping0_raw = mappings.get(0);
        SQLPPTriplesMap mapping0 = (SQLPPTriplesMap) mapping0_raw;
        System.out.println("*********************************");
        System.out.println("mapping0: " + mapping0);
        System.out.println("*********************************");
        ImmutableList<TargetAtom> targetAtoms = mapping0.getTargetAtoms();
        TargetAtom targetAtom_raw = targetAtoms.get(0);
        TargetAtomImpl targetAtom = (TargetAtomImpl) targetAtom_raw;
        System.out.println("*********************************");
        System.out.println("targetAtom: " + targetAtom);
        System.out.println("*********************************");

        ImmutableTerm targetAtomSubstitutedTerm0_raw = targetAtom.getSubstitutedTerm(0);
        NonGroundFunctionalTermImpl targetAtomSubstitutedTerm0 = (NonGroundFunctionalTermImpl) targetAtomSubstitutedTerm0_raw;
        System.out.println("*********************************");
        System.out.println("targetAtomSubstitutedTerm0: " + targetAtomSubstitutedTerm0);
        System.out.println("*********************************");

        ImmutableTerm targetAtomSubstitutedTerm0Term0_raw = targetAtomSubstitutedTerm0.getTerm(0);
        NonGroundFunctionalTermImpl targetAtomSubstitutedTerm0Term0 = (NonGroundFunctionalTermImpl) targetAtomSubstitutedTerm0Term0_raw;
        System.out.println("*********************************");
        System.out.println("targetAtomSubstitutedTerm0Term0: " + targetAtomSubstitutedTerm0Term0);
        System.out.println("*********************************");

        FunctionSymbol targetAtomSubstitutedTerm0Term0FunctionSymbol_raw = targetAtomSubstitutedTerm0Term0.getFunctionSymbol();
        IRIStringTemplateFunctionSymbolImpl targetAtomSubstitutedTerm0Term0FunctionSymbol = (IRIStringTemplateFunctionSymbolImpl) targetAtomSubstitutedTerm0Term0FunctionSymbol_raw;
        System.out.println("*********************************");
        System.out.println(targetAtomSubstitutedTerm0Term0FunctionSymbol);
        System.out.println("*********************************");

        String targetAtomSubstitutedTerm0Term0FunctionSymbolTemplate = targetAtomSubstitutedTerm0Term0FunctionSymbol.getTemplate();
        System.out.println("*********************************");
        System.out.println(targetAtomSubstitutedTerm0Term0FunctionSymbolTemplate);
        System.out.println("*********************************");
        return;
    }

    public boolean testExtractIRIAndCompare(String ontologyFile, String nativeOntopMappingFile, String propertyFilePath) throws Exception {
        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(ontologyFile)
                .nativeOntopMappingFile(nativeOntopMappingFile)
                .propertyFile(propertyFilePath)
                .enableTestMode()
                .build();

        printExtractIRISteps(configure);

        SQLPPMapping mapping = configure.loadProvidedPPMapping();
        ImmutableList<SQLPPTriplesMap> mappings = mapping.getTripleMaps();
        NonGroundFunctionalTermImpl iri0 = (NonGroundFunctionalTermImpl)((SQLPPTriplesMap) mappings.get(0)).getTargetAtoms().get(0).getSubstitutedTerm(0);
        NonGroundFunctionalTermImpl iri1 = (NonGroundFunctionalTermImpl)((SQLPPTriplesMap) mappings.get(1)).getTargetAtoms().get(0).getSubstitutedTerm(0);
        System.out.println("Are the functional terms (or IRI) equal?\n" + checkEqualityNonGroundFunctionalTermImplIRI(iri0, iri1));
        System.out.println("*********************************");
        return checkEqualityNonGroundFunctionalTermImplIRI(iri0, iri1);
    }

    @Test
    public void testExtractIRIAndCompare() throws Exception {
        assertEquals(false, testExtractIRIAndCompare("src/test/resources/compareIRI/boot-multiple-inheritance.owl", "src/test/resources/compareIRI/boot-multiple-inheritance-v1.obda", "src/test/resources/compareIRI/boot-multiple-inheritance.properties"));
        assertEquals(true, testExtractIRIAndCompare("src/test/resources/compareIRI/boot-multiple-inheritance.owl", "src/test/resources/compareIRI/boot-multiple-inheritance-v2.obda", "src/test/resources/compareIRI/boot-multiple-inheritance.properties"));
        return;
    }

}
