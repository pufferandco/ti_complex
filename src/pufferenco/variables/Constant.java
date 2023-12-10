package pufferenco.variables;

import pufferenco.*;
import pufferenco.readers.ExpressionReader;

import java.util.HashMap;

public class Constant {
    public static HashMap<String, StackElement> constants = new HashMap<>();
    public static void init(TokenStream stream, AssemblyBuilder builder) {
        Token name = stream.read();
        if (name.type != Token.TokenTypes.IDENTIFIER)
            builder.error("token after [const] must be an identifier");

        if (!stream.read().content.equals(":"))
            builder.error("no required token [:] after name of value");

        Token typeIdentifier = stream.read();
        if (typeIdentifier.type != Token.TokenTypes.IDENTIFIER || !ArrayUtil.isInArray(typeIdentifier.content, DataType.NAMES)) {
            builder.error("unknown token after [:]");
            return;
        }

        if (!stream.read().content.equals("="))
            builder.error("no required token [=] after type of value");

        int dataId = ArrayUtil.inArray(typeIdentifier.content, DataType.NAMES);
        if (dataId == -1)
            builder.error("no type exists with the name [" + typeIdentifier.content);


        DataType dataType = DataType.TYPES[dataId];

        StackElement constant = ExpressionReader.evalExpression(stream, builder, true);
        if (!constant.is_constant)
            builder.error("constant [" + constant.name + "] is not a constant");

        if(constant.type != dataType.getId()){

            builder.error("constant [" + constant.name + "] is not of type " + DataType.NAMES[dataType.getId()]);
            return;
        }

        constants.put(name.content, constant);
    }

    public static StackElement get(String name, AssemblyBuilder builder, boolean allow_constant) {
        StackElement element = constants.get(name);
        System.out.println(element.Constant_value);
        System.out.println(builder.lines.get(builder.lines.size()-1));
        element = DataType.getInstance(element.type).convertFrom(element.retrieve(), builder, allow_constant);

        return element;
    }

}
