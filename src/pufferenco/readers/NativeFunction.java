package pufferenco.readers;

import pufferenco.*;
import pufferenco.variables.StackElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pufferenco.DataType.*;

public class NativeFunction {
    private static final HashMap<String, ArrayList<NativeFunction>> Functions = new HashMap<>();

    public static boolean exists(String name) {
        return Functions.containsKey(name);
    }

    public static void init() {
        new NativeFunction("print", new int[]{STRING}, NULL, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "_PutS")
        ));
        new NativeFunction("printLn", new int[]{STRING}, NULL, List.of(
                new AssemblyLine("call", "printLn_string")
        ));
        new NativeFunction("print", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("call", "print_byte")
        ));
        new NativeFunction("printLn", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("call", "printLn_byte")
        ));
        new NativeFunction("print", new int[]{INT}, NULL, List.of(
                new AssemblyLine("call", "print_int")
        ));
        new NativeFunction("printLn", new int[]{INT}, NULL, List.of(
                new AssemblyLine("call", "printLn_int")
        ));
        new NativeFunction("printLn", new int[]{POINTER}, NULL, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "_DispHL"),
                new AssemblyLine("call", "_NewLine")
        ));
        new NativeFunction("print", new int[]{POINTER}, NULL, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "_DispHL")
        ));
        new NativeFunction("print", new int[]{BOOL}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "print_bool")
        ));
        new NativeFunction("printLn", new int[]{BOOL}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "print_bool"),
                new AssemblyLine("call", "_NewLine")
        ));
        new NativeFunction("putChar", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "_PutMap")
        ));
        new NativeFunction("printChar", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "_PutC")
        ));
        new NativeFunction("newLine", new int[]{}, NULL, List.of(
                new AssemblyLine("call", "_NewLine")
        ));
        new NativeFunction("len", new int[]{STRING}, BYTE, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "get_string_size"),
                new AssemblyLine("dec", "A"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("len", new int[]{ARRAY}, INT, List.of(
                new AssemblyLine("call", "get_array_size")
        ));
        new NativeFunction("merge", new int[]{BYTE, BYTE}, INT, List.of(
                new AssemblyLine("call","merge_byte_to_int")
        ));
        new NativeFunction("getUpper", new int[]{INT}, BYTE, List.of(
                new AssemblyLine("call","get_upper_int")
        ));
        new NativeFunction("getLower", new int[]{INT}, BYTE, List.of(
                new AssemblyLine("call","get_lower_int")
        ));
        new NativeFunction("not", new int[]{BOOL}, BOOL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("CPL"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("pause", new int[]{}, NULL, List.of(
                new AssemblyLine("call", "_GetKey")
        ));
        new NativeFunction("setCursor", new int[]{BYTE, BYTE}, NULL, List.of(
                new AssemblyLine("call", "set_character_cursor")
        ));
        new NativeFunction("getCursorCol", new int[]{}, BYTE, List.of(
                new AssemblyLine("ld", "A", "(curCol)"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("getCursorRow", new int[]{}, BYTE, List.of(
                new AssemblyLine("ld", "A", "(curRow)"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("getHeap", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "HEAP_START"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("getKeyMapping", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "KEY_MAPPING"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("getAlphaKeyMapping", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "ALPHA_KEY_MAPPING"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("getVRam", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "vRam"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("getBuffer", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "BUFFER_B1"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("waitKey", new int[]{}, BYTE, List.of(
                new AssemblyLine("call", "_GetKey"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("scanKey", new int[]{}, BYTE, List.of(
                new AssemblyLine("call", "_GetCSC"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("shiftLeft", new int[]{BYTE}, BYTE, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("SLL", "A"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("shiftRight", new int[]{BYTE}, BYTE, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("SRL", "A"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("shiftLeft", new int[]{BYTE}, BYTE, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("pop", "DE"),
                new AssemblyLine("RL", "A"),
                new AssemblyLine("push", "AF")
        ));
        new NativeFunction("sleep", new int[]{INT}, NULL, List.of(
                new AssemblyLine("call", "sleep_millis")
        ));
        new NativeFunction("randomInt", new int[]{}, INT, List.of(
                new AssemblyLine("call", "random_number"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("randomByte", new int[]{}, BYTE, List.of(
                new AssemblyLine("call", "random_number"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("getSaveData", new int[]{}, POINTER, List.of(
                new AssemblyLine("ld", "HL", "SaveMemory"),
                new AssemblyLine("push", "HL")
        ));
        new NativeFunction("writeMemory", new int[]{}, NULL, List.of(
                new AssemblyLine("call", "write_to_memory")
        ));
        new NativeFunction("readMemory", new int[]{}, NULL, List.of(
                new AssemblyLine("call", "load_from_memory")
        ));

    }

    public static StackElement exec(String name, List<StackElement> params, AssemblyBuilder builder, boolean returns) {
        ArrayList<NativeFunction> overloads = Functions.get(name);
        overload_list:
        for (NativeFunction overload : overloads) {
            if (overload == null)
                continue;
            if(overload.params.length != params.size())
                continue;
            for (int i = 0; i < overload.params.length; i++) {
                if (overload.params[i] != params.get(i).type)
                    continue overload_list;
            }

            if (overload.content != null) {
                builder.appendList(overload.content);
                if (returns)
                    return overload.return_type.retrieve();
                else
                    return null;
            }
        }

        builder.error("mismatching parameter type for function call " + name);
        return null;
    }


    String name;
    List<AssemblyLine> content;
    int[] params;
    StackElement return_type;

    NativeFunction(String name,  int[] params, int return_type, List<AssemblyLine> content) {
        this(name, params, new StackElement("return_value", return_type), content);
    }

    NativeFunction(String name,  int[] params, StackElement return_type, List<AssemblyLine> content) {
        this.name = name;
        this.content = content;
        this.params = params;
        this.return_type = return_type;

        if (Functions.containsKey(name))
            Functions.get(name).add(this);
        else
            Functions.put(name, new ArrayList<>(List.of(this)));
    }

    static void read(TokenStream tokens, AssemblyBuilder builder){
        Token name_token = tokens.read();
        if(name_token.type != Token.TokenTypes.IDENTIFIER)
            builder.error("Expected name after native function declaration");

        Token assembly_name_token = tokens.read();
        if(assembly_name_token.type != Token.TokenTypes.IDENTIFIER)
            builder.error("Excepted assembly name after native function declaration");

        Token return_type_token = tokens.read();
        if(return_type_token.type != Token.TokenTypes.IDENTIFIER)
            builder.error("Expected return type after native function declaration");

        ArrayList<Integer> parameters = new ArrayList<>();
        while(tokens.isNotEmpty()){
            Token current_parameter_token = tokens.read();
            if(!ArrayUtil.isInArray(current_parameter_token.content, NAMES))
                builder.error("unknown parameter " + current_parameter_token.content);
            parameters.add(ArrayUtil.inArray(current_parameter_token.content, NAMES));
        }

        int[] params = new int[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            params[i] = parameters.get(i);
        }

        new NativeFunction(
                name_token.content,
                params,
                ArrayUtil.inArray(return_type_token.content, NAMES),
                List.of(
                        new AssemblyLine("call", assembly_name_token.content)
                )
        );

    }
}
