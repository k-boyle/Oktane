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
        return monoMessage("echo: " + input);
    }

    @Aliases("add")
    public Mono<CommandResult> add(int a, int b) {
        return monoMessage(a + b + "");
    }

    @Aliases("remainder")
    public Mono<CommandResult> remainder(String a, @Remainder String b) {
        return monoMessage("a: " + a + " b: " + b);
    }

    @Aliases("throw1")
    public Mono<CommandResult> throw0() {
        throw new RuntimeException();
    }

    @Aliases("throw2")
    public Mono<CommandResult> throw0(Exception ex) {
        return monoNop();
    }

    @Aliases("throw3")
    @Require(precondition = ThrowingPrecondition.class)
    public Mono<CommandResult> throw1() {
        return monoNop();
    }

    @Aliases("error")
    public Mono<CommandResult> error() {
        return Mono.error(new RuntimeException());
    }

    @Aliases("overload")
    public Mono<CommandResult> overload() {
        return monoMessage("1");
    }

    @Aliases("overload")
    @Require(precondition = LongPrecondition.class)
    public Mono<CommandResult> overload(int a) {
        return monoMessage("2");
    }

    @Aliases("overload")
    @Require(precondition = LongPrecondition.class)
    public Mono<CommandResult> overload(int a, String b) {
        return monoMessage("3");
    }

    @Aliases("sync")
    public CommandResult sync() {
        return nop();
    }

    private Mono<CommandResult> privateM() {
        return monoNop();
    }

    @Aliases("nested")
    @OktaneModule
    public static class Nested extends ReactiveModuleBase<Context> {
        public Mono<CommandResult> root() {
            return monoNop();
        }

        @Aliases("notroot")
        public Mono<CommandResult> notRoot() {
            return monoNop();
        }

        @OktaneModule
        public static class Nested2 extends ReactiveModuleBase<Context> {
            @Aliases("nested")
            public Mono<CommandResult> nestedNested() {
                return monoNop();
            }
        }
    }
}
