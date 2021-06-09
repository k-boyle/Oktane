package kboyle.oktane.example;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.prefix.CharPrefix;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.core.results.search.CommandMatchFailedResult;
import kboyle.oktane.example.modules.PingModule;
import kboyle.oktane.example.results.KillAppCommandResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Program {
    public static void main(String[] args) {
        var commandHandler = CommandHandler.<ExampleCommandContext>builder()
            .withModules(PingModule.class)
            .withModule(moduleBuilder -> {
                moduleBuilder.groups.add("builder");
                moduleBuilder.withName("Builder Module")
                    .withCommand(commandBuilder -> {
                        commandBuilder.aliases.add("command");
                        commandBuilder.withName("Builder Command")
                            .withParameter(parameterBuilder -> {
                                parameterBuilder.withName("Builder Parameter")
                                    .withType(int.class);
                            })
                            .withCallback((ctx, beans, parameters) -> new CommandMessageResult(ctx.command(), String.valueOf(parameters[0])).mono());
                    });
            })
            .withPrefixHandler(context -> Mono.just(List.of(new CharPrefix('!'))))
            .build();

        var beanProvider = BeanProvider.simple()
            .add(Random.class, new Random());

        var scanner = new Scanner(System.in);
        while (true) {
            var result = commandHandler.execute(scanner.nextLine(), new ExampleCommandContext(beanProvider)).block();
            if (result instanceof CommandMessageResult message) {
                System.out.println(message.message());
            } else if (result instanceof KillAppCommandResult) {
                System.out.println("Kill app...");
                break;
            } else if (result instanceof CommandMatchFailedResult commandMatchFailedResult) {
                var results = commandMatchFailedResult.failedResults().stream()
                    .map(Result::toString)
                    .collect(Collectors.joining("\n"));
                System.out.println(results);
            } else {
                System.out.println("Got result: " + result);
            }
        }
    }
}
