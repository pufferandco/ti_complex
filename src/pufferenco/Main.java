package pufferenco;

import pufferenco.optimization.AssemblyCollapse;
import pufferenco.variables.DataStack;
import pufferenco.variables.Variable;

import java.util.List;

import static pufferenco.AssemblyLine.customAssemblyLine;

public class Main {

    public static AssemblyBuilder Constants = new AssemblyBuilder();
    public static DataStack Variable_stack = new DataStack();
    public static DataStack Call_stack = new DataStack();
    final static int OPTIMIZE_LEVEL = 1;
    public static void main(String[] args) {
        AssemblyBuilder builder = new AssemblyBuilder();
        init();

        builder.append(customAssemblyLine("#include \"asm/include.inc\""));
        builder.append(customAssemblyLine(".assume\tADL=1"));
        builder.append(customAssemblyLine(".org\tuserMem-2"));
        builder.append_db("tExtTok,tAsm84CeCmp");
        builder.append((new AssemblyLine("")));
        builder.append_ld("(StackSave)", "SP");
        builder.append_ld("HL", "callStackStart");
        builder.append_ld("("+DataStack.CallStack +")", "HL");
        builder.append_ld("SP", DataStack.STACK_START);
        builder.append_call("init");
        builder.append_call("_homeup");
        builder.append_call("_ClrScrnFull");
        builder.append((new AssemblyLine("")));

        Variable.increase_scope(builder);
        String code = IOUtil.readTxt("root/main.TIC");
        tokenizeAndRun(code, builder);
        Variable.decrease_scope();

        builder.append((new AssemblyLine("")));
        builder.append_tag("ProgramExit");
        builder.append_call("_GetKey");
        builder.append_call("_ClrScrnFull");
        builder.append_res("donePrgm","(iy+doneFlags)");
        builder.append_ld("SP", "(StackSave)");
        builder.append_ret();
        builder.append(customAssemblyLine("#include \"asm/api.asm\""));
        builder.append_tag("StackSave");
        builder.append_db("0,0,0");
        builder.append_tag("CallStack");
        builder.append_db("0,0,0");
        builder.append(customAssemblyLine(DataStack.STACK_START + " .equ saveSScreen+" + (768 - Call_stack.max_size)));
        builder.append(customAssemblyLine("callStackStart" + " .equ saveSScreen+768"));


        IOUtil.writeTxt("asm/main.asm", AssemblyCollapse.optimize(builder, OPTIMIZE_LEVEL).getAssembly() + "\n" + Constants.getAssembly());
    }

    public static void tokenizeAndRun(String code, AssemblyBuilder builder){
        List<List<Token>> tokenLines = Token.tokenizeLines(code);
        for (List<Token> tokens : tokenLines) {
            builder.append(new AssemblyLine(""));
            System.out.println(tokens);
            builder.tIC_line++;
            globalReader.read(tokens, builder);
        }
        builder.tIC_line++;
    }

    private static void init(){
        NativeFunction.init();
        DataType.staticInit();
    }

    private static long id = 0;
    public static String getId(){
        return String.valueOf(id++);
    }
}