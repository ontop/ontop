package it.unibz.inf.ontop.answering.resultset.legacy;

import javax.annotation.Nullable;

public class MainTypeLangValues {

    @Nullable
    private Integer typeValue;

    @Nullable
    private String langValue;

    @Nullable
    private Object mainValue;

    MainTypeLangValues(Object mainValue, Integer typeValue, String langValue) {
        this.mainValue = mainValue;
        this.typeValue = typeValue;
        this.langValue = langValue;
    }

    @Nullable
    public Integer getTypeValue() {
        return typeValue;
    }

    @Nullable
    public String getLangValue() {
        return langValue;
    }

    @Nullable
    public Object getMainValue() {
        return mainValue;
    }
}

