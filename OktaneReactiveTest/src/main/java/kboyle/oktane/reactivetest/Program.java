package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.results.Result;
import reactor.core.publisher.Mono;

import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        ReactiveCommandHandler<Context> commandHandler = ReactiveCommandHandler.<Context>builder()
            .withTypeParser(Exception.class, new ThrowingTypeParser())
            .withModule(Module.class)
            .build();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            Mono<Result> result = commandHandler.execute(scanner.nextLine(), new Context());
            long start = System.nanoTime();
            result.subscribe(r -> printResult(start, r), ex -> printResult(start, ex));
        }
    }

    private static void printResult(long start, Object obj) {
        System.out.printf("took: %dns, result: %s%n", System.nanoTime() - start, obj);
    }
}
