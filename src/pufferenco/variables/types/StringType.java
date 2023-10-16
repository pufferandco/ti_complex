package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.AssemblyLine;
import pufferenco.DataType;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;

import static pufferenco.variables.DataStack.StackStart;

public class StringType implements DataType {
    public static int Data_type_id = 2;
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
        builder.append_ld("HL", "("+StackStart + "-" + (element.location+3)+")");
        builder.append_push("HL");
        return element;
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if(old.type != Data_type_id) {
            builder.error("cannot convert value to string");
            throw new RuntimeException();
        }
        return old;
    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        builder.error("operator "+ operator + " is not supported by ");
        return null;
    }

    @Override
    public void init() {}

    @Override
    public void setValue(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        newValue = convertFrom(newValue, builder, false);

        builder.append_ld("HL", "("+StackStart + "-" + (variable.location+3)+")");
        builder.append_call("free");

        builder.append_pop("HL");
        builder.append_call("string_to_heap");
        builder.append_ld("("+StackStart + "-" + (variable.location+3)+")", "DE");

        newValue.location = variable.location;
    }
}
