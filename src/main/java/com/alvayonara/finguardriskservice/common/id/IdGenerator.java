package com.alvayonara.finguardriskservice.common.id;

import de.huxhorn.sulky.ulid.ULID;

public class IdGenerator {
    private static final ULID ulid = new ULID();
    public static String generate(String prefix) {
        return prefix + "_" + ulid.nextULID();
    }
}
