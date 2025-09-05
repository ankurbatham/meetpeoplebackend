package com.meetThePeople;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetThePeopleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetThePeopleApplication.class, args);
	}

}
