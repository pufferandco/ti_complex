package pufferenco;

import java.util.HashMap;

public class Function {
    private static final HashMap<String, Function> Functions = new HashMap<>();
    public static boolean exists(String name){
        return Functions.containsKey(name);
    }
}
