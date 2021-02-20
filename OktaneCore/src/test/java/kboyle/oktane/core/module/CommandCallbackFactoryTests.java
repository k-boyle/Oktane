package kboyle.oktane.core.module;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.modules.TestModuleOne;
import kboyle.oktane.core.modules.TestModuleTwo;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.core.results.command.CommandSuccessfulResult.NOP;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandCallbackFactoryTests {
    @Test
    public void testCorrectCommandCreated() {
        CommandCallbackFactory factory = new CommandCallbackFactory();
        CommandCallback commandCallback = factory.createCommandCallback(
            TestModuleOne.class,
            false,
            null,
            false,
            TestModuleOne.get("a"),
            BeanProvider.get()
        );
        CommandResult result = commandCallback.execute(new TestCommandContext(), new Object[0], new Object[0]);
        assertTrue(result instanceof NOP);
    }

    @Test
    public void testCorrectResultReturned() {
        CommandCallbackFactory factory = new CommandCallbackFactory();
        CommandCallback commandCallback = factory.createCommandCallback(
            TestModuleOne.class,
            false,
            null,
            false,
            TestModuleOne.get("b"),
            BeanProvider.get()
        );
        CommandResult result = commandCallback.execute(new TestCommandContext(), new Object[0], new Object[0]);
        assertTrue(result instanceof CommandMessageResult msg && msg.message().equals("hi"));
    }

    @Test
    public void testSingletonThrowsWhenNotInProvider() {
        CommandCallbackFactory factory = new CommandCallbackFactory();
        Assertions.assertThrows(NullPointerException.class, () -> factory.createCommandCallback(
            TestModuleOne.class,
            true,
            null,
            false,
            TestModuleOne.get("b"),
            BeanProvider.get()
        ));
    }

    // this is hard to verify
    @Test
    public void testCompiles() {
        CommandCallbackFactory factory = new CommandCallbackFactory();
        Assertions.assertDoesNotThrow(() ->
            factory.createCommandCallback(
                TestModuleOne.class,
                false,
                new Object(),
                true,
                TestModuleTwo.get("a"),
                BeanProvider.get()
            )
        );
    }
}
