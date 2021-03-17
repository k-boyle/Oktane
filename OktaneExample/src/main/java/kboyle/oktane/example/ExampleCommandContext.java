package kboyle.oktane.example;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;

public class ExampleCommandContext extends CommandContext {
    public ExampleCommandContext(BeanProvider.Simple beanProvider) {
        super(beanProvider);
    }
}
