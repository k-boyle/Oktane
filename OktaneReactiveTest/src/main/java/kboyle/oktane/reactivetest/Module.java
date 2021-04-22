package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.processor.OktaneModule;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

@OktaneModule
public class Module extends ReactiveModuleBase<Context> {
    @Aliases("echo")
    public Mono<CommandResult> echo(@Remainder String input) {
        return message("echo: " + input);
    }

    @Aliases("add")
    public Mono<CommandResult> add(int a, int b) {
        return message(a + b + "");
    }

    @Aliases("remainder")
    public Mono<CommandResult> remainder(String a, @Remainder String b) {
        return message("a: " + a + " b: " + b);
    }

    @Aliases("throw1")
    public Mono<CommandResult> throw0() {
        throw new RuntimeException();
    }

    @Aliases("throw2")
    public Mono<CommandResult> throw0(Exception ex) {
        return nop();
    }

    @Aliases("throw3")
    @Require(precondition = ThrowingPrecondition.class)
    public Mono<CommandResult> throw1() {
        return nop();
    }

    @Aliases("error")
    public Mono<CommandResult> error() {
        return Mono.error(new RuntimeException());
    }

    @Aliases("overload")
    public Mono<CommandResult> overload() {
        return message("1");
    }

    @Aliases("overload")
    @Require(precondition = LongPrecondition.class)
    public Mono<CommandResult> overload(int a) {
        return message("2");
    }

    @Aliases("overload")
    @Require(precondition = LongPrecondition.class)
    public Mono<CommandResult> overload(int a, String b) {
        return message("3");
    }

    private Mono<CommandResult> privateM() {
        return nop();
    }

    @Aliases("nested")
    @OktaneModule
    public static class Nested extends ReactiveModuleBase<Context> {
        public Mono<CommandResult> root() {
            return nop();
        }

        @Aliases("notroot")
        public Mono<CommandResult> notRoot() {
            return nop();
        }

        @OktaneModule
        public static class Nested2 extends ReactiveModuleBase<Context> {
            @Aliases("nested")
            public Mono<CommandResult> nestedNested() {
                return nop();
            }
        }
    }
}
