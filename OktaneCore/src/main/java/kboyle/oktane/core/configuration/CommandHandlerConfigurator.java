package kboyle.oktane.core.configuration;

import kboyle.oktane.core.CommandHandler;

/**
 * Represents a configurator for the {@link CommandHandler}.
 * Intended to be used internally by Oktane and not externally implemented.
 */
@FunctionalInterface
public interface CommandHandlerConfigurator {
    /**
     * Applies a configuration to a {@link CommandHandler.Builder}.
     *
     * @param commandHandler The {@link CommandHandler.Builder} to apply the configuration to.
     */
    void apply(CommandHandler.Builder<?> commandHandler);

    /**
     * The priority to execute the configurator in, applied in ascending order
     */
    default int priority() {
        return -1;
    }
}
