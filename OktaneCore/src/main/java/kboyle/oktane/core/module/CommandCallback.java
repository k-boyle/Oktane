package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.command.CommandResult;

@FunctionalInterface
public interface CommandCallback {
     CommandResult execute(CommandContext context, Object[] services, Object[] parameters);
}
