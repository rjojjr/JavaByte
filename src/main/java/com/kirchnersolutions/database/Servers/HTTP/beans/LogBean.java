package com.kirchnersolutions.database.Servers.HTTP.beans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class LogBean {

    private String name = "", count = "";

}
