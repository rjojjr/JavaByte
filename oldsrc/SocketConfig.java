package com.kirchnersolutions.database;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;


@Configuration
public class SocketConfig {

    /*
    @Bean
    public TcpNetClientConnectionFactory cf() {
        TcpNetClientConnectionFactory cf = new TcpNetClientConnectionFactory("localhost",
                Integer.parseInt(System.getProperty("4444")));
        cf.setSingleUse(true);
        return cf;
    }

    @Bean
    public ThreadAffinityClientConnectionFactory tacf() {

        return new ThreadAffinityClientConnectionFactory(cf());
    }

    @Bean
    @ServiceActivator(inputChannel = "out")
    public TcpOutboundGateway outGate() {
        TcpOutboundGateway outGate = new TcpOutboundGateway();
        outGate.setConnectionFactory(tacf());
        outGate.setReplyChannelName("toString");
        return outGate;
    }

     */


}
