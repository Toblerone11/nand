import java.util.HashMap;
import java.util.Map;

/**
 *  helps the Assembler to keep track on the already defined symbols.
 *  it is used to keep labels which points to location at the instruction memory
 *  and variables which points on RAM addresses.
 */
public class SymbolTable {

    private Map<String, Integer> table;

    // Ctor - init
    public SymbolTable()
    {
        table = new HashMap<String, Integer>();
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
        for (int i = 0; i < 16; i++)
        {
            table.put("R" + String.valueOf(i), i);
        }
    }

    // Add entry to the table
    public void addEntry(String symbol, int address)
    {
        table.put(symbol, address);
    }

    // Check if the symbol exits in the table
    public boolean contains(String symbol)
    {
        return table.containsKey(symbol);
    }

    // Get the address of the symbol
    public int getAddress(String symbol)
    {
        return table.get(symbol);
    }

}
