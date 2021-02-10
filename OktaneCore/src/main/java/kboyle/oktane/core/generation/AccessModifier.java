package kboyle.oktane.core.generation;

public enum AccessModifier {
    PRIVATE("private"),
    PROTECTED("protected"),
    PACKAGE_PRIVATE(""),
    PUBLIC("public"),
    ;

    private final String declaration;

    AccessModifier(String declaration) {
        this.declaration = declaration;
    }

    public String declaration() {
        return declaration;
    }
}
