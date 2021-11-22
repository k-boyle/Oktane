package com.github.kboyle.oktane.test;

import com.github.kboyle.oktane.core.configuration.OktaneApplication;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;
import java.util.concurrent.Executor;

// todo remove varargs builder methods?
@OktaneApplication
@Configuration
public class Program {
    private final CommandService commandService;
    private final Executor executor;

    @Autowired
    public Program(CommandService commandService, Executor executor) {
        this.commandService = commandService;
        this.executor = executor;
    }

    public static void main(String[] args) {
        SpringApplication.run(Program.class);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> commandLoop() {
        return ready -> {
            var scanner = new Scanner(System.in);
            while(true) {
                var input = scanner.nextLine();
                var result = commandService.execute(new CommandContext(), input);
                System.out.println(result);
                executor.execute(this::commandLoop);
            }
        };
    }
}
