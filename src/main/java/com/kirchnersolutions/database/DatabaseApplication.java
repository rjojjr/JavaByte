package com.kirchnersolutions.database;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.Configuration.ThreadConfig;
import com.kirchnersolutions.database.Servers.HTTP.StompConfig;
import com.kirchnersolutions.database.sessions.HttpSessionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.ApplicationScope;

//@ComponentScan({"com.kirchnersolutions.utilities", "com.kirchnersolutions.database.Configuration", "com.kirchnersolutions.database.dev", "com.kirchnersolutions.database.Controllers", "com.kirchnersolutions.database.core" +
//	"", "com.kirchnersolutions.database.objects","com.kirchnersolutions.database.core.tables",  "com.kirchnersolutions.database.Servers.HTTP", /* "com.kirchnersolutions.database.Servers.socket", */"com.kirchnersolutions.database.Servers.HTTP.beans",
//"com.kirchnersolutions.database.sessions", "com.kirchnersolutions.utilities.SerialService", "com.kirchnersolutions.database.exceptions"})
@SpringBootApplication
public class DatabaseApplication {

	public static void main(String[] args) throws  Exception{
		//ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class, StompConfig.class, HttpSessionConfig.class, MainConfig.class);
		SpringApplication.run(DatabaseApplication.class, args);
	}

	/*
	@Bean
	//@ApplicationScope
	@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
	public SocketService socketService() throws Exception{
		return new SocketService();
	}

	 */


}
