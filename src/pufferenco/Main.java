package pufferenco;

import pufferenco.optimization.AssemblyCollapse;
import pufferenco.variables.DataStack;
import pufferenco.variables.Variable;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static pufferenco.ArrayUtil.inArray;
import static pufferenco.ArrayUtil.isInArray;
import static pufferenco.AssemblyLine.customAssemblyLine;

public class Main {

    public static AssemblyBuilder Constants = new AssemblyBuilder();
    public static Stack<DataStack> VariableStacks = new Stack<>();
    public static Stack<Function> Function_stack = new Stack<>();
    public static DataStack Call_stack = new DataStack(DataStack.CallStack);
    final static int OPTIMIZE_LEVEL = 1;
    public static boolean early_exit = false;
    public static String root_folder;
    public static boolean safety_mode = true;


    public static void main(String[] args) {

        root_folder = args[0] + "/";
        int safe_size = Integer.parseInt(args[2]);
        safety_mode = isInArray("*unsafe", args);

        DataStack global_stack = new DataStack("stackStart");
        VariableStacks.push(global_stack);
        AssemblyBuilder builder = new AssemblyBuilder();
        init();

        builder.append(customAssemblyLine("#include \"asm/include.inc\""));
        builder.append(customAssemblyLine(".assume\tADL=1"));
        builder.append(customAssemblyLine(".org\tuserMem-2"));
        builder.append_tag("MemoryStart");
        builder.append_db("tExtTok,tAsm84CeCmp");
        builder.append((new AssemblyLine("")));
        builder.append(new AssemblyLine("set", "AppAutoScroll", "(IY + AppFlags)"));
        builder.append_ld("(StackSave)", "SP");
        builder.append_ld("HL", "callStackStart");
        builder.append_ld("(" + DataStack.CallStack + ")", "HL");
        builder.append_ld("SP", "stackStart");
        builder.append_call("init");
        builder.append_call("_homeup");
        builder.append_call("_ClrScrnFull");
        builder.append((new AssemblyLine("")));

        get_and_run(args[1], builder);

        builder.append((new AssemblyLine("")));
        builder.append_tag("ProgramExit");
        builder.append_call("_ClrScrnFull");
        builder.append_res("donePrgm", "(iy+doneFlags)");
        builder.append_ld("SP", "(StackSave)");
        builder.append_ret();
        builder.append(customAssemblyLine("#include \"asm/defaults.asm\""));
        builder.append_tag("StackSave");
        builder.append_db("0,0,0");
        builder.append_tag("CallStack");
        builder.append_db("0,0,0");
        builder.append_tag("SaveLocation");
        builder.append_db("0,0,0");
        builder.append(customAssemblyLine("stackStart" + " .equ saveSScreen+7315"));
        builder.append(customAssemblyLine("callStackStart" + " .equ saveSScreen+14630"));
        builder.append(customAssemblyLine("globalVars" + " .equ pixelShadow"));
        builder.append(AssemblyLine.customAssemblyLine("saveSize .equ " + safe_size));
        builder.append_tag("saveMemory");
        String size = "0,".repeat(safe_size);
        builder.append_db(size.substring(0, size.length()-1));


        IOUtil.writeTxt("asm/main.asm",
                AssemblyCollapse.optimize(builder, OPTIMIZE_LEVEL).getAssembly() + "\n" +
                        AssemblyCollapse.optimize(Function.FunctionBuilder, OPTIMIZE_LEVEL).getAssembly() + "\n"
                        + Constants.getAssembly());
    }

    public static void tokenizeAndRun(String code, AssemblyBuilder builder) {
        List<List<Token>> tokenLines = Token.tokenizeLines(code);

        early_exit = false;
        for (List<Token> tokens : tokenLines) {
            globalReader.read(tokens, builder);

        }
    }

    private static void init() {
        NativeFunction.init();
        DataType.staticInit();
    }

    private static long id = 0;

    public static String getId() {
        return String.valueOf(id++);
    }

    public static void get_and_run(String file_name, AssemblyBuilder builder) {
        Variable.increase_scope(builder);

        String code = IOUtil.readTxt(root_folder + file_name);
        builder.add_func(file_name);
        tokenizeAndRun(code, builder);
        builder.remove_func();

        Variable.decrease_scope();
    }
}