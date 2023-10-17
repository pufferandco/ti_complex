package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;

import java.util.HashMap;
import java.util.function.BiFunction;

import static pufferenco.variables.DataStack.StackStart;

public class ByteType implements DataType {
    public static final int Data_type_id = 0;
    public static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();
    @Override
    public StackElement initStackVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_byte_" + Main.getId();
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
            builder.error("cannot convert value to byte");
            throw new RuntimeException();
        }
        if(old.is_constant && !keep_constant){
            old.is_constant = false;
            try{
                builder.append_ld("HL", String.valueOf(old.Constant_value));
                builder.append_push("HL");
                return old;
            }catch (NumberFormatException e){
                builder.error("constant value is not a byte");
            }
        }
        return old;
    }


    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if(!Operators.containsKey(operator))
            builder.error("illegal operator [" + operator + "] with combination: " + DataType.NAMES[Data_type_id] + " and " + DataType.NAMES[right.type]);

        return Operators.get(operator).apply(builder, right);
    }

    @Override
    public void init() {
        Operators.put("+", (AssemblyBuilder builder, StackElement right)->{
            right = convertFrom(right, builder, true);

            builder.append_pop("HL");
            if(right.is_constant) {
                builder.append_ld("A", "H");
                builder.append_add("A", String.valueOf(right.Constant_value));
            }else{
                builder.append_pop("AF");
                builder.append_add("A", "H");
            }
            builder.append_ld("H", "A");
            builder.append_push("HL");
            return new StackElement("byte_add_" + right.name, Data_type_id);
        });

        Operators.put("-", (AssemblyBuilder builder, StackElement right)->{
            right = convertFrom(right, builder, true);

            builder.append_pop("HL");
            if(right.is_constant){
                builder.append_ld("A", "H");
                builder.append_sub("A", String.valueOf(right.Constant_value));
            }else{
                builder.append_pop("AF");
                builder.append_sub("A","H");
            }
            builder.append_ld("H", "A");
            builder.append_push("HL");

            return new StackElement("byte_sub_" + right.name, Data_type_id);
        });

        Operators.put("*", (AssemblyBuilder builder, StackElement right)->{
            right = convertFrom(right, builder, true);

            builder.append_pop("HL");
            if(right.is_constant){
                builder.append_ld("L", String.valueOf(right.Constant_value));
            }else{
                builder.append_pop("BC");
                builder.append_ld("L", "B");
            }
            builder.append_mlt("HL");
            builder.append_ld("H", "L");
            builder.append_push("HL");

            return new StackElement("byte_mlt_" + right.name, Data_type_id);
        });

        Operators.put(">", (AssemblyBuilder builder, StackElement right)->{
            convertFrom(right, builder, true);

            builder.append_pop("AF");
            builder.append_pop("HL");
            builder.append_call("byte_higher");
            builder.append_push("HL");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("<", (AssemblyBuilder builder, StackElement right)->{
            convertFrom(right, builder, true);

            builder.append_pop("AF");
            builder.append_pop("HL");
            builder.append_call("byte_smaller");
            builder.append_push("HL");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("=>", (AssemblyBuilder builder, StackElement right)->{
            convertFrom(right, builder, true);

            builder.append_pop("AF");
            builder.append_pop("HL");
            builder.append_call("byte_higher_or_equals");
            builder.append_push("HL");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("<=", (AssemblyBuilder builder, StackElement right)->{
            convertFrom(right, builder, true);

            builder.append_pop("AF");
            builder.append_pop("HL");
            builder.append_call("byte_lower_or_equals");
            builder.append_push("HL");

            return new StackElement("byte_compare", DataType.BOOL);
        });
    }


}
