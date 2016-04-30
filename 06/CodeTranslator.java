import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  responsible on translating specific commands. this object
 *  is able to translate 'dest' commands, 'jump' commands and 'comute' commands.
 *  each command is being translated separately when given.
 */
public class CodeTranslator {

    /* constants */
    private static final String RETURN_TO_NULL_INPUT = "000";
    private static final char jumpCmdBegin = 'J';
    private static final Pattern COMMAND = Pattern.compile(
			"(?<lhs>[DMA])?(?<op>[\\+\\-&!\\|])?(?:(?<rhs>[01DMA])|(?<shift><<|>>))");

    private static final int A_VAL = 2, OP_BIT = 7, RES_NG = 8, D_ZR_BIT = 3, D_NG_BIT = 4, A_ZR_BIT = 5, A_NG_BIT = 6,
                             SHIFT_MARK = 0, SHIFT_DIRECT = 3, SHIFT_D = 4;
    private static final int ZR_BIT_INDEX = 0, NG_BIT_INDEX = 1;
    private static final String D_BIT = "D", A_BIT = "A", M_BIT = "M";

    private static Map<String, Integer[]> cmdIndex;
    static {
        cmdIndex = new HashMap<>();
        cmdIndex.put(D_BIT, new Integer[]{D_ZR_BIT, D_NG_BIT});
        cmdIndex.put(A_BIT, new Integer[]{A_ZR_BIT, A_NG_BIT});
        cmdIndex.put(M_BIT, new Integer[]{A_ZR_BIT, A_NG_BIT});
    }

    /* static */

    /* C-tors */
    public CodeTranslator() {}

    /* methods */
	/**
	 *  translating dest instruction, given as String.
	 *  return String of binary representation
	 */
    public static String translateDest(String dest) throws Exception {
        char Dbit = '0', Abit = '0', Mbit = '0';

        if (dest == null) {
            return RETURN_TO_NULL_INPUT;
        }

        for (char c : dest.toCharArray()) {
            switch (c) {
                case 'D':
                    Dbit = '1';
                    break;
                case 'A':
                    Abit = '1';
                    break;
                case 'M':
                    Mbit = '1';
                    break;
                case 'n':
                    return String.format("%c%c%c",Abit, Dbit, Mbit);
                default:
                    throw new Exception(String.format("the following charachter cannot be a destination: %s", c));
            }
        }
        return String.format("%c%c%c",Abit, Dbit, Mbit);
    }

	/**
	 *  translating jump instruction, given as String.
	 *  return String of binary representation
	 */
    public static String translateJump(String jmp) throws Exception {
        char lowerBit = '0', greaterBit = '0', equalBit = '0';

        if (jmp == null)
            return RETURN_TO_NULL_INPUT;

        if (jmp.charAt(0) != jumpCmdBegin) {
            throw new Exception("not a proper jump command");
        }

        char firstCondition = jmp.charAt(1);
        if ((firstCondition == 'N') || (firstCondition == 'M')) { // if JNE or JMP
            lowerBit = '1';
            greaterBit = '1';
            if (firstCondition == 'M')
                equalBit = '1';
        }
        else if (firstCondition == 'E') { // if JEQ
            equalBit = '1';
        }
        else if (firstCondition == 'G' || firstCondition == 'L') { //if JGT, JLT, JGE, JLE
            if (firstCondition == 'G')
                greaterBit = '1';
            else
                lowerBit = '1';

            if (jmp.charAt(2) == 'E')
                equalBit = '1';
        }

        return String.format("%c%c%c",lowerBit, equalBit, greaterBit);
    }

	/**
	 *  used to give the other register when one is given.
	 *  return A if D was given, D if A or M was given.
	 */
    private static String getOtherReg(String reg) {
        if (reg.equals(D_BIT))
            return A_BIT;
        return D_BIT;

    }

    /**
     * translate the given command into binary machine language code.
     * the command can  be any possible computatuin which can be written in the assembly hack language.
     * @param comp String containing the operation should be translated.
     * @return String in length 7 containing 0's and 1's in respect to the desired operation
     */
    public static String translateComp(String comp) {
        char[] binCmd = new char[] {'1', '1', '0', '0', '0', '0', '0', '0', '0'};

        Matcher command = COMMAND.matcher(comp);
        command.find();
        if (command.group("lhs") != null && command.group("rhs") != null){ // two variables operation recognized
            if ((!command.group("op").equals("&")) && !command.group("op").equals("|"))
                binCmd[OP_BIT] = '1';

            if (command.group("rhs").equals("1")) {
                binCmd[cmdIndex.get(getOtherReg(command.group("lhs")))[ZR_BIT_INDEX]] = '1';
                binCmd[cmdIndex.get(getOtherReg(command.group("lhs")))[NG_BIT_INDEX]] = '1';
                if (command.group("op").equals("+")) {
                    binCmd[cmdIndex.get(command.group("lhs"))[NG_BIT_INDEX]] = '1';
                    binCmd[RES_NG] = '1';
                }
            if (command.group("lhs").equals(M_BIT) || command.group("rhs").equals(M_BIT))
                binCmd[A_VAL] = '1';


            } else {
                if (command.group("op").equals("|") || command.group("op").equals("-")) {
                    binCmd[cmdIndex.get(command.group("lhs"))[NG_BIT_INDEX]] = '1';
                    binCmd[RES_NG] = '1';
                    if (command.group("op").equals("|")) {
                        binCmd[cmdIndex.get(command.group("rhs"))[NG_BIT_INDEX]] = '1';
                    }
                }
            }
            if (command.group("lhs").equals(M_BIT) || command.group("rhs").equals(M_BIT))
                binCmd[A_VAL] = '1';
        } else if (command.group("rhs") != null) {
            if (command.group("rhs").equals("0")) {
                binCmd[cmdIndex.get(D_BIT)[ZR_BIT_INDEX]] = '1';
                binCmd[cmdIndex.get(A_BIT)[ZR_BIT_INDEX]] = '1';
                binCmd[OP_BIT] = '1';
            } else {
                if (!command.group("rhs").equals(D_BIT)) {
                    binCmd[cmdIndex.get(D_BIT)[ZR_BIT_INDEX]] = '1';
                    binCmd[cmdIndex.get(D_BIT)[NG_BIT_INDEX]] = '1';
                }
                if (!command.group("rhs").equals(A_BIT) && (!command.group("rhs").equals(M_BIT))) {
                    binCmd[cmdIndex.get(A_BIT)[ZR_BIT_INDEX]] = '1';
                    binCmd[cmdIndex.get(A_BIT)[NG_BIT_INDEX]] = '1';
                }
                if (command.group("op") != null) {
                    if (command.group("op").equals("-"))
                        binCmd[OP_BIT] = '1';
                    if (command.group("rhs").equals("1"))
                        binCmd[cmdIndex.get(A_BIT)[NG_BIT_INDEX]] = '0';
                    else
                        binCmd[RES_NG] = '1';
                } else if (command.group("rhs").equals("1")) {
                    binCmd[OP_BIT] = '1';
                    binCmd[RES_NG] = '1';
                }
                if (command.group("rhs").equals(M_BIT))
                    binCmd[A_VAL] = '1';
            }
        } else { // shift operation given
            binCmd[SHIFT_MARK] = '0';
            if (command.group("shift").equals("<<"))
                binCmd[SHIFT_DIRECT] = '1';
            if (command.group("lhs").equals(D_BIT)) {
                binCmd[SHIFT_D] = '1';
            }
            if (command.group("lhs").equals(M_BIT))
                binCmd[A_VAL] = '1';
        }
        return new String (binCmd);
    }
	
	/**
	 *  translating decimal numbers into binary.
	 *  negative integers are represented using the 2's complement method.
	 */
    public static String translateDecToBin(int value) {
        String binInt = Integer.toBinaryString(value);
        String result;
        try {
            if (value >= 0)
                result = new String(new char[16 - binInt.length()]).replace("\0", "0");
            else
                result = new String(new char[16 - binInt.length()]).replace("\0", "1");
        } catch (NegativeArraySizeException e){
            binInt = binInt.substring(binInt.length() - 16);
            result = new String(new char[16 - binInt.length()]).replace("\0", "1");

        }

        return result + binInt;
    }

	/**
	 *  translating decimal numbers into binary.
	 *  negative integers are represented using the 2's complement method.
	 */
    public static String translateDecToBin(String value) {
        return translateDecToBin(Integer.parseInt(value));
    }
}
