package pufferenco;

import pufferenco.variables.StackElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pufferenco.DataType.*;

public class NativeFunction {
    private static final HashMap<String, ArrayList<NativeFunction>> Functions = new HashMap<>();
    public static boolean exists(String name){
        return Functions.containsKey(name);
    }

    static void init(){
        new NativeFunction("print", new int[]{STRING}, NULL, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "_PutS")
        ));
        new NativeFunction("printLn", new int[]{STRING}, NULL, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "_PutS"),
                new AssemblyLine("call","_NewLine")
        ));
        new NativeFunction("print", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("pop", "DE"),
                new AssemblyLine("ld", "HL", "0"),
                new AssemblyLine("ld", "L", "D"),
                new AssemblyLine("call", "_DispHL")
        ));
        new NativeFunction("printLn", new int[]{BYTE}, NULL, List.of(
                new AssemblyLine("pop", "DE"),
                new AssemblyLine("ld", "HL", "0"),
                new AssemblyLine("ld", "L", "D"),
                new AssemblyLine("call", "_DispHL"),
                new AssemblyLine("call","_NewLine")
        ));
        new NativeFunction("print", new int[]{DOUBLE}, NULL, List.of(
                new AssemblyLine("pop", "DE"),
                new AssemblyLine("ld", "HL", "0"),
                new AssemblyLine("ld", "H", "D"),
                new AssemblyLine("ld", "L", "E"),
                new AssemblyLine("call", "_DispHL"),
                new AssemblyLine("call", "_DispHL")
        ));
        new NativeFunction("printLn", new int[]{DOUBLE}, NULL, List.of(
                new AssemblyLine("pop", "DE"),
                new AssemblyLine("ld", "HL", "0"),
                new AssemblyLine("ld", "H", "D"),
                new AssemblyLine("ld", "L", "E"),
                new AssemblyLine("call", "_DispHL"),
                new AssemblyLine("call","_NewLine")
        ));
        new NativeFunction("print", new int[]{BOOL}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "print_bool")
        ));
        new NativeFunction("printLn", new int[]{BOOL}, NULL, List.of(
                new AssemblyLine("pop", "AF"),
                new AssemblyLine("call", "print_bool"),
                new AssemblyLine("call","_NewLine")
        ));
        new NativeFunction("len", new int[]{STRING}, DOUBLE, List.of(
                new AssemblyLine("pop", "HL"),
                new AssemblyLine("call", "get_string_size"),
                new AssemblyLine("dec", "HL"),
                new AssemblyLine("push", "HL")
        ));
    }

    public static StackElement exec(String name, List<StackElement> params, AssemblyBuilder builder, boolean returns){
        ArrayList<NativeFunction> overloads = Functions.get(name);
        overload_list:
        for (NativeFunction overload : overloads) {
            if(overload == null)
                continue;
            for (int i = 0; i < overload.params.length; i++) {
                if(overload.params[i] != params.get(i).type)
                    continue overload_list;
            }

            if(overload.content != null){
                builder.appendList(overload.content);
                if(returns)
                    return new StackElement(DataType.NAMES[overload.return_type], overload.return_type);
                else
                    return null;
            }
        }
        builder.error("mismatching parameter type for function call "  + name);
        return null;
    }



    String name;
    List<AssemblyLine> content;
    String assembly_name;
    int[] params;
    int return_type;
    NativeFunction(String name, String assembly_name, int[] params, int return_type){
        this.name = name;
        this.assembly_name = assembly_name;
        this.params = params;
        this.return_type = return_type;

        if(Functions.containsKey(name))
            Functions.get(name).add(this);
        else
            Functions.put(name, new ArrayList<>(List.of(this)));
    }

    NativeFunction(String name, int[] params, int return_type, List<AssemblyLine> content){
        this.name = name;
        this.content = content;
        this.params = params;
        this.return_type = return_type;

        if(Functions.containsKey(name))
            Functions.get(name).add(this);
        else
            Functions.put(name, new ArrayList<>(List.of(this)));
    }


}
