package com.kirchnersolutions.database;

import com.kirchnersolutions.database.Servers.socket.SocketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.ApplicationScope;

@Configuration
@ComponentScan({"com.kirchnersolutions.utilities", "com.kirchnersolutions.database.Configuration", "com.kirchnersolutions.database.dev", "com.kirchnersolutions.database.Controllers", "com.kirchnersolutions.database.core" +
        "", "com.kirchnersolutions.database.objects","com.kirchnersolutions.database.core.tables",  "com.kirchnersolutions.database.Servers.HTTP",  "com.kirchnersolutions.database.Servers.socket", "com.kirchnersolutions.database.Servers.HTTP.beans",
        "com.kirchnersolutions.database.sessions", "com.kirchnersolutions.utilities.SerialService", "com.kirchnersolutions.database.exceptions"})
public class MainConfig {

/*
    @Bean
    public SocketService socketService() throws Exception{
        return new SocketService();
    }

 */

}
