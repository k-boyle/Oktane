package kboyle.oktane.core.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.function.Consumer;

enum ProcessorUtil {
    ;

    static String getNestedPath(Element element, String separator) {
        var enclosingElement = element.getEnclosingElement();
        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return element.getSimpleName().toString();
        }

        return getNestedPath(enclosingElement, separator) + separator + element.getSimpleName();
    }

    static String getPackage(Element element) {
        var enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.toString();
        }

        return getPackage(enclosingElement);
    }

    static TypeMirror getBaseGeneric(Types types, TypeMirror superType, TypeMirror baseType) {
        return types.directSupertypes(superType).stream()
            .<TypeMirror>mapMulti((type, downstream) -> getBaseGeneric(types, baseType, type, downstream))
            .findFirst()
            .orElse(null);
    }

    private static void getBaseGeneric(Types types, TypeMirror baseType, TypeMirror type, Consumer<TypeMirror> downstream) {
        if (types.isSameType(types.erasure(baseType), types.erasure(type)) && type instanceof DeclaredType declaredType) {
            var targetType = declaredType.getTypeArguments().get(0);
            downstream.accept(targetType);
            return;
        }

        for (var superType : types.directSupertypes(type)) {
            getBaseGeneric(types, baseType, superType, downstream);
        }
    }
}
