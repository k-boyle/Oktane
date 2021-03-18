[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=k-boyle_Oktane&metric=alert_status)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=k-boyle_Oktane&metric=ncloc)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane)

Oktane is a high performance, highly configurable Java command framework used to map strings to methods, and execute them.

Inspired by [Qmmands](https://github.com/quahu/qmmands).

Oktane is hosted on [Sonatype](https://oss.sonatype.org/content/repositories/snapshots), find the latest snapshot release [here](https://oss.sonatype.org/#nexus-search;quick~oktane).

# Performance Benchmarks #

**Benchmarks ran on a Ryzen 5600x @ 4.6GHz**

| Benchmark                   | Mode | Cnt  | Score    |  Error    | Units   |
| --------------------------- | ---- | ---- | -------- | --------- | ------- |
| commandFiveParameters       | avgt |  5   | 126.187  | ± 0.489   | ns/op   |
| commandIntParameter         | avgt |  5   | 59.992   | ± 0.283   | ns/op   |
| commandNoParameters         | avgt |  5   | 23.304   | ± 0.202   | ns/op   |
| commandNotFound             | avgt |  5   | 8.817    | ± 0.084   | ns/op   |
| commandOneParameter         | avgt |  5   | 50.439   | ± 0.177   | ns/op   |
| commandRemainderParameter   | avgt |  5   | 50.201   | ± 0.224   | ns/op   |

**Benchmarks ran on a Ryzen 2700x @ 3.6GHZ**

| Benchmark                   | Mode | Cnt  | Score    | Error     | Units   |
| --------------------------- | ---- | ---- | -------- | --------- | ------- |
| commandFiveParameters       | avgt |  5   | 245.235  | ± 8.549   | ns/op   |
| commandIntParameter         | avgt |  5   | 126.463  | ± 0.727   | ns/op   |
| commandNoParameters         | avgt |  5   | 61.077   | ± 0.973   | ns/op   |
| commandNotFound             | avgt |  5   | 10.687   | ± 0.071   | ns/op   |
| commandOneParameter         | avgt |  5   | 107.434  | ± 0.866   | ns/op   |
| commandRemainderParameter   | avgt |  5   | 101.537  | ± 0.979   | ns/op   |

# Usage #

**Context Creation**

Your command context is a standard pojo used to pass contextual data into your commands. `BeanProvider` is a service container with the interface providing a default implementation.
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

To define a class as a module the class just needs to extend `CommandModuleBase<T>`. Any methods in the class that are annotated with the CommandDescription
annotation and return `CommandResult`; 
```java
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    @Aliases({"echo", "e"})
    public CommandResult pingPong(@Remainder String input) {
        return message(context().user() + " said: " + input);
    }
}
```

**CommandHandler Creation**

The command handler is your interface for interacting with your commands.
```java
public CommandHandler<OktaneCommandContext> commandHandler() {
    return CommandHandler.<OktaneCommandContext>builder()
        .withModule(OktaneCommandModule.class)
        .build();
}    
```

**Command Invocation**

To invoke a command you simply call to call `excute` on the command handler, pass it your command context, and the string input to parse.
```java
OktaneCommandContext context = new OktaneCommandContext("Kieran", BeanProvider.get());
Result result = commandHandlder.execute("echo Oktane is really cool :)", context);
```

**Granular Configuration**

Modules and commands can be configured a fair amount.

```java
@Name("My Module")                                      // Can be used in help displays, all the modules and commands can be accessed via
                                                        // CommandHandler#modules, and CommandHandler#commands 
@Description("This is a command module")                // Can be used in help displays
@Aliases({"a", "b"})                                    // commands inside a group must have the group prefix to execute, e.g. "a echo"
@Require(precondition = RequireOwnerPrecondition.class) // The preconditions to run to determine whether a module is executable or not
@Singleton                                              // Makes the module a singleton (transient by default)
@Synchronised                                           // Makes it so that all commands in the module are synchronised on a shared lock
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    
    @Name("Echo Command")                                                     // Can be used in help displays
    @Description("Echos input")                                               // Can be used in help displays
    @Aliases({"echo", "e"})                                                   // Defines the different aliases that can invoke the command
    @Require(precondition = ChannelPrecondition.class, arguments = "general") // The preconditions to run to determine whether the command is executable
    @Synchronised                                                             // Makes it so that the command is locally synchronised (public CommandResult synchronised ...)
    public CommandResult pingPong(
            @Name("User Input")               // Can be used in help displays       
            @Description("The input to echo") // Can be used in help displays
            @Remainder                        // Denotes the parameter as a remainder, so all the remaining text left to parse
                                              // will be passed into this parameter. There can only be one remainder, and it
                                              // must be the last parameter
            String input) {
        return message(context().user() + " said: " + input);
    }
}
```

**Type Parsing**

Oktane supports parsing all the primitive types, see `PrimitiveTypeParser`, and allowing a user to define their own.
Type parsers are added during the `CommandHandler` building stage using the withTypeParser method.
```java
public class UserTypeParser implements TypeParser<User> {
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

Preconditions can be used to add permissions to your commands. Preconditions will be fetched from the `BeanProvider`.
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

Beans can be injected into module using the `BeanProvider`, any constructor arguments will be passed into the module on instantiation, the `CommandHandler`
will inject itself and does not need to be added to a provider.
```java
public class OktaneCommandModule extends CommandModuleBase<OktaneCommandContext> {
    private final CommandHandler commandHandler;
    
    public OktaneCommandModule(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
    
    @CommandDescription(aliases = {"echo", "e"})
    public CommandResult pingPong(@ParameterDescription(remainder = true) String input) {
        return message(context().user() + " said: " + input);
    }
}
```

**Custom Argument Parsing**

The Oktane ArgumentParser can be overridden using the withArgumentParser method, it's however not recommended.

**How Oktane Works**

Oktane uses a few sprinkles of magic, when building a CommandHandler it uses reflection to get all the command methods, then 
classes are generated, compiled, and loaded in that are then used to invoke the commands and handle any module instantiation.
