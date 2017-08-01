package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.collect.ImmutableList;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class OntopMappingCleanupRelatedCommand implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.obda",
            description = "Output mapping file in Ontop native format (.obda)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String outputMappingFile;


    @Option(type = OptionType.COMMAND, name = {"--simplify-projection"},
            description = "Replace projection with * whenever possible in SQL queries")
    protected boolean SIMPLIFY_SQL_PROJECTIONS;

    private final Pattern selectPattern = Pattern.compile("SELECT\\W.*\\WFROM", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private final Pattern asPattern = Pattern.compile("\\WAS\\W", Pattern.CASE_INSENSITIVE);

      /*
        returns colum names in a given template
     */

    protected  Set<String> getTemplateColumns(String template) {
        Set<String> set = new HashSet<>();
        int pos = 0;
        while (true) {
            pos = template.indexOf('{', pos);
            if (pos == -1)
                break;

            if (pos > 0 && template.charAt(pos - 1) == '\\') {
                pos++;
                continue;
            }
            int endpos;
            do {
                endpos = template.indexOf('}', pos);
                if (endpos == -1) {
                    System.err.println("FAIL " + template + " AT " + pos);
                    return set;
                }
            } while (template.charAt(endpos - 1) == '\\');
            set.add(template.substring(pos + 1, endpos));
            pos++;
        }
        return set;
    }

    protected   String extractMappingColumnsFromSQL(Set<String> args, String sql, String mapping) {
        Matcher m = selectPattern.matcher(sql);
        if (m.find()) {
            String selectClause = m.group();
            String projection = selectClause.substring("SELECT ".length(), selectClause.length() - " FROM".length());
            String[] columns = projection.split(",");
            for (String column : columns) {
                Matcher mp = asPattern.matcher(column);
                String alias = (mp.find()) ? column.substring(mp.end()) : column;
                alias = alias.replaceAll("\\s", "");
                //if (!args.contains(alias))
                //	System.err.println("WARNING: column \"" + alias + "\" is not used in mapping " + mapping);

                args.add(alias);
            }
            return selectClause;
        }
        return null;
    }

    protected String getRenaming(Map<String, String> renaming, String selectClause, String mapping) {
        String replacementSelectClause = selectClause;

        for (Map.Entry<String, String> r : renaming.entrySet()) {
            Pattern p = Pattern.compile("\\b" + r.getKey() + "\\b", Pattern.DOTALL);
            Matcher mp = p.matcher(replacementSelectClause);
            if (mp.find())
                replacementSelectClause = mp.replaceFirst(r.getValue());
            else
                System.err.println("ERROR: cannot find column " + r.getKey() + " in " + replacementSelectClause + " in mapping " + mapping);
        }

        return replacementSelectClause;
    }



    /*
        replaces projection with * whenever possible
    */

    protected  String getSimplifiedProjection(String sql) {
        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select)statement;

                PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

                if (plainSelect.getJoins() == null) {
                    boolean ok = true;
                    for (SelectItem si : plainSelect.getSelectItems()) {
                        if (!(si instanceof SelectExpressionItem)) {
                            ok = false;
                            break;
                        }
                        SelectExpressionItem sei = (SelectExpressionItem) si;
                        if (sei.getAlias() != null) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        plainSelect.setSelectItems(ImmutableList.of(new AllColumns()));

                        Matcher m = selectPattern.matcher(sql);
                        if (m.find()) {
                            String selectClause = m.group();
                            String projection = selectClause.substring("SELECT".length(), selectClause.length() - "FROM".length());
                            Pattern endSpaces = Pattern.compile("\\S\\s+\\z", Pattern.DOTALL);
                            Matcher es = endSpaces.matcher(projection);
                            if (es.find())
                                projection = projection.substring(0, projection.length() - es.group().length() + 1);
                            return sql.replace(projection, " *");
                        }
                    }
                }
            }
        }
        catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return sql;
    }

    /*
        returns
            table name if the SQL query contains no join, no where and no column aliases
            null otherwise
     */

    protected  String extractSimpleTable(String sql) {
        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select)statement;
                PlainSelect plainSelect = (PlainSelect)select.getSelectBody();
                if (plainSelect.getJoins() == null) {
                    boolean ok = true;
                    for (SelectItem si : plainSelect.getSelectItems()) {
                        if (!(si instanceof SelectExpressionItem)) {
                            ok = false;
                            break;
                        }
                        SelectExpressionItem sei = (SelectExpressionItem) si;
                        if (sei.getAlias() != null) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok && (plainSelect.getWhere() == null))
                        return ((Table)plainSelect.getFromItem()).getName();
                }
            }
        }
        catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
}
