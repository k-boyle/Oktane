[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=k-boyle_Oktane&metric=alert_status)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=k-boyle_Oktane&metric=ncloc)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane)

Oktane is a high performance, highly configurable Java command framework used to map strings to methods, and execute them.

Inspired by [Qmmands](https://github.com/quahu/qmmands).

Oktane is hosted on [Sonatype](https://oss.sonatype.org/content/repositories/snapshots), find the latest snapshot release [here](https://oss.sonatype.org/#nexus-search;quick~oktane).

**Performance Benchmarks**

| Benchmark                   | Mode | Cnt  | Score     |  Error  | Units   |
| --------------------------- | ---- | ---- | --------- | ------- | ------- |
| commandFiveParameters       | avgt |  5   | 269.236 ± |  4.315  | ns/op   |
| commandIntParameter         | avgt |  5   | 172.706 ± |  1.491  | ns/op   |
| commandNoParameters         | avgt |  5   | 103.287 ± |  2.052  | ns/op   |
| commandNotFound             | avgt |  5   |  12.609 ± |  0.119  | ns/op   |
| commandOneParameter         | avgt |  5   | 166.109 ± |  3.101  | ns/op   |
| commandRemainderParameter   | avgt |  5   | 147.749 ± | 15.468  | ns/op   |


# Usage #

**Context Creation**

Your command context is a standard pojo used to pass contextual data into your commands. BeanProvider is a service container with the interface providing a default implementation.
```java
public class OktaneCommandContext extends CommandContext {
    private final String user;
    
    public OktaneCommandContext(String user, BeanProvider beanProvider) {
        super(beanProvider);
        this.user = user;
    }
    
    public String user() {
        return user;
    }
}
```

**Module Creation**

To define a class as a module the class just needs to extend CommandModuleBase&lt;T&gt;. Any methods in the class that are annotated with the CommandDescription
annotation and return Mono&lt;CommandResult&gt; 
```java
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    @CommandDescription(aliases = {"echo", "e"})
    public Mono<CommandResult> pingPong(@ParameterDescription(remainder = true) String input) {
        return reply(context().user() + " said: " + input);
    }
}
```

**CommandHandler Creation**

The command handler is your interface for interacting with your commands.
```java
public CommandHandler<OktaneCommandContext> commandHandler() {
    return CommandHandler.builderForContext(OktaneCommandContext.class)
        .withModule(OktaneCommandModule.class)
        .build();
}    
```

**Command Invocation**

To invoke a command you simply call to call `excute` on the command handler, pass it your command context, and the string input to parse.
```java
OktaneCommandContext context = new OktaneCommandContext("Kieran", BeanProvider.get());
Mono<Result> result = commandHandlder.execute("echo Oktane is really cool :)", context);
```

**Granular Configuration**

Modules and commands can be configured a fair amount.
```java
@ModuleDescription(
    name = "My Module",                                 // Can be used in help displays, all the modules and commands can be accessed via
                                                        // CommandHandler#module, and CommandHandler#commands 
    description = "This is a command module",           // Can be used in help displays
    groups = {"a", "b"},                                // Used to group commands together,
                                                        // commands inside a group must have the group prefix to execute, e.g. "a echo"
    preconditions = RequireOwnerPrecondition.class,     // The preconditions to run to determine whether a module is executable or not
    singleton = true,                                   // Makes the module a singleton (transient by default)
    synchronised = true                                 // Makes it so that all commands in the module are synchronised on a shared lock
)
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    @CommandDescription(
        name = "Echo Command",                          // Can be used in help displays
        description = "Echos input",                    // Can be used in help displays
        aliases = {"echo", "e"},                        // Defines the different aliases that can invoke the command
        preconditions = ChannelPrecondition.class,      // The preconditions to run to determine whether the command is executable
        synchronised = true                             // Makes it so that the command is locally synchronised (public Mono<CommandResult> synchronised ...)
    )
    public Mono<CommandResult> pingPong(
            @ParameterDescription(
                name = "User Input",                    // Can be used in help displays       
                description = "The input to echo",      // Can be used in help displays
                remainder = true                        // Denotes the parameter as a remainder, so all the remaining text left to parse
                                                        // will be passed into this parameter. There can only be one remainder, and it
                                                        // must be the last parameter
            ) 
            String input) {
        return reply(context().user() + " said: " + input);
    }
}
```

**Type Parsing**

Oktane supports parsing all the primitive types, see PrimitiveTypeParser, and allowing a user to define their own.
Type parsers are added during the CommandHandler building stage using the withTypeParser method.
```java
public class PrimitiveTypeParser<User> implements TypeParser<User> {
    @Override
    public TypeParserResult parse(CommandContext context, String input) {
        User user = User.parseFromInput(input);
        if (user != null) {
            return success(user);
        }
        
        return failure("Failed to parse %s as a valid user", input);
    }
} 
```

**Preconditions**

Preconditions can be used to add permissions to your commands. Preconditions will be fetched from the BeanProvider.
```java
public class RequireOwnerPrecondition implements Precondition {
    public PreconditionResult run(CommandContext c) {
        OktaneCommandContext context = (OktaneCommandContext) c;
        if (context.user().equals("Kieran")) {
            return success();
        }
        
        return failure("Only Kieran can execute this command.");
    }
}
```

**Dependency Injection**

Beans can be injected into module using the BeanProvider, any constructor arguments will be passed into the module on instantiation, the CommandHandler 
will inject itself and does not need to be added to a provider.
```java
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    private final CommandHandler commandHandler;
    
    public OktaneCommandModule(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
    
    @CommandDescription(aliases = {"echo", "e"})
    public Mono<CommandResult> pingPong(@ParameterDescription(remainder = true) String input) {
        return reply(context().user() + " said: " + input);
    }
}
```

**Custom Argument Parsing**

The Oktane ArgumentParser can be overridden using the withArgumentParser method, it's however not recommended.

**How Oktane Works**

Oktane uses a few sprinkles of magic, when building a CommandHandler Oktane uses reflection to get all the command methods, then during run time
classes are generated, compiled, and loaded in that are used to invoke the commands and handle any module instantiation.
