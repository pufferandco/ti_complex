package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.HashMap;
import java.util.function.BiFunction;

public class PointerType implements DataType {
    HashMap<String, BiFunction<StackElement, AssemblyBuilder, StackElement>> operators = new HashMap<>();
    @Override
    public StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_pointer_" + Main.getId();
        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_push("HL");
        return new StackElement("var_read_pointer_" + Main.getId(), getId());
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if(old.type == DataType.ARRAY || old.type == DataType.STRING)
            return new StackElement("converted_pointer", getId());
        if (old.type != getId()) {

            builder.error("cannot convert value to pointer");
            throw new RuntimeException();
        }
        if(old.is_constant && !keep_constant) {
            builder.append_ld("HL", String.valueOf((int)old.Constant_value));
            builder.append_push("HL");
        }
        return old;
    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if(operators.containsKey(operator))
            return operators.get(operator).apply(right, builder);

        builder.error("operator " + operator + " is not supported by pointer");
        return null;
    }

    @Override
    public void init() {
        operators.put("+", (right, builder) -> {

            if(right.type == DataType.INT) {
                DataType.getInstance(INT).convertFrom(right, builder, false);
                builder.append_pop("DE");
                builder.append_pop("HL");
                builder.append_ld("BC", "0");
                builder.append_ld("B", "D");
                builder.append_ld("C", "E");
                builder.append_add("HL", "DE");
                builder.append_push("HL");
                return new StackElement("var_pointer_add" + Main.getId(), getId());
            }
            if(right.type == DataType.BYTE) {
                DataType.getInstance(BYTE).convertFrom(right, builder, false);
                builder.append_pop("AF");
                builder.append_pop("HL");
                builder.append_ld("DE", "0");
                builder.append_ld("E", "A");
                builder.append_add("HL", "DE");
                builder.append_push("HL");
                return new StackElement("var_pointer_add" + Main.getId(), getId());
            }
            builder.error("cannot add to pointer type");
            throw new RuntimeException();
        });
        operators.put("-", (right, builder) -> {
            if(right.type == DataType.INT) {
                DataType.getInstance(INT).convertFrom(right, builder, false);
                builder.append_pop("DE");
                builder.append_pop("HL");
                builder.append_ld("BC", "0");
                builder.append_ld("B", "D");
                builder.append_ld("C", "E");
                builder.append_or("A", "A");
                builder.append_sbc("HL", "BC");
                builder.append_push("HL");
                return new StackElement("var_pointer_add" + Main.getId(), getId());
            }
            if(right.type == DataType.BYTE) {
                DataType.getInstance(BYTE).convertFrom(right, builder, false);
                builder.append_pop("AF");
                builder.append_pop("HL");
                builder.append_ld("DE", "0");
                builder.append_ld("E", "A");
                builder.append_or("A", "A");
                builder.append_sbc("HL", "DE");
                builder.append_push("HL");
                return new StackElement("var_pointer_add" + Main.getId(), getId());
            }
            builder.error("cannot add to pointer type");
            throw new RuntimeException();
        });

        operators.put("->", (right, builder) -> {
            if(right.type != DataType.TYPE)
                builder.error("& operator requires a type identifier");

            switch ((int)right.Constant_value) {
                case DataType.INT ->{
                    builder.append_pop("HL");
                    builder.append_ld("DE", "0");
                    builder.append_ld("E", "(HL)");
                    builder.append_inc("HL");
                    builder.append_ld("D", "(HL)");
                    builder.append_push("DE");
                    return new StackElement("from_pointer_" + Main.getId(), (int)right.Constant_value);
                }
                case DataType.BYTE, DataType.BOOL ->{
                    builder.append_pop("HL");
                    builder.append_ld("A", "(HL)");
                    builder.append_push("AF");
                    return new StackElement("from_pointer_" + Main.getId(), (int)right.Constant_value);
                }
                case DataType.ARRAY, DataType.POINTER, DataType.STRING  ->{
                    builder.append_pop("HL");
                    builder.append_ld("DE", "(HL)");
                    builder.append_push("DE");
                    return new StackElement("from_pointer_" + Main.getId(), (int)right.Constant_value);
                }
                case DataType.NULL -> builder.error("cannot retrieve as null variable");
                default -> builder.error("cannot write " + DataType.getName((int)right.Constant_value) + " at pointer location");
        }
        throw new RuntimeException();
        });

    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        newValue = convertFrom(newValue, builder, false);

        builder.append_pop("HL");
        builder.append_ld("(" + variable.location + ")", "HL");

        newValue.location = variable.location;
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        builder.error("cannot read sub of int type");
        throw new RuntimeException();
    }

    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        builder.error("cannot set sub of int type");
    }

    @Override
    public int elementSize(AssemblyBuilder builder) {
        return 3;
    }

    @Override
    public StackElement getAt(StackElement pointer, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_ld("HL", "(HL)");
        builder.append_push("HL");
        return new StackElement("var_read_str_" + Main.getId(), getId());
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_pop("DE");
        builder.append_ld("(HL)", "DE");
    }

    @Override
    public StackElement initGlobal(StackElement element, AssemblyBuilder builder) {
        element = convertFrom(element, builder, false);
        builder.append_pop("HL");
        element.location = "globalVars+" + (Variable.GlobalOffset);
        builder.append_ld("("+element.location+")", "HL");
        Variable.GlobalOffset += 3;
        return element;
    }

    @Override
    public StackElement getStatic(AssemblyBuilder builder, String location) {
        builder.append_ld("HL", "("+location+")");
        builder.append_push("HL");
        return new StackElement("fetched_pointer", getId());
    }

    @Override
    public void setStatic(String location, StackElement value, AssemblyBuilder builder) {
        convertFrom(value, builder, false);
        builder.append_pop("HL");
        builder.append_ld( "("+location+")", "HL");
    }
}
