package com.kirchnersolutions.utilities.SerialService;

import com.kirchnersolutions.database.objects.Field;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.objects.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerialConfig {

    @Bean
    public Serializer<Transaction> transactionSerializer(){
        return new TransactionSerializer();
    }

    @Bean
    public Serializer<String[]> configSerializer(){
        return new ConfigSerializer();
    }

    @Bean
    public Serializer<Field> fieldSerializer() {
        return new FieldSerializer();
    }

    @Bean
    public Serializer<Object> generalSerializer(){
        return new GeneralSerializer();
    }

    @Bean
    public Serializer<User> userSerializer(){
        return new UserSerializer();
    }

}
