package kb.octane.core.parsers;

import kb.octane.core.CommandContext;
import kb.octane.core.results.Result;

public interface ArgumentParser {
    Result parse(CommandContext context, String input, int index);
}
