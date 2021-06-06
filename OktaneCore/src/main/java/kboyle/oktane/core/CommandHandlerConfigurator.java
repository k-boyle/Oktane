package kboyle.oktane.core;

public interface CommandHandlerConfigurator {
    void apply(CommandHandler.Builder commandHandler);
}
