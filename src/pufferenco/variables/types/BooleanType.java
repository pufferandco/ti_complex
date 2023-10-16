package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;

import java.util.HashMap;
import java.util.function.BiFunction;

import static pufferenco.variables.DataStack.StackStart;

public class BooleanType implements DataType {
    public static final int Data_type_id = 4;
    public static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();
    @Override
    public StackElement initStackVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_bool_" + Main.getId();
        return stack.push(value, builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "("+StackStart + "-" + (element.location+3)+")");
        builder.append_push("HL");
        return element;
    }

    @Override
    public void setValue(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        convertFrom(newValue, builder, false);
        builder.append_pop("HL");
        builder.append_ld("("+StackStart + "-" + (variable.location+3)+")", "HL");
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if(old.type != Data_type_id) {
            builder.error("cannot convert value to bool");
            throw new RuntimeException();
        }
        if(old.is_constant && !keep_constant){
            old.is_constant = false;
            try{
                builder.append_ld("HL", String.valueOf(old.Constant_value));
                builder.append_push("HL");
                return old;
            }catch (NumberFormatException e){
                builder.error("constant value is not a bool");
            }
        }
        return old;
    }


    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if(!Operators.containsKey(operator))
            builder.error("illegal operator [+] with combination: boolean and " + DataType.NAMES[right.type]);

        return Operators.get(operator).apply(builder, right);
    }

    @Override
    public void init() {
        Operators.put("and", ((builder, right) -> {
            right = convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_and("A","H");
            builder.append_push("A");
            return right;
        }));
        Operators.put("or", ((builder, right) -> {
            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_and("A","H");
            builder.append_push("A");
            return right;
        }));
    }

}
