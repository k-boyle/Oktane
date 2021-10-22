package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.*;
import com.github.kboyle.oktane.core.parsing.TypeParserProvider;
import com.github.kboyle.oktane.core.result.Result;
import com.github.kboyle.oktane.core.result.command.CommandExceptionResult;
import com.github.kboyle.oktane.core.result.command.CommandTextResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultCommandServiceTests {
    private static final DefaultCommandService COMMAND_SERVICE = new DefaultCommandService.Builder()
        .typeParserProvider(TypeParserProvider.defaults())
        .modulesFactory(CommandModulesFactory.classes(TestCommandModule.class))
        .build();

    @ParameterizedTest(name = "Input: {0}, Expected: {1}")
    @MethodSource("testDefaultCommandServiceSource")
    void testDefaultCommandService(String input, Result expectedResult) {
        var actualResult = COMMAND_SERVICE.execute(new CommandContext(), input);
        if (!(actualResult instanceof CommandExceptionResult actualException)) {
            Assertions.assertEquals(expectedResult, actualResult);
        } else {
            actualException.exception().printStackTrace();
            Assertions.assertEquals(expectedResult.getClass(), actualException.getClass());
            Assertions.assertEquals(actualException.exception().getClass(), actualException.exception().getClass());
        }
    }

    private static Stream<Arguments> testDefaultCommandServiceSource() {
        return Stream.of(
            Arguments.of(
                "ping",
                new CommandTextResult(getCommand("ping"), "pong")
            ),
            Arguments.of(
                "p",
                new CommandTextResult(getCommand("ping"), "pong")
            ),
            Arguments.of(
                "add 10 20",
                new CommandTextResult(getCommand("add"), "add 2: 30")
            ),
            Arguments.of(
                "addvargs 10",
                new CommandTextResult(getCommand("addVargs"), "add vargs: 10")
            ),
            Arguments.of(
                "addvargs 10 20 30",
                new CommandTextResult(getCommand("addVargs"), "add vargs: 60")
            ),
            Arguments.of(
                "echo a",
                new CommandTextResult(getCommand("echo"), "a")
            ),
            Arguments.of(
                "echo a b",
                new CommandTextResult(getCommand("echo"), "a b")
            ),
            Arguments.of(
                "optionalstring a",
                new CommandTextResult(getCommand("optionalstring"), "a")
            ),
            Arguments.of(
                "optionalstring",
                new CommandTextResult(getCommand("optionalstring"), null)
            ),
            Arguments.of(
                "default a",
                new CommandTextResult(getCommand("default0"), "a")
            ),
            Arguments.of(
                "default",
                new CommandTextResult(getCommand("default0"), "pong")
            ),
            Arguments.of(
                "optionaldefault a b",
                new CommandTextResult(getCommand("optionaldefault"), "a: a, b: b")
            ),
            Arguments.of(
                "optionaldefault a",
                new CommandTextResult(getCommand("optionaldefault"), "a: a, b: this is the default")
            ),
            Arguments.of(
                "optionaldefault",
                new CommandTextResult(getCommand("optionaldefault"), "a: null, b: this is the default")
            ),
            Arguments.of(
                "optionalint 10",
                new CommandTextResult(getCommand("optionalint"), "x: 10")
            ),
            Arguments.of(
                "optionalint",
                new CommandTextResult(getCommand("optionalint"), "x: 0")
            ),
            Arguments.of(
                "optionalvargs a",
                new CommandTextResult(getCommand("optionalvargs"), "args: a")
            ),
            Arguments.of(
                "optionalvargs a b c",
                new CommandTextResult(getCommand("optionalvargs"), "args: a, b, c")
            ),
            Arguments.of(
                "optionalvargs",
                new CommandTextResult(getCommand("optionalvargs"), "args: null")
            ),
            Arguments.of(
                "defaultvargs a",
                new CommandTextResult(getCommand("defaultvargs"), "args: a")
            ),
            Arguments.of(
                "defaultvargs a b c",
                new CommandTextResult(getCommand("defaultvargs"), "args: a, b, c")
            ),
            Arguments.of(
                "defaultvargs",
                new CommandTextResult(getCommand("defaultvargs"), "args: default")
            ),
            Arguments.of(
                "commandthrow",
                new CommandExceptionResult(getCommand("commandthrow"), new RuntimeException("oh no!"))
            ),
            Arguments.of(
                "nested",
                new CommandTextResult(getCommand("group"), "group command")
            ),
            Arguments.of(
                "nested ping",
                new CommandTextResult(getCommand("nestedPing"), "nested pong")
            ),
            Arguments.of(
                "enum one",
                new CommandTextResult(getCommand("enum0"), "ONE")
            ),
            Arguments.of(
                "enum TWO",
                new CommandTextResult(getCommand("enum0"), "TWO")
            ),
            Arguments.of(
                "enum 0",
                new CommandTextResult(getCommand("enum0"), "ONE")
            ),
            Arguments.of(
                "greedyint 1 2 3 4 a",
                new CommandTextResult(getCommand("greedyInt"), "a: 1, sum: 9, str: a")
            ),
            Arguments.of(
                "greedyint 1 2 a",
                new CommandTextResult(getCommand("greedyInt"), "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 a",
                new CommandTextResult(getCommand("optionalGreedyInt"), "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "optionalgreedyint 1 2",
                new CommandTextResult(getCommand("optionalGreedyInt"), "a: 1, sum: 2, str: null")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 3",
                new CommandTextResult(getCommand("optionalGreedyInt"), "a: 1, sum: 2, str: 3")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 3 4 a",
                new CommandTextResult(getCommand("optionalGreedyInt"), "a: 1, sum: 9, str: a")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 a",
                new CommandTextResult(getCommand("defaultGreedyInt"), "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "defaultgreedyint 1",
                new CommandTextResult(getCommand("defaultGreedyInt"), "a: 1, sum: 10, str: default")
            ),
            Arguments.of(
                "defaultgreedyint 1 2",
                new CommandTextResult(getCommand("defaultGreedyInt"), "a: 1, sum: 2, str: default")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 3",
                new CommandTextResult(getCommand("defaultGreedyInt"), "a: 1, sum: 2, str: 3")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 3 4 a",
                new CommandTextResult(getCommand("defaultGreedyInt"), "a: 1, sum: 9, str: a")
            )
        );
    }

    private static Command getCommand(String name) {
        return COMMAND_SERVICE.commands()
            .filter(command -> name.equals(command.name().get()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find a command with name " + name));
    }

    private static String createSignature(Command command) {
        var parametersString = command.parameters().stream()
            .map(CommandParameter::name)
            .<String>mapMulti(Optional::ifPresent)
            .collect(Collectors.joining());
        return command.name().get() + parametersString;
    }
}
