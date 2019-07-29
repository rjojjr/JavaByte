package com.kirchnersolutions.database;

import com.kirchnersolutions.database.Configuration.*;
import com.kirchnersolutions.database.Servers.HTTP.DeviceRepository;
import com.kirchnersolutions.database.Servers.HTTP.DeviceService;
import com.kirchnersolutions.database.Servers.HTTP.HTTPService;
import com.kirchnersolutions.database.Servers.HTTP.StompService;
import com.kirchnersolutions.database.core.tables.*;
import com.kirchnersolutions.database.core.tables.threads.TableThreadService;
import com.kirchnersolutions.database.core.tables.transactionthreads.TransactionThreadService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.sessions.SessionRepository;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ComponentScan({"com.kirchnersolutions.utilities", "com.kirchnersolutions.database.Configuration", "com.kirchnersolutions.database.Controllers", "com.kirchnersolutions.database.core" +
       "", "com.kirchnersolutions.database.objects","com.kirchnersolutions.database.core.tables",  "com.kirchnersolutions.database.Servers.HTTP", "com.kirchnersolutions.database.Servers.HTTP.beans",
       "com.kirchnersolutions.database.sessions", "com.kirchnersolutions.utilities.SerialService"})
public class MainConfig {



    @Bean
    DatabaseObjectFactory databaseObjectFactory(){
        return new DatabaseObjectFactory();
    }




    @Bean
    TableConfiguration tableConfiguration(){
        return new TableConfiguration();
    }

    @Bean
    TransactionThreadService transactionThreadService(){
        return new TransactionThreadService();
    }

    @Bean
    TableThreadService tableThreadService(){
        return new TableThreadService();
    }

    @Bean
    TableManagerService tableManagerService() throws Exception{
        return new TableManagerService();
    }
    @Bean
    @DependsOn("tableManagerService")
    PasswordService passwordService()  throws Exception{
        return new PasswordService();
    }
    @Bean
    UserRepository userRepository() throws Exception{
        return new UserRepository();
    }

    @Bean
    UserService userService() throws Exception{
        return new UserService();
    }

    @Bean
    TransactionRepository transactionRepository() throws Exception{
        return new TransactionRepository();
    }

    @Bean
    TransactionService transactionService() throws Exception{
        return new TransactionService();
    }

    @Bean
    SessionRepository sessionRepository(){
        return new SessionRepository();
    }



    @Bean
    SessionService sessionService(){
        return new SessionService();
    }

    @Bean
    DeviceRepository deviceRepository() throws Exception{
        return new DeviceRepository();
    }

    @Bean
    DeviceService deviceService(){
        return new DeviceService();
    }

    @Bean
    HTTPService httpService(){
        return new HTTPService();
    }

    @Bean
    StompService stompService(){
        return new StompService();
    }

    @Bean
    ConfigurationService configurationService() throws Exception{
        return new ConfigurationService();
    }


}
