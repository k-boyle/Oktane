package com.github.kboyle.oktane.test;

import com.github.kboyle.oktane.core.command.*;
import com.github.kboyle.oktane.core.configuration.OktaneApplication;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.CommandService;
import com.github.kboyle.oktane.core.result.command.CommandTextResult;
import com.github.kboyle.oktane.test.modules.TestModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

// todo remove varargs builder methods?
@OktaneApplication
public class Program {
    public static void main(String[] args) {
        SpringApplication.run(Program.class);
    }

    @Bean
    public CommandModule.Builder builder() {
        return CommandModule.builder()
            .command(
                Command.builder()
                    .callback(ctx -> new CommandTextResult(ctx.command(), "callback"))
                    .alias("something")
            );
    }

    @Bean
    public CommandModulesFactory commandModulesFactory() {
        return CommandModulesFactory.classes(TestModule.class);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> commandLoop(CommandService commandService) {
        return ready -> {
            var scanner = new Scanner(System.in);
            while (true) {
                var input = scanner.nextLine();
                var result = commandService.execute(new CommandContext(), input);
                System.out.println(result);
            }
        };
    }
}
