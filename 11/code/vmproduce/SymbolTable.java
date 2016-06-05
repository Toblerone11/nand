package vmproduce;

import java.util.HashMap;
import java.util.Map;

import static vmproduce.VmConstants.*;

/**
 * Error free version
 */
public class SymbolTable {

    /* constants */
    private static final int INITIAL_INDEX = 0, NO_INDEX = -1;
    private static final String METHOD = "method", FUNCTION = "function", CTOR = "constructor";

    /* data members */
    private Map<String,SymbolEntry> classVarTable; // {var : details}}
    private Map<String, SymbolEntry> subroutineTable; // {var :  details}}}
    private Map<String, String[]> subroutinesReturnType; // {subroutine : returnType} }
    private Map<String, Integer> kindRunningIndex;

    public SymbolTable() {
        classVarTable = new HashMap<>();
        subroutineTable = new HashMap<>();
        subroutinesReturnType = new HashMap<>();
        kindRunningIndex = new HashMap<>();

        kindRunningIndex.put(STATIC_KIND, INITIAL_INDEX);
        kindRunningIndex.put(FIELD_KIND, INITIAL_INDEX);
        kindRunningIndex.put(ARG, INITIAL_INDEX);
        kindRunningIndex.put(VAR_KIND, INITIAL_INDEX);
    }

    private void updateIndex(String kind) {
        kindRunningIndex.put(kind, kindRunningIndex.get(kind)+ 1);
    }

    private SymbolEntry getVarEntry(String varName) {
        if (subroutineTable.containsKey(varName)) {
            return subroutineTable.get(varName);
        }
        else if (classVarTable.containsKey(varName)) {
            return classVarTable.get(varName);
        }
        return null;
    }

    public void startSubroutine() {
        subroutineTable.clear();
        kindRunningIndex.put(ARG, INITIAL_INDEX);
        kindRunningIndex.put(VAR_KIND, INITIAL_INDEX);
    }

    public void define(String varName, String type, String kind) {
        int newIndex = kindRunningIndex.get(kind);
        updateIndex(kind);

        SymbolEntry varEntry = new SymbolEntry(varName, type, kind, newIndex);
        if (kind.equals(FIELD_KIND) || kind.equals(STATIC_KIND)) {
            classVarTable.put(varName, varEntry);
        } else if (kind.equals(ARG) || kind.equals(VAR_KIND)) {
            subroutineTable.put(varName, varEntry);
        }
    }

    public void defineSubroutine(String subroutineName, String subroutineType, String returnType) {
        String[] subroutineValues = new String[] {subroutineType, returnType};
        subroutinesReturnType.put(subroutineName, subroutineValues);
    }

    public String getSubroutineReturnType(String subroutineName) {
        if (subroutinesReturnType.containsKey(subroutineName)) {
            return subroutinesReturnType.get(subroutineName)[1];
        }
        return null;
    }

    public boolean isMethod(String subroutineName) {
        String[] subroutineValues = subroutinesReturnType.get(subroutineName);
        if (subroutineValues == null) {
            return false;
        }
        return METHOD.equals(subroutineValues[0]);
    }

    public boolean isCtor(String subroutineName) {
        return CTOR.equals(subroutinesReturnType.get(subroutineName)[0]);
    }

    public boolean isFunction(String subroutineName) {
        return FUNCTION.equals(subroutinesReturnType.get(subroutineName)[0]);
    }

    public int getVarCount(String kind) {
        return kindRunningIndex.get(kind);
    }

    public String getTypeOf(String varName) {
        SymbolEntry varEntry = getVarEntry(varName);
        if (varEntry != null) {
            return varEntry.getType();
        }
        return null;
    }

    public String getKindOf(String varName) {
        SymbolEntry varEntry = getVarEntry(varName);
        if (varEntry != null) {
            return varEntry.getKind();
        }
        return null;
    }

    public int getIndexOf(String varName) {
        SymbolEntry varEntry = getVarEntry(varName);
        if (varEntry != null) {
            return varEntry.getIndex();
        }
        return NO_INDEX;
    }

    public boolean isClassName(String name) {
        return subroutinesReturnType.containsKey(name);
    }

    /**
     * a nested class represent a symbol with all of its definition details.
     */
    private class SymbolEntry {

        /* data members */
        private String name, type, kind;
        private int index;

        /**
         * C-tor
         * @param name the name of the symbol
         * @param type type of the symbol: int, bool, char or <className>
         * @param kind the scope where the symbol lives: static, field, argument or local
         * @param index the index given to the symbol.
         */
        SymbolEntry(String name, String type, String kind, int index) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public String getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }



    }

}
