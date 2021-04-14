package kboyle.oktane.reactive.results.search;

public record CommandNotFoundResult() implements CommandSearchResult {
    private static class SingletonHolder {
        private static final CommandNotFoundResult INSTANCE = new CommandNotFoundResult();
    }

    public static CommandNotFoundResult get() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public String reason() {
        return "Command not found";
    }
}
