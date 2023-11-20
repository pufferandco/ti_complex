package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.AssemblyLine;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.HashMap;
import java.util.function.BiFunction;


public class IntType implements DataType {
    private static final int Data_type_id = 1;
    private static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();

    @Override
    public StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_int_" + Main.getId();
        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_push("HL");
        return new StackElement("var_read_int_" + Main.getId(), Data_type_id);
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        if(!key.is_constant)
            builder.error("int sub requires a constant key");

        int number = (int) key.Constant_value;
        if(number < 0 || number > 15){
            builder.error("int bit getting requires a constant key between 0 and 15");
            throw new RuntimeException();
        }

        builder.append_pop("BC");
        builder.append_ld("A", "regTRUE");
        if(number > 7)
            builder.append(new AssemblyLine("bit", String.valueOf(number-8), "B"));
        else
            builder.append(new AssemblyLine("bit", String.valueOf(number), "C"));
        builder.append(new AssemblyLine("call", "Z", "A_false_ret"));
        builder.append_push("AF");
        return new StackElement("var_bit_" + Main.getId(), DataType.BOOL);
    }

    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        builder.error("setSub not allowed for integer tpye");
    }

    @Override
    public int elementSize(AssemblyBuilder builder) {
        return 2;
    }

    @Override
    public StackElement getAt(StackElement pointer, AssemblyBuilder builder) {

        builder.append_pop("HL");
        builder.append_ld("DE", "0");
        builder.append_ld("D", "(HL)");
        builder.append_inc("HL");
        builder.append_ld("E", "(HL)");
        builder.append_push("DE");
        return new StackElement("var_read_int_" + Main.getId(), getId());
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_pop("DE");
        builder.append_ld("(HL)", "D");
        builder.append_inc("HL");
        builder.append_ld("(HL)", "E");
    }

    @Override
    public StackElement initGlobal(StackElement element, AssemblyBuilder builder) {
        element = convertFrom(element, builder, false);
        element.location = "globalVars+" + (Variable.GlobalOffset);

        builder.append_pop("DE");
        builder.append_ld("HL", element.location);
        builder.append_ld("(HL)", "D");
        builder.append_inc("HL");
        builder.append_ld("(HL)", "E");

        Variable.GlobalOffset += 2;
        return element;
    }

    @Override
    public StackElement getStatic(AssemblyBuilder builder, String location) {
        builder.append_ld("DE", "0");
        builder.append_ld("HL", location);
        builder.append_ld("D", "(HL)");
        builder.append_inc("HL");
        builder.append_ld("E","(HL)");
        builder.append_push("DE");
        return new StackElement("fetched_int", getId());
    }

    @Override
    public void setStatic(String location, StackElement value, AssemblyBuilder builder) {
        convertFrom(value, builder, false);
        builder.append_pop("DE");
        builder.append_ld("HL", location);
        builder.append_ld("(HL)", "D");
        builder.append_inc("HL");
        builder.append_ld("(HL)", "E");
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

        builder.error("cannot convert " + DataType.getName(old.type) + " to integer");
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
            if(right.is_constant && (int)right.Constant_value == 1) {
                builder.append_pop("HL");
                builder.append_inc("HL");
                builder.append_push("HL");
                return new StackElement("int_sum_" + Main.getId(), Data_type_id);
            }

            builder.append_pop("HL");
            if (right.is_constant) {
                builder.append_ld("DE", right.Constant_value.toString());
                builder.append_add("HL", "DE");
            } else {
                builder.append_pop("DE");
                builder.append_add("HL", "DE");
            }
            builder.append_push("HL");
            return new StackElement("int_sum_" + Main.getId(), Data_type_id);
        }));

        Operators.put("-", ((builder, right) -> {
            convertFrom(right, builder, true);
            if(right.is_constant && (int)right.Constant_value == 1) {
                builder.append_pop("HL");
                builder.append_dec("HL");
                builder.append_push("HL");
                return new StackElement("int_sub_" + Main.getId(), Data_type_id);
            }

            builder.append_pop("HL");
            builder.append_or("A", "A");
            if (right.is_constant) {
                builder.append_ld("DE", right.Constant_value.toString());
                builder.append_sbc("HL", "DE");
            } else {
                builder.append_pop("DE");
                builder.append_ex("DE", "HL");
                builder.append_sbc("HL", "DE");
            }
            builder.append_push("HL");
            return new StackElement("int_sub_" + Main.getId(), Data_type_id);
        }));

        Operators.put("*", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("DE");
            builder.append_pop("BC");
            builder.append_call("multiply_int");
            builder.append_push("HL");
            return new StackElement("int_mlt_" + Main.getId(), Data_type_id);
        }));

        Operators.put(">", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_higher");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("<", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_smaller");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("=>", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_higher_or_equals");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("<=", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_smaller_or_equals");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("==", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_equals");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));

        Operators.put("!=", ((builder, right) -> {
            convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("DE");
            builder.append_ex("DE", "HL");
            builder.append_call("int_not_equals");
            builder.append_push("AF");
            return new StackElement("int_sub_" + Main.getId(), DataType.BOOL);
        }));
    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        convertFrom(newValue, builder, false);
        builder.append_pop("HL");
        builder.append_ld("(" + variable.location + ")", "HL");
    }

    @Override
    public int getId() {
        return 1;
    }
}
