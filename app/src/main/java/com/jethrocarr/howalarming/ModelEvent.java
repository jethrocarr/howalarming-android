package com.jethrocarr.howalarming;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Express event values in the sqllite DB.
 */
public class ModelEvent extends SugarRecord {
    String type;
    String code;
    String message;
    String raw;

    // Default constructor is necessary for SugarRecord
    public ModelEvent() {
    }

    public ModelEvent(String type, String code, String message, String raw) {
        this.type = type;
        this.code = code;
        this.message = message;
        this.raw = raw;
    }
}