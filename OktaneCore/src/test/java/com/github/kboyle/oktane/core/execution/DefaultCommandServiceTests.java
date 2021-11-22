package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.OktaneTestConfiguration;
import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.result.Result;
import com.github.kboyle.oktane.core.result.command.CommandExceptionResult;
import com.github.kboyle.oktane.core.result.command.CommandTextResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;
import java.util.stream.Stream;

@SpringBootTest(classes = { DefaultCommandService.class, OktaneTestConfiguration.class })
class DefaultCommandServiceTests {
    @Autowired
    DefaultCommandService commandService;

    @ParameterizedTest(name = "Input: {0}, Expected: {1}")
    @MethodSource("testDefaultCommandServiceSource")
    void testDefaultCommandService(String input, Function<CommandService, Result> expectedResultFunction) {
        var actualResult = commandService.execute(new CommandContext(), input);
        var expectedResult = expectedResultFunction.apply(commandService);
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
                textResult("ping", "pong")
            ),
            Arguments.of(
                "p",
                textResult("ping", "pong")
            ),
            Arguments.of(
                "add 10 20",
                textResult("add", "add 2: 30")
            ),
            Arguments.of(
                "addvargs 10",
                textResult("addVargs", "add vargs: 10")
            ),
            Arguments.of(
                "addvargs 10 20 30",
                textResult("addVargs", "add vargs: 60")
            ),
            Arguments.of(
                "echo a",
                textResult("echo", "a")
            ),
            Arguments.of(
                "echo a b",
                textResult("echo", "a b")
            ),
            Arguments.of(
                "optionalstring a",
                textResult("optionalstring", "a")
            ),
            Arguments.of(
                "optionalstring",
                textResult("optionalstring", null)
            ),
            Arguments.of(
                "default a",
                textResult("default0", "a")
            ),
            Arguments.of(
                "default",
                textResult("default0", "pong")
            ),
            Arguments.of(
                "optionaldefault a b",
                textResult("optionaldefault", "a: a, b: b")
            ),
            Arguments.of(
                "optionaldefault a",
                textResult("optionaldefault", "a: a, b: this is the default")
            ),
            Arguments.of(
                "optionaldefault",
                textResult("optionaldefault", "a: null, b: this is the default")
            ),
            Arguments.of(
                "optionalint 10",
                textResult("optionalint", "x: 10")
            ),
            Arguments.of(
                "optionalint",
                textResult("optionalint", "x: 0")
            ),
            Arguments.of(
                "optionalvargs a",
                textResult("optionalvargs", "args: a")
            ),
            Arguments.of(
                "optionalvargs a b c",
                textResult("optionalvargs", "args: a, b, c")
            ),
            Arguments.of(
                "optionalvargs",
                textResult("optionalvargs", "args: null")
            ),
            Arguments.of(
                "defaultvargs a",
                textResult("defaultvargs", "args: a")
            ),
            Arguments.of(
                "defaultvargs a b c",
                textResult("defaultvargs", "args: a, b, c")
            ),
            Arguments.of(
                "defaultvargs",
                textResult("defaultvargs", "args: default")
            ),
            Arguments.of(
                "commandthrow",
                exceptionResult("commandthrow", new RuntimeException("oh no!"))
            ),
            Arguments.of(
                "nested",
                textResult("group", "group command")
            ),
            Arguments.of(
                "nested ping",
                textResult("nestedPing", "nested pong")
            ),
            Arguments.of(
                "enum one",
                textResult("enum0", "ONE")
            ),
            Arguments.of(
                "enum TWO",
                textResult("enum0", "TWO")
            ),
            Arguments.of(
                "enum 0",
                textResult("enum0", "ONE")
            ),
            Arguments.of(
                "greedyint 1 2 3 4 a",
                textResult("greedyInt", "a: 1, sum: 9, str: a")
            ),
            Arguments.of(
                "greedyint 1 2 a",
                textResult("greedyInt", "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 a",
                textResult("optionalGreedyInt", "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "optionalgreedyint 1 2",
                textResult("optionalGreedyInt", "a: 1, sum: 2, str: null")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 3",
                textResult("optionalGreedyInt", "a: 1, sum: 2, str: 3")
            ),
            Arguments.of(
                "optionalgreedyint 1 2 3 4 a",
                textResult("optionalGreedyInt", "a: 1, sum: 9, str: a")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 a",
                textResult("defaultGreedyInt", "a: 1, sum: 2, str: a")
            ),
            Arguments.of(
                "defaultgreedyint 1",
                textResult("defaultGreedyInt", "a: 1, sum: 10, str: default")
            ),
            Arguments.of(
                "defaultgreedyint 1 2",
                textResult("defaultGreedyInt", "a: 1, sum: 2, str: default")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 3",
                textResult("defaultGreedyInt", "a: 1, sum: 2, str: 3")
            ),
            Arguments.of(
                "defaultgreedyint 1 2 3 4 a",
                textResult("defaultGreedyInt", "a: 1, sum: 9, str: a")
            )
        );
    }

    private static Command getCommand(CommandService commandService, String name) {
        return commandService.commands()
            .filter(command -> name.equals(command.name().get()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find a command with name " + name));
    }

    private static Function<CommandService, Result> textResult(String commandName, String text) {
        return service -> new CommandTextResult(getCommand(service, commandName), text);
    }

    private static Function<CommandService, Result> exceptionResult(String commandName, Exception ex) {
        return service -> new CommandExceptionResult(getCommand(service, commandName), ex);
    }
}
