package pufferenco;

import java.util.ArrayList;
import java.util.LinkedList;

public class ArrayUtil {
    public static <type> int inArray(type element, type[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element))
                return i;
        }
        return -1;
    }

    public static <type> boolean isInArray(type element, type[] array) {
        return inArray(element, array) != -1;
    }

    public static <type> ArrayList<type> linkedToArrayList(LinkedList<type> list) {
        return new ArrayList<>(list);
    }
}
