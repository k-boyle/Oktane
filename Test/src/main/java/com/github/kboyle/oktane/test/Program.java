package com.github.kboyle.oktane.test;

import com.github.kboyle.oktane.core.annotation.OtkaneApplication;
import com.github.kboyle.oktane.core.command.CommandModulesFactory;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.CommandService;
import com.github.kboyle.oktane.core.parsing.TypeParserProvider;
import com.github.kboyle.oktane.test.modules.TestModule;
import lombok.Data;

import java.util.Scanner;

// todo remove varargs builder methods?
@OtkaneApplication
public class Program {
    public static void main(String[] args) {
        var service = CommandService.builder()
            .typeParserProvider(TypeParserProvider.defaults())
            .modulesFactory(CommandModulesFactory.classes(TestModule.class))
            .build();

        var scanner = new Scanner(System.in);
        while (true) {
            var input = scanner.nextLine();
            var result = service.execute(new CommandContext(), input);
            System.out.println(result);
        }
    }

    @Data
    private static class Test {
        private final int i;
    }
}
