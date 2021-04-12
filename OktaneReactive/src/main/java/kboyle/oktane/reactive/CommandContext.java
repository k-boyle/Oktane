package kboyle.oktane.reactive;

public abstract class CommandContext {
    private final BeanProvider beanProvider;

    protected CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    protected CommandContext() {
        this(BeanProvider.empty());
    }

    public BeanProvider beanProvider() {
        return beanProvider;
    }
}
