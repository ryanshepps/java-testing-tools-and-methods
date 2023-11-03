package com.valueconnect.domain.generated;

import com.valueconnect.repository.domain.BooleanExpression;

public class QueryObject {

    private QueryObject value;

    public QueryObject name;
    public QueryObject region;
    public QueryObject disabled;
    public QueryObject province;
    public QueryObject id;
    
    public BooleanExpression isNotNull() {
        return new BooleanExpression(value);
    }

    public BooleanExpression like(String string) {
        return null;
    }

    public BooleanExpression eq(Long provinceId) {
        return null;
    }

    public BooleanExpression eq(boolean b) {
        return null;
    }

    public BooleanExpression eq(String cityName) {
        return null;
    }

}
