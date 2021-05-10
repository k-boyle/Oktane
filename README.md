[![Quality Gate Status](https://img.shields.io/sonar/quality_gate/k-boyle_Oktane?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge)](https://sonarcloud.io/dashboard?id=k-boyle_Oktane)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.k-boyle/Oktane?label=stable&style=for-the-badge)](https://search.maven.org/artifact/com.github.k-boyle/Oktane)
[![OSS Snapshot](https://img.shields.io/nexus/s/com.github.k-boyle/Oktane?label=snapshot&server=https%3A%2F%2Foss.sonatype.org%2F&style=for-the-badge)](https://oss.sonatype.org/#nexus-search;quick~oktane)

Oktane is a high performance, highly configurable Java command framework used to map strings to methods, and execute them.
It is built upon the [Reactor](https://projectreactor.io/) framework to allow easy integration with reactive projects, such as [Discord4J](https://github.com/Discord4J/Discord4J)
Inspired by [Qmmands](https://github.com/quahu/qmmands).

Example usage can be seen in the OktaneExample module, and an example using Discord4J and Spring [here](https://github.com/k-boyle/degenerate).

# Usage #

**Context Creation**

Your command context is a standard pojo used to pass contextual data into your commands. `BeanProvider` is a service container with the interface providing a default implementations.
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

To define a class as a module the class just needs to extend `ModuleBase<T>`.
The `@Aliases` annotation is used to force an annotation processor to run.
Methods that are **public** and return `CommandResult` or `Mono<CommandResult>` are designated as commands.
When a class extends `ModuleBase<T>` a class will be generated that corresponds to each command method which will be used to invoke the commands at runtime,
this approach means that there is no overhead vs a direct method call,
if the module is not annotated then reflection will be used to invoke the methods (this is some magnitudes slower).
The `Aliases` annotation tells the `CommandHandler` what strings to map to this method.

```java
public class OktaneCommandModule extends ModuleBase<OktaneCommandContext> {
    @Aliases({"echo", "e"})
    public CommandResult pingPong(@Remainder String input) {
        return message(context().user() + " said: " + input);
    }
    
    @Aliases({"ping", "p"})
    public Mono<CommandResult> ping() {
        return sendWebRequest()
            .map(statusCode -> message("Got response: " + statusCode));
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
Mono<Result> result = commandHandlder.execute("echo Oktane is really cool :)", context);
result.ofType(CommandMessageResult.class)
    .map(CommandMessageResult::message)
    .subscribe(message -> System.out.println(message));
```

**Granular Configuration**

Modules and commands can be configured a fair amount.

```java
@Name("My Module")                                                              // Can be used in help displays, all the modules and commands can be accessed via
                                                                                // CommandHandler#modules, and CommandHandler#commands 
@Description("This is a command module")                                        // Can be used in help displays
@Aliases({"a", "b"})                                                            // commands inside a group must have the group prefix to execute, e.g. "a echo"
@Require(precondition = RequireOwnerPrecondition.class)                         // The preconditions to run to determine whether a module is executable or not
@Singleton                                                                      // Makes the module a singleton (transient by default)
@Synchronised                                                                   // Makes it so that all commands in the module are synchronised on a shared lock
public class OktaneCommandModule extends ModuleBase<OktaneCommandContext> {
    
    @Name("Echo Command")                                                       // Can be used in help displays
    @Description("Echos input")                                                 // Can be used in help displays
    @Aliases({"echo", "e"})                                                     // Defines the different aliases that can invoke the command
    @Require(precondition = ChannelPrecondition.class, arguments = "general")   // The preconditions to run to determine whether the command is executable
    @Synchronised                                                               // Makes it so that the command is locally synchronised (public CommandResult synchronised ...)
    public CommandResult pingPong(
            @Name("User Input")                                                 // Can be used in help displays       
            @Description("The input to echo")                                   // Can be used in help displays
            @Remainder                                                          // Denotes the parameter as a remainder, so all the remaining text left to parse
                                                                                // will be passed into this parameter. There can only be one remainder, and it
                                                                                // must be the last parameter
            String input) {
        return message(context().user() + " said: " + input);
    }
}
```

**Type Parsing**

Oktane supports parsing all the primitive types, see `PrimitiveTypeParserFactory`, and allowing a user to define their own.
Type parsers are added during the `CommandHandler` building stage using the `withTypeParser` method.
```java
public class UserTypeParser implements TypeParser<User> {
    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return context.beanProvider().get(UserService.class)
            .getUser(input)
            .map(this::success)
            .switchOnEmpty(failure("Failed to parse %s as a valid user", input).mono());
    }
} 
```

**Preconditions**

Preconditions can be used to add permissions to your commands.
During module creation the `CommandHandler` will instantiate any preconditions using reflection.
Preconditions are singletons per command.
```java
public class RequireOwnerPrecondition implements Precondition {
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        OktaneCommandContext context = (OktaneCommandContext) c;
        if (context.user().equals("Kieran")) {
            return success().mono();
        }
        
        return failure("Only Kieran can execute this command.").mono();
    }
}
```

**Dependency Injection**

Beans can be injected into module using the `BeanProvider`, any constructor arguments will be passed into the module on instantiation, the `CommandHandler`
will inject itself and does not need to be added to a provider.
```java
public class OktaneCommandModule extends ModuleBase<OktaneCommandContext> {
    private final CommandHandler<OktaneCommandContext> commandHandler;
    private final UserService userService;
    
    public OktaneCommandModule(
            CommandHandler<OktaneCommandContext> commandHandler,
            UserService userService) {
        this.commandHandler = commandHandler;
        this.userService = userService;
    }
    
    @Aliases({"commands", "c"})
    public CommandResult commandCount() {
        return message("There are " + commandHandler.commands().count() + " commands");
    }
    
    @Aliases({"users"})
    public Mono<CommandResult> listUsers() {
        return userService.getAll()
            .map(users -> message(users));
    }
}
```
