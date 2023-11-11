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


public class ByteType implements DataType {
    private static final int Data_type_id = 0;
    private static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();

    @Override
    public StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_byte_" + Main.getId();

        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_push("HL");
        return new StackElement("var_read_byte_" + Main.getId(), Data_type_id);
    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        convertFrom(newValue, builder, false);
        builder.append_pop("HL");
        builder.append_ld("(" + variable.location + ")", "HL");
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        if(!key.is_constant)
            builder.error("byte sub requires a constant key");

        int number = (int) key.Constant_value;
        if(number < 0 || number > 7){
            builder.error("byte bit getting requires a constant key between 0 and 7");
            throw new RuntimeException();
        }

        builder.append_pop("BC");
        builder.append_ld("A", "regTRUE");
        builder.append(new AssemblyLine("bit", String.valueOf(number), "B"));
        builder.append(new AssemblyLine("call", "Z", "A_false_ret"));
        builder.append_push("AF");
        return new StackElement("var_read_byte_" + Main.getId(), DataType.BOOL);
    }


    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        builder.error("cannot set bit of a byte type use: [<byte> ~ <constant number>] to set " +
                "and [<byte> ~! <constant number>] to reset");
    }

    @Override
    public int elementSize(AssemblyBuilder builder) {
        return 1;
    }

    @Override
    public StackElement getAt(StackElement pointer, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_ld("A", "(HL)");
        builder.append_push("AF");
        return new StackElement("var_read_byte_" + Main.getId(), Data_type_id);
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_pop("AF");
        builder.append_ld("(HL)", "A");
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if (old.type != Data_type_id) {
            builder.error("cannot convert " + DataType.NAMES[old.type] + " to byte" );
            throw new RuntimeException();
        }
        if (old.is_constant && !keep_constant) {
            old.is_constant = false;
            try {
                builder.append_ld("A", String.valueOf(old.Constant_value));
                builder.append_push("AF");
                return old;
            } catch (NumberFormatException e) {
                builder.error("constant value is not a byte");
            }
        }
        return old;
    }


    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if (!Operators.containsKey(operator))
            builder.error("illegal operator [" + operator + "] with combination: " + DataType.NAMES[Data_type_id] + " and " + DataType.NAMES[right.type]);

        return Operators.get(operator).apply(builder, right);
    }

    @Override
    public StackElement initGlobal(StackElement element, AssemblyBuilder builder) {

        element = convertFrom(element, builder, false);
        builder.append_pop("AF");
        element.location = "globalVars+" + (Variable.GlobalOffset);
        builder.append_ld("("+element.location+")", "A");
        Variable.GlobalOffset += 1;
        return element;
    }

    @Override
    public StackElement get(AssemblyBuilder builder, String location) {
        builder.append_ld("A", "("+location+")");
        builder.append_push("AF");
        return new StackElement("fetched_byte", getId());
    }

    @Override
    public void set(String location, StackElement value, AssemblyBuilder builder) {
        convertFrom(value, builder, false);
        builder.append_pop("AF");
        builder.append_ld( "("+location+")", "A");
    }

    @Override
    public void init() {
        Operators.put("+", (AssemblyBuilder builder, StackElement right) -> {
            right = convertFrom(right, builder, true);

            if(right.is_constant && (int)right.Constant_value == 1) {
                builder.append_pop("AF");
                builder.append_inc("A");
                builder.append_push("AF");
                return new StackElement("byte_sum_" + Main.getId(), Data_type_id);
            }

            builder.append_pop("HL");
            if (right.is_constant) {
                builder.append_ld("A", "H");
                builder.append_add("A", String.valueOf(right.Constant_value));
            } else {
                builder.append_pop("AF");
                builder.append_add("A", "H");
            }
            builder.append_ld("H", "A");
            builder.append_push("HL");
            return new StackElement("byte_add_" + right.name, Data_type_id);
        });

        Operators.put("-", (AssemblyBuilder builder, StackElement right) -> {
            right = convertFrom(right, builder, true);

            if(right.is_constant && (int)right.Constant_value == 1) {
                builder.append_pop("AF");
                builder.append_dec("A");
                builder.append_push("AF");
                return new StackElement("byte_sub_" + Main.getId(), Data_type_id);
            }

            builder.append_pop("HL");
            if (right.is_constant) {
                builder.append_ld("A", "H");
                builder.append_sub("A", String.valueOf(right.Constant_value));
            } else {
                builder.append_pop("AF");
                builder.append_sub("A", "H");
            }
            builder.append_ld("H", "A");
            builder.append_push("HL");

            return new StackElement("byte_sub_" + right.name, Data_type_id);
        });

        Operators.put("*", (AssemblyBuilder builder, StackElement right) -> {
            right = convertFrom(right, builder, true);

            builder.append_pop("HL");
            if (right.is_constant) {
                builder.append_ld("L", String.valueOf(right.Constant_value));
            } else {
                builder.append_pop("BC");
                builder.append_ld("L", "B");
            }
            builder.append_mlt("HL");
            builder.append_ld("H", "L");
            builder.append_push("HL");

            return new StackElement("byte_mlt_" + right.name, Data_type_id);
        });

        Operators.put(">", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_higher");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("<", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_smaller");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("=>", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_higher_or_equals");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("<=", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_lower_or_equals");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("==", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_equals");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("!=", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, false);

            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_call("byte_not_equals");
            builder.append_push("AF");

            return new StackElement("byte_compare", DataType.BOOL);
        });

        Operators.put("~", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, true);
            if(!right.is_constant)
                builder.error("byte set required constant key");

            builder.append_pop("AF");
            builder.append(new AssemblyLine("set", String.valueOf(right.Constant_value), "A"));
            builder.append_push("AF");

            return new StackElement("byte_set_bit_" + right.name, Data_type_id);
        });

        Operators.put("~!", (AssemblyBuilder builder, StackElement right) -> {
            convertFrom(right, builder, true);
            if(!right.is_constant)
                builder.error("byte set required constant key");

            builder.append_pop("AF");
            builder.append(new AssemblyLine("res", String.valueOf(right.Constant_value), "A"));
            builder.append_push("AF");

            return new StackElement("byte_reset_bit_" + right.name, Data_type_id);
        });
    }


}
