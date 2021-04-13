package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.results.Result;
import reactor.core.publisher.Mono;

import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        ReactiveCommandHandler<Context> commandHandler = ReactiveCommandHandler.<Context>builder()
            .withModule(Module.class)
            .build();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            Mono<Result> result = commandHandler.execute(scanner.nextLine(), new Context());
            System.out.println(result.block());
        }
    }
}
