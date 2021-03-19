package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;

public final class CommandUtil {
    private CommandUtil() {
    }

    public static PreconditionResult runPreconditions(CommandContext context, ImmutableList<Precondition> preconditions) {
        if (preconditions.isEmpty()) {
            return PreconditionSuccessfulResult.get();
        }

        ImmutableList.Builder<PreconditionResult> failedResults = null;
        boolean failedResult = false;

        for (Precondition precondition : preconditions) {
            PreconditionResult result = Preconditions.checkNotNull(precondition.run(context), "A precondition cannot return null");
            if (!result.success()) {
                if (failedResults == null) {
                    failedResults = ImmutableList.builder();
                }
                failedResults.add(result);
                failedResult = true;
            }
        }

        return failedResult
            ? new PreconditionsFailedResult(failedResults.build())
            : PreconditionSuccessfulResult.get();
    }
}
