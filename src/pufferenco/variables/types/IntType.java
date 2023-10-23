package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;

import java.util.HashMap;
import java.util.function.BiFunction;


public class IntType implements DataType {
    private static final int Data_type_id = 1;
    private static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();

    @Override
    public StackElement initStackVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_int_" + Main.getId();
        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + Main.VariableStacks.peek().stack_start + "-" + (element.location + 3) + ")");
        builder.append_push("HL");
        return element;
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if (old.type == Data_type_id) {
            if (old.is_constant && !keep_constant) {
                old.is_constant = false;
                try {
                    builder.append_ld("HL", String.valueOf(old.Constant_value));
                    builder.append_push("HL");
                    return old;
                } catch (NumberFormatException e) {
                    builder.error("constant value is not an integer");
                }
            }
            return old;
        }

        if (old.type == DataType.BYTE) {
            builder.append_pop("DE");
            builder.append_ld("HL", "0");
            builder.append_ld("L", "D");
            builder.append_push("HL");
            return new StackElement("converted_int_" + Main.getId(), Data_type_id);
        }

        builder.error("cannot convert value to double");
        throw new RuntimeException();

    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if (!Operators.containsKey(operator))
            builder.error("illegal operator [" + operator + "] with combination: " + DataType.NAMES[Data_type_id] + " and " + DataType.NAMES[right.type]);

        return Operators.get(operator).apply(builder, right);
    }

    @Override
    public void init() {
        Operators.put("+", ((builder, right) -> {
            convertFrom(right, builder, true);

            builder.append_pop("HL");
            if (right.is_constant) {
                builder.append_add("HL", right.Constant_value.toString());
            } else {
                builder.append_pop("DE");
                builder.append_add("HL", "DE");
            }
            builder.append_push("HL");
            return new StackElement("int_sum_" + Main.getId(), Data_type_id);
        }));

        Operators.put("-", ((builder, right) -> {
            convertFrom(right, builder, true);
            builder.append_pop("HL");
            builder.append_or("A", "A");
            if (right.is_constant) {
                builder.append_sbc("HL", right.Constant_value.toString());
            } else {
                builder.append_pop("DE");
                builder.append_ex("DE", "HL");
                builder.append_sbc("HL", "DE");
            }
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), Data_type_id);
        }));

        Operators.put(">", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_higher");
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("<", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_smaller");
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("=>", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_higher_or_equals");
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("<=", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_smaller_or_equals");
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));
    }

    @Override
    public void setValue(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        convertFrom(newValue, builder, false);
        builder.append_pop("HL");
        builder.append_ld("(" + Main.VariableStacks.peek().stack_start + "-" + (variable.location + 3) + ")", "HL");
    }

    @Override
    public int getId() {
        return 1;
    }
}
