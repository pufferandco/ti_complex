package pufferenco;

import pufferenco.optimization.AssemblyCollapse;
import pufferenco.readers.Function;
import pufferenco.readers.NativeFunction;
import pufferenco.readers.globalReader;
import pufferenco.variables.DataStack;
import pufferenco.variables.Variable;

import java.util.List;
import java.util.Stack;

import static pufferenco.AssemblyLine.customAssemblyLine;

public class Main {

    public static AssemblyBuilder Constants = new AssemblyBuilder();
    public static Stack<DataStack> VariableStacks = new Stack<>();
    public static Stack<Function> Function_stack = new Stack<>();
    public static DataStack Call_stack = new DataStack(DataStack.CallStack);
    final static int OPTIMIZE_LEVEL = 1;
    public static boolean early_exit = false;
    public static String root_folder;
    public static boolean safety_mode = false;


    public static void main(String[] args) {
        if(args.length != 2)
            throw new RuntimeException("the program requires 2 inputs: root folder and main file");
        root_folder = args[0] + "/";

        DataStack global_stack = new DataStack("stackStart");
        VariableStacks.push(global_stack);
        AssemblyBuilder builder = new AssemblyBuilder();
        init();

        builder.append(customAssemblyLine("#include \"asm/include.inc\""));
        builder.append(customAssemblyLine(".assume\tADL=1"));
        builder.append(customAssemblyLine(".org\tuserMem-2"));
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

        getAndRun(args[1], builder);

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


        IOUtil.writeTxt("asm/main.asm",
                AssemblyCollapse.optimize(builder, OPTIMIZE_LEVEL).getAssembly() + "\n" +
                        AssemblyCollapse.optimize(Function.FunctionBuilder, OPTIMIZE_LEVEL).getAssembly() + "\n"
                        + Constants.getAssembly());

        System.out.println("compilation successful!");
    }

    public static void tokenizeAndRun(String code, AssemblyBuilder builder) {
        List<List<Token>> tokenLines = Token.tokenizeLines(code, builder);

        early_exit = false;
        for (List<Token> tokens : tokenLines) {
            builder.newLine(tokens);
            globalReader.read(tokens, builder);
            builder.closeLine();
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

    public static void getAndRun(String file_name, AssemblyBuilder builder) {
        Variable.increase_scope(builder);

        String code = IOUtil.readTxt(root_folder + file_name);
        builder.add_func(file_name);
        tokenizeAndRun(code, builder);
        builder.remove_func();

        Variable.decrease_scope();
    }
}