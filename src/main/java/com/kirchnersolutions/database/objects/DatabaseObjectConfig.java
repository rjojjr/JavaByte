package com.kirchnersolutions.database.objects;

import com.kirchnersolutions.utilities.SerialService.UserSerializer;
import org.springframework.context.annotation.Bean;

public class DatabaseObjectConfig {

    @Bean
    public UserSerializer UserSerializer(){
        return new UserSerializer();
    }

}