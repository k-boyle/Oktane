package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;

public enum ModuleBaseProxy {
    ;

    public static <CONTEXT extends CommandContext> void setContext(ModuleBase<CONTEXT> module, CONTEXT context) {
        module.setContext(context);
    }
}
