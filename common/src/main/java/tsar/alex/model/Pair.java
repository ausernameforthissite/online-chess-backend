package tsar.alex.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Array;



@Getter
@ToString
public class Pair<T> {
    private final static int ELEMENTS_IN_PAIR = 2;

    private T[] pairArray;

    public Pair(@JsonProperty("pairArray") T[] pairArray) {
        if (pairArray.length == 2) {
            this.pairArray = pairArray;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public Pair(T object1, T object2) {
        pairArray = (T[]) Array.newInstance(object1.getClass(), ELEMENTS_IN_PAIR);
        pairArray[0] = object1;
        pairArray[1] = object2;
    }

    public T get(int index) {
        if (index >= 0 && index < ELEMENTS_IN_PAIR) {
            return (T) pairArray[index];
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void set(int index, T object) {
        if (index >= 0 && index < ELEMENTS_IN_PAIR) {
            pairArray[index] = object;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}

