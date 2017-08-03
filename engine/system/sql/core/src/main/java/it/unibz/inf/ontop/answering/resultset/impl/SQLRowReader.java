package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLRowReader {

    public List<MainTypeLangValues> read(ResultSet rs, int bindingNamesCount) throws SQLException {

        final ImmutableList.Builder<MainTypeLangValues> builder = ImmutableList.builder();

        for (int i = 1; i <= bindingNamesCount; i++) {
            final int mainColumnIndex = 3 * i;
            final int typeColumnIndex = 3 * i - 2;
            final int langColumnIndex = 3 * i - 1;

            builder.add(new MainTypeLangValues(
                    rs.getObject(mainColumnIndex),
                    rs.getInt(typeColumnIndex),
                    rs.getString(langColumnIndex)
            ));
        }

        return builder.build();
    }
}
