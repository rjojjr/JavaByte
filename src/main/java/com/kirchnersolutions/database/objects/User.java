package com.kirchnersolutions.database.objects;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import java.util.HashMap;
import java.util.Map;

public class User implements DatabaseObjectInterface {

    private Map<String, String> details = new HashMap<>();

    public User(Map<String, String> details) {
        this.details = details;
    }

    public String getDetail(String detailName) {
        return details.get(detailName);
    }

    public void setDetail(String detailName, String detailValue) {
        details.put(detailName, detailValue);
    }

    public Map<String, String> getDetails() {
        return new HashMap<>(details);
    }

    @Override
    public byte getHeader() throws Exception{
        return "3".getBytes("UTF-8")[0];
    }
}