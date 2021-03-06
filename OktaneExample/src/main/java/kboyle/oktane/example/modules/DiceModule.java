package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aliases("roll")
public class DiceModule extends CommandModuleBase<ExampleCommandContext> {
    private final Random random;

    public DiceModule(Random random) {
        this.random = random;
    }

    public CommandResult roll() {
        return message("rolled: " + random.nextInt(6) + 1);
    }

    public CommandResult roll(int number, int sides) {
        String results = IntStream.range(0, number).map(i -> random.nextInt(sides) + 1)
            .boxed()
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        return message("rolled: " + results);
    }
}
