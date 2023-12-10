package pufferenco;

import pufferenco.variables.RegisterManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static pufferenco.AssemblyLine.customAssemblyLine;

public class AssemblyBuilder {
    public LinkedList<AssemblyLine> lines = new LinkedList<>();
    private final Stack<String> current_function = new Stack<>();
    private static final Stack<String> current_line = new Stack<>();
    private boolean locked = false;
    public int max_stack_size = 0;

    public AssemblyLine append(AssemblyLine line) {
        if (locked)
            error("attempted to write to locked builder");

        lines.add(line);
        return line;
    }

    public void appendList(List<AssemblyLine> list) {
        if (locked)
            error("attempted to write to locked builder");

        lines.addAll(list);

    }

    public String getAssembly() {
        locked = true;
        StringBuilder builder = new StringBuilder();
        for (AssemblyLine line : lines) {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    public void error(String error_code) {
        throw new RuntimeException("error at " + current_function.peek() + ": " + error_code + "\n" + "\t\tat: " + current_line.peek().trim());
    }

    public void newLine(List<Token> line){
        StringBuilder builder = new StringBuilder();
        for (Token token : line) {
            builder.append(token).append(" ");
        }
        current_line.push(builder.toString());
    }

    public void closeLine(){
        current_line.pop();
    }

    public void add_func(String hint) {
        current_function.push(hint);

    }

    public void remove_func() {
        current_function.pop();

    }

    //public AssemblyLine append_ld(String left, String right, String name){
    //    append(new AssemblyLine("ld", left, right));
    //    if(RegisterManager.isRegister(left))
    //        RegisterManager.setValue(left, name);
    //}
    public AssemblyLine append_ld(String left, String right) {
        if (RegisterManager.isRegister(left))
            RegisterManager.clearValue(left);
        return append(new AssemblyLine("ld", left, right));
    }

    public AssemblyLine append_db(String content) {
        return append(new AssemblyLine(".db", content));
    }
    public AssemblyLine append_dw(String content) {
        return append(new AssemblyLine(".dw", content));
    }

    public AssemblyLine append_tag(String name) {
        return append(customAssemblyLine(name + ":"));
    }

    public AssemblyLine append_push(String register) {
        max_stack_size += 3;
        return append(new AssemblyLine("push", register));
    }

    public AssemblyLine append_pop(String register) {
        return append(new AssemblyLine("pop", register));
    }

    //public AssemblyLine append_pop(String register, String name){
    //    append(new AssemblyLine("pop", register));
    //}
    public AssemblyLine append_call(String pointer) {
        max_stack_size += 3;
        return append(new AssemblyLine("call", pointer));
    }

    public AssemblyLine append_ret() {
        return append(new AssemblyLine("ret"));
    }

    public AssemblyLine append_res(String left, String right) {
        return append(new AssemblyLine("res", left, right));
    }

    public AssemblyLine append_add(String left, String right) {
        return append(new AssemblyLine("add", left, right));
    }

    public AssemblyLine append_sub(String left, String right) {
        return append(new AssemblyLine("sub", left, right));
    }

    public AssemblyLine append_sbc(String left, String right) {
        return append(new AssemblyLine("sbc", left, right));
    }

    public AssemblyLine append_ex(String left, String right) {
        return append(new AssemblyLine("ex", left, right));
    }


    public AssemblyLine append_mlt(String rr) {
        return append(new AssemblyLine("mlt", rr));
    }

    public AssemblyLine append_and(String operand1, String operand2) {
        return append(new AssemblyLine("and", operand1, operand2));
    }

    public AssemblyLine append_or(String operand1, String operand2) {
        return append(new AssemblyLine("or", operand1, operand2));
    }

    public AssemblyLine append_xor(String operand1, String operand2) {
        return append(new AssemblyLine("xor", operand1, operand2));
    }

    public AssemblyLine append_jp(String statement, String pointer) {
        return append(new AssemblyLine("jp", statement, pointer));
    }

    public AssemblyLine append_jp(String pointer) {
        return append(new AssemblyLine("jp", pointer));
    }
    public AssemblyLine append_jr(String pointer) {
        return append(new AssemblyLine("jr", pointer));
    }

    public AssemblyLine append_jr(String statement, String pointer) {
        return append(new AssemblyLine("jr", statement, pointer));
    }


    public AssemblyLine append_cp(String left, String right) {
        return append(new AssemblyLine("cp", left, right));
    }

    public AssemblyLine append_inc(String operand) {
        return append(new AssemblyLine("inc", operand));
    }
    public AssemblyLine append_dec(String operand) {
        return append(new AssemblyLine("dec", operand));
    }

}
