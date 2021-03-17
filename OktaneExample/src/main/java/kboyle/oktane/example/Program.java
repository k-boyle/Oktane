package kboyle.oktane.example;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.example.modules.*;
import kboyle.oktane.example.results.KillAppCommandResult;

import java.util.Random;
import java.util.Scanner;

// TODO update README
public class Program {
    public static void main(String[] args) {
        CommandHandler<ExampleCommandContext> commandHandler = CommandHandler.<ExampleCommandContext>builder()
            .withModule(PingModule.class)
            .withModule(GroupModule.class)
            .withModule(ErrorModule.class)
            .withModule(HelpModule.class)
            .withModule(DiceModule.class)
            .build();

        BeanProvider.Simple beanProvider = BeanProvider.simple()
            .add(Random.class, new Random());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            Result result = commandHandler.execute(scanner.nextLine(), new ExampleCommandContext(beanProvider));
            if (result instanceof FailedResult failure) {
                System.out.println("Failed due to: " + failure.reason());
            } else if (result instanceof CommandMessageResult message) {
                System.out.println(message.message());
            } else if (result instanceof KillAppCommandResult) {
                System.out.println("Kill app...");
                break;
            } else {
                System.out.println("Got result: " + result);
            }
        }
    }
}
