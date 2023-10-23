package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;


public class StringType implements DataType {
    private final static int Data_type_id = 2;

    @Override
    public StackElement initStackVariable(StackElement pointer, DataStack stack, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_call("string_to_heap");
        builder.append_push("DE");

        stack.push(pointer, builder);
        return new StackElement(pointer.name, DataType.STRING);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + Main.VariableStacks.peek().stack_start + "-" + (element.location + 3) + ")");
        builder.append_push("HL");
        return element;
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if (old.type != Data_type_id) {
            builder.error("cannot convert value to string");
            throw new RuntimeException();
        }
        return old;
    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        builder.error("operator " + operator + " is not supported by ");
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public void setValue(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        newValue = convertFrom(newValue, builder, false);

        builder.append_ld("HL", "(" + Main.VariableStacks.peek().stack_start + "-" + (variable.location + 3) + ")");
        builder.append_call("free");

        builder.append_pop("HL");
        builder.append_call("string_to_heap");
        builder.append_ld("(" + Main.VariableStacks.peek().stack_start + "-" + (variable.location + 3) + ")", "DE");

        newValue.location = variable.location;
    }

    @Override
    public int getId() {
        return 2;
    }
}
