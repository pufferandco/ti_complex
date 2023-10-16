package pufferenco.variables;

import pufferenco.*;

import java.util.HashMap;

public class Variable {
    static HashMap<String, StackElement> Variables = new HashMap<>();
    public static void init(TokenStream stream, AssemblyBuilder builder){
        Token name = stream.read();
        if(name.type != Token.TokenTypes.IDENTIFIER)
            builder.error("token after [val] must be an identifier");

        if(!stream.read().content.equals(":"))
            builder.error("no required token [:] after name of value");

        Token typeIdentifier = stream.read();
        if(typeIdentifier.type != Token.TokenTypes.IDENTIFIER || !ArrayUtil.isInArray(typeIdentifier.content, DataType.NAMES)){
            builder.error("unknown token after [:]");
            return;
        }

        if(!stream.read().content.equals("="))
            builder.error("no required token [=] after type of value");

        int dataId = ArrayUtil.inArray(typeIdentifier.content, DataType.NAMES);
        if(dataId == -1)
            builder.error("no type exists with the name [" + typeIdentifier.content);



        DataType dataType = DataType.TYPES[dataId];
        Variables.put(name.content, dataType.initStackVariable(
                ExpressionReader.evalExpression(stream, builder, false),
                Main.Data_stack,
                builder
        ));
    }

    public static boolean exists(String name){
        return Variables.containsKey(name);
    }

    public static StackElement get(String name, AssemblyBuilder builder){
        StackElement element = Variables.get(name);
        return DataType.getInstance(element.type).getStackVariable(element,builder);
    }

    public static void set(String name, AssemblyBuilder builder, TokenStream stream){
        StackElement element = Variables.get(name);
        if(!stream.read().content.equals("="))
            builder.error("no required token [=] after global mention of variable");



        DataType.getInstance(element.type).setValue(
                element,
                ExpressionReader.evalExpression(stream, builder, false),
                builder
        );
    }
}
