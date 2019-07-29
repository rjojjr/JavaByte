package com.kirchnersolutions.database.Servers.HTTP.beans;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StompTableRequest {

    private String username = "", sessionid = "", tablename = "", query = "";

}
