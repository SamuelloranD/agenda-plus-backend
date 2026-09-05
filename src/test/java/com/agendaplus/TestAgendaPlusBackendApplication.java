package com.agendaplus;

import org.springframework.boot.SpringApplication;

public class TestAgendaPlusBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgendaPlusBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
