package kboyle.oktane.core.configuration;

import com.google.auto.service.AutoService;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.parsers.CharTypeParser;
import kboyle.oktane.core.parsers.PrimitiveTypeParser;

@AutoService(CommandHandlerConfigurator.class)
public class CommandHandlerWithPrimitiveTypeParsers implements CommandHandlerConfigurator {
    @Override
    public void apply(CommandHandler.Builder<?> commandHandler) {
        commandHandler.withTypeParser(boolean.class, new PrimitiveTypeParser<>(Boolean.class, Boolean::parseBoolean))
            .withTypeParser(Boolean.class, new PrimitiveTypeParser<>(Boolean.class, Boolean::parseBoolean))
            .withTypeParser(byte.class, new PrimitiveTypeParser<>(Byte.class, Byte::parseByte))
            .withTypeParser(Byte.class, new PrimitiveTypeParser<>(Byte.class, Byte::parseByte))
            .withTypeParser(char.class, new CharTypeParser())
            .withTypeParser(Character.class, new CharTypeParser())
            .withTypeParser(int.class, new PrimitiveTypeParser<>(Integer.class, Integer::parseInt))
            .withTypeParser(Integer.class, new PrimitiveTypeParser<>(Integer.class, Integer::parseInt))
            .withTypeParser(short.class, new PrimitiveTypeParser<>(Short.class, Short::parseShort))
            .withTypeParser(Short.class, new PrimitiveTypeParser<>(Short.class, Short::parseShort))
            .withTypeParser(float.class, new PrimitiveTypeParser<>(Float.class, Float::parseFloat))
            .withTypeParser(Float.class, new PrimitiveTypeParser<>(Float.class, Float::parseFloat))
            .withTypeParser(long.class, new PrimitiveTypeParser<>(Long.class, Long::parseLong))
            .withTypeParser(Long.class, new PrimitiveTypeParser<>(Long.class, Long::parseLong))
            .withTypeParser(double.class, new PrimitiveTypeParser<>(Double.class, Double::parseDouble))
            .withTypeParser(Double.class, new PrimitiveTypeParser<>(Double.class, Double::parseDouble));
    }
}
