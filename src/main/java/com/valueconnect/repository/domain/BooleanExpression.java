package com.valueconnect.repository.domain;

import com.valueconnect.domain.generated.QueryObject;

public class BooleanExpression {

    private QueryObject queryObject;

    public BooleanExpression(QueryObject queryObject) {
        this.queryObject = queryObject;
    }

    public BooleanExpression and(Object eq) {
        return null;
    }
    
}
