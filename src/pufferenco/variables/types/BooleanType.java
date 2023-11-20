package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.HashMap;
import java.util.function.BiFunction;


public class BooleanType implements DataType {
    private static final int Data_type_id = 4;
    private static final HashMap<String, BiFunction<AssemblyBuilder, StackElement, StackElement>> Operators = new HashMap<>();

    @Override
    public StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_bool_" + Main.getId();
        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_push("HL");
        return new StackElement("var_read_bool_" + Main.getId(), Data_type_id);
    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        convertFrom(newValue, builder, false);
        builder.append_pop("HL");
        builder.append_ld("(" + variable.location + ")", "HL");
    }

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        builder.error("cannot read sub of boolean type");
        throw new RuntimeException();
    }

    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        builder.error("cannot set sub of boolean type");
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
        return new StackElement("var_read_bool_" + Main.getId(), Data_type_id);
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_pop("AF");
        builder.append_ld("(HL)", "A");
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
    public StackElement getStatic(AssemblyBuilder builder, String location) {
        builder.append_ld("A", "("+location+")");
        builder.append_push("AF");
        return new StackElement("fetched_bool", getId());
    }

    @Override
    public void setStatic(String location, StackElement value, AssemblyBuilder builder) {
        convertFrom(value, builder, false);
        builder.append_pop("AF");
        builder.append_ld( "("+location+")", "A");
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if (old.type != Data_type_id) {
            builder.error("cannot convert " + DataType.NAMES[old.type] + " to bool");
            throw new RuntimeException();
        }
        if (old.is_constant) {
            old.is_constant = false;
            if (old.Constant_value.equals("true"))
                builder.append_ld("A", "%11111111");
            else
                builder.append_ld("A", "%00000000");
            builder.append_push("AF");
            return old;
        }
        return old;
    }


    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        if (!Operators.containsKey(operator))
            builder.error("illegal operator [" + operator + "] with combination: boolean and " + DataType.NAMES[right.type]);

        return Operators.get(operator).apply(builder, right);
    }

    @Override
    public void init() {
        Operators.put("and", ((builder, right) -> {
            right = convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_and("A", "H");
            builder.append_push("AF");
            return right;
        }));
        Operators.put("or", ((builder, right) -> {
            right = convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_or("A", "H");
            builder.append_push("AF");
            return right;
        }));
        Operators.put("xor", ((builder, right) -> {
            right = convertFrom(right, builder, false);
            builder.append_pop("HL");
            builder.append_pop("AF");
            builder.append_xor("A", "H");
            builder.append_push("AF");
            return right;
        }));
    }



}
