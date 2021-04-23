package kboyle.oktane.core.processor;

import javax.lang.model.element.ExecutableElement;

record MethodData(ExecutableElement method, boolean isValid, boolean monoReturn) {
}
