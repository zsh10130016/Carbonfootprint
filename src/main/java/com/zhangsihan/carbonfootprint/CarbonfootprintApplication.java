package com.zhangsihan.carbonfootprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarbonfootprintApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarbonfootprintApplication.class, args);
	}

}
