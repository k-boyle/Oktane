[![Quality Gate Status](https://img.shields.io/sonar/quality_gate/k-boyle_Oktane?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane) 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.k-boyle/Oktane?label=stable&style=for-the-badge)](https://search.maven.org/artifact/com.github.k-boyle/Oktane) 
[![OSS Snapshot](https://img.shields.io/nexus/s/com.github.k-boyle/Oktane?label=snapshot&server=https%3A%2F%2Foss.sonatype.org%2F&style=for-the-badge)](https://oss.sonatype.org/#nexus-search;quick~oktane)

Oktane is a high performance, highly configurable Java command framework used to map strings to methods, and execute them.

Inspired by [Qmmands](https://github.com/quahu/qmmands).

Example usage can be seen in the OktaneExample module.

# Performance Benchmarks #

**Benchmarks ran on a Ryzen 5600x @ 4.6GHz, as of 2.1.4-SNAPSHOT**

| Benchmark                   | Mode | Cnt  | Score    |  Error    | Units   |
| --------------------------- | ---- | ---- | -------- | --------- | ------- |
| commandFiveParameters       | avgt |  5   | 127.673  | ± 0.910   | ns/op   |
| commandIntParameter         | avgt |  5   | 57.682   | ± 0.523   | ns/op   |
| commandNoParameters         | avgt |  5   | 24.152   | ± 0.336   | ns/op   |
| commandNotFound             | avgt |  5   | 8.542    | ± 0.012   | ns/op   |
| commandOneParameter         | avgt |  5   | 48.747   | ± 0.218   | ns/op   |
| commandRemainderParameter   | avgt |  5   | 47.546   | ± 0.450   | ns/op   |

**Benchmarks ran on a Ryzen 2700x @ 3.6GHZ, as of 2.1.4-SNAPSHOT**

| Benchmark                   | Mode | Cnt  | Score    | Error     | Units   |
| --------------------------- | ---- | ---- | -------- | --------- | ------- |
| commandFiveParameters       | avgt |  5   | 239.477  | ± 5.330   | ns/op   |
| commandIntParameter         | avgt |  5   | 100.668  | ± 1.390   | ns/op   |
| commandNoParameters         | avgt |  5   | 34.057   | ± 0.446   | ns/op   |
| commandNotFound             | avgt |  5   | 12.995   | ± 0.346   | ns/op   |
| commandOneParameter         | avgt |  5   | 84.959   | ± 1.308   | ns/op   |
| commandRemainderParameter   | avgt |  5   | 81.584   | ± 1.053   | ns/op   |

**Benchmarks ran on a Xeon E5-2650L v3 (1 core vps) @ 1.80GHz, as of 2.1.4-SNAPSHOT**

| Benchmark                   | Mode | Cnt  | Score    | Error     | Units   |
| --------------------------- | ---- | ---- | -------- | --------- | ------- |
| commandFiveParameters       | avgt |  5   | 350.029  | ± 4.436   | ns/op   |
| commandIntParameter         | avgt |  5   | 177.304  | ± 8.751   | ns/op   |
| commandNoParameters         | avgt |  5   | 66.973   | ± 0.887   | ns/op   |
| commandNotFound             | avgt |  5   | 26.127   | ± 0.936   | ns/op   |
| commandOneParameter         | avgt |  5   | 139.783  | ± 5.129   | ns/op   |
| commandRemainderParameter   | avgt |  5   | 149.797  | ± 2.943   | ns/op   |

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
    public TypeParserResult<User> parse(CommandContext context, String input) {
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
    private final CommandHandler<OktaneCommandContext> commandHandler;
    
    public OktaneCommandModule(CommandHandler<OktaneCommandContext> commandHandler) {
        this.commandHandler = commandHandler;
    }
    
    @Aliases({"echo", "e"})
    public CommandResult pingPong(@Remainder String input) {
        return message(context().user() + " said: " + input);
    }
}
```

**Custom Argument Parsing**

The Oktane ArgumentParser can be overridden using the withArgumentParser method, it's however not recommended.

**How Oktane Works**

Oktane uses a few sprinkles of magic, when building a CommandHandler it uses reflection to get all the command methods, then 
classes are generated, compiled, and loaded in that are then used to invoke the commands and handle any module instantiation.
