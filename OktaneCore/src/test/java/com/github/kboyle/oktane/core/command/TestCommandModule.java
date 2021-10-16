package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.annotation.*;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.result.command.CommandResult;

import java.util.Arrays;
import java.util.stream.IntStream;

// todo map optional with default separately to no default
public class TestCommandModule extends ModuleBase<CommandContext> {
    @Aliases({"ping", "p"})
    public CommandResult ping() {
        return text("pong");
    }

    @Aliases("add")
    @Priority(1)
    public CommandResult add(Integer a, int b) {
        return text("add 2: %d", a + b);
    }

    @Aliases("addvargs")
    public CommandResult addVargs(int... args) {
        return text("add vargs: %d", Arrays.stream(args).sum());
    }

    @Aliases("echo")
    public CommandResult echo(@Remainder String echo) {
        return text(echo);
    }

    @Aliases("optionalstring")
    public CommandResult optionalstring(@Optional String input) {
        return text(input);
    }

    @Aliases("default")
    public CommandResult default0(@Default("pong") String input) {
        return text(input);
    }

    @Aliases("optionaldefault")
    public CommandResult optionaldefault(@Optional String a, @Default("this is the default") String b) {
        return text("a: %s, b: %s", a, b);
    }

    @Aliases("optionalint")
    public CommandResult optionalint(@Optional int x) {
        return text("x: %d", x);
    }

    @Aliases("optionalvargs")
    public CommandResult optionalvargs(@Optional String... args) {
        return text("args: %s", String.join(", ", args));
    }

    @Aliases("defaultvargs")
    public CommandResult defaultvargs(@Default("default") String... args) {
        return text("args: %s", String.join(", ", args));
    }

    @Aliases("commandthrow")
    public CommandResult commandthrow() {
        throw new RuntimeException("oh no!");
    }

    @Aliases("enum")
    public CommandResult enum0(TestEnum value) {
        return text(value.name());
    }

    @Aliases("greedyint")
    public CommandResult greedyInt(int a, @Greedy int[] greedyInts, String str) {
        return text("a: %d, sum: %d, str: %s", a, IntStream.of(greedyInts).sum(), str);
    }

    @Aliases("optionalgreedyint")
    public CommandResult optionalGreedyInt(int a, @Optional @Greedy int[] greedyInts, @Optional String str) {
        return text("a: %d, sum: %d, str: %s", a, IntStream.of(greedyInts).sum(), str);
    }

    @Aliases("defaultgreedyint")
    public CommandResult defaultGreedyInt(int a, @Default("10") @Greedy int[] greedyInts, @Default("default") String str) {
        return text("a: %d, sum: %d, str: %s", a, IntStream.of(greedyInts).sum(), str);
    }

    @Aliases("nested")
    public static class Nested extends TestCommandModule {
        public CommandResult group() {
            return text("group command");
        }

        @Aliases("ping")
        public CommandResult nestedPing() {
            return text("nested pong");
        }
    }

    public enum TestEnum {
        ONE,
        TWO
    }
}
