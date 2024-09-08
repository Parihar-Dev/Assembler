package Assembler;

import java.util.*;
import java.io.*;

class intWrapper {
    public int value;
    public intWrapper(int value) {
        this.value = value;
    }
}
class tableentry {
    public int index;
    public String name;
    public int address;
    public tableentry(int index, String name, int address) {
        this.index = index;
        this.name = name;
        this.address = address;
    }
    public String toString() {
        return "{"+index+","+name+","+address+"}";
    }
}
class Pass1 {
    public static void main(String[] args) {
        intWrapper loc_cntr = new intWrapper(0);
        intWrapper littab_ptr = new intWrapper(0);
        List<tableentry> symboltab = new ArrayList<>();
        List<tableentry> littab = new ArrayList<>();
        List<String> pooltab = new ArrayList<>();
        pooltab.add("#"+String.valueOf(littab_ptr.value));
        HashMap<String, String> mot = new HashMap<>();
    
        try (BufferedWriter intermediateWriter = new BufferedWriter(new FileWriter("intermediate.txt"));
             BufferedWriter symbolTableWriter = new BufferedWriter(new FileWriter("symbol_table.txt"));
             BufferedWriter literalTableWriter = new BufferedWriter(new FileWriter("literal_table.txt"));
             BufferedWriter poolTableWriter = new BufferedWriter(new FileWriter("pool_table.txt"))) {
    
            try {
                File motFile = new File("mot.txt");
                Scanner motScanner = new Scanner(motFile);
    
                while (motScanner.hasNextLine()) {
                    String[] motEntry = motScanner.nextLine().split(" ");
                    mot.put(motEntry[0], motEntry[1] + " " + motEntry[2]);
                }
                motScanner.close();
            }
            catch (FileNotFoundException e) {
                System.out.println("Mot file not found");
                return;
            }
    
            try {
                File file = new File("source.txt");
                Scanner scanner = new Scanner(file);
    
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;
    
                    Scanner linescanner = new Scanner(line);
                    String label = null, opcode = null, operand1 = null, operand2 = null;
    
                    if (linescanner.hasNext()) {
                        String firsttoken = linescanner.next();
                        if (!mot.containsKey(firsttoken)) {
                            label = firsttoken;
                            if (linescanner.hasNext()) opcode = linescanner.next();
                        }
                        else {
                            opcode = firsttoken;
                        }
                        if (linescanner.hasNext()) operand1 = linescanner.next();
                        if (linescanner.hasNext()) operand2 = linescanner.next();
                        generateIntermediateCode(intermediateWriter, label, opcode, operand1, operand2, mot, symboltab, littab, loc_cntr, littab_ptr, pooltab);
                        loc_cntr.value += 1;
                    }
                    linescanner.close();
                }
                scanner.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
    
            symbolTableWriter.write("SYMBOL TABLE\n");
            for (tableentry entry : symboltab) {
                symbolTableWriter.write(entry.toString() + "\n");
            }
    
            literalTableWriter.write("LITERAL TABLE\n");
            for (tableentry entry : littab) {
                literalTableWriter.write(entry.toString() + "\n");
            }
    
            poolTableWriter.write("POOLTABLE\n");
            for (String entry : pooltab) {
                poolTableWriter.write(entry + "\n");
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    public static void generateIntermediateCode(BufferedWriter writer, String label, String opcode, String operand1, String operand2, HashMap<String, String> mot, List<tableentry> symboltab, List<tableentry> littab, intWrapper loc_cntr, intWrapper littab_ptr, List<String> pooltab) throws IOException {
        if (label != null) {
            if (getIndex(label, symboltab) == -1) {
                symboltab.add(new tableentry(symboltab.size(), label, loc_cntr.value));
            }
        }
        if (mot.containsKey(opcode)) {
            String[] motInfo = mot.get(opcode).split(" ");
            String opType = motInfo[0];
            String opCode = motInfo[1];
            if (!opcode.equals("LTORG")) {
                writer.write(loc_cntr.value + " ");
                writer.write("(" + opType + "," + opCode + ")");
            }
            if (opType.equals("IS")) {
                if (operand2 != null && !symbolExists(operand2, symboltab) && !operand2.startsWith("=")) {
                    symboltab.add(new tableentry(symboltab.size(), operand2, -1));
                }
            }
            if (opType.equals("DL")) {
                int index = getIndex(label, symboltab);
                symboltab.get(index).address = loc_cntr.value;
                writer.write("(C," + operand1 + ")");
            }
            else {
                if (opcode.equals("START")) {
                    loc_cntr.value = Integer.parseInt(operand1) - 1;
                    writer.write("(C," + operand1 + ")");
                }
                else if (opcode.equals("ORIGIN")) {
                    String[] parts = operand1.split("\\+");
                    String l = parts[0];
                    int immediateValue = Integer.parseInt(parts[1]);
                    int address_of_l = getSymbolAddress(l, symboltab);
                    loc_cntr.value = address_of_l + immediateValue - 1;
                    int address = loc_cntr.value + 1;

                    writer.write("(C," + address + ")");
                }
                else if (opcode.equals("EQU")) {
                    int address = getSymbolAddress(operand1, symboltab);
                    int index = getIndex(label, symboltab);
                    int index2 = getIndex(operand1, symboltab);
                    if (index == -1) {
                        symboltab.add(new tableentry(symboltab.size(), label, address));
                    } else {
                        symboltab.get(index).address = address;
                    }
                    writer.write("(C," + symboltab.get(index2).address + ")");
                }
                else if (opcode.equals("LTORG")) {
                    assignLiteralAddresses(littab, loc_cntr, writer);
                    pooltab.add("#" + String.valueOf(littab_ptr.value));
                }
                else if (opcode.equals("END")) {
                    writer.write("\n");
                    assignLiteralAddresses(littab, loc_cntr, writer);
                }
            }
        }
        if (operand1 != null) {
            if (mot.containsKey(operand1)) {
                String[] motInfo = mot.get(operand1).split(" ");
                writer.write("(" + motInfo[0] + "," + motInfo[1] + ")");
            }
            else if (operand1.startsWith("=")) {
                String literalValue = operand1.substring(2, operand1.length() - 1);
                littab.add(new tableentry(littab_ptr.value++, literalValue, -1));
                int index = getLiteralIndex(operand1, littab);
                writer.write("(L," + index + ")");
            }
            else if (symbolExists(operand2, symboltab)) {
                int index = getIndex(operand2, symboltab);
                writer.write("(S," + index + ")");
            }
        }
        if (operand2 != null) {
            if (mot.containsKey(operand2)) {
                String[] motInfo = mot.get(operand2).split(" ");
                writer.write("(" + motInfo[0] + "," + motInfo[1] + ")");
            }
            else if (operand2.startsWith("=")) {
                String literalValue = operand2.substring(2, operand2.length() - 1);
                littab.add(new tableentry(littab_ptr.value++, literalValue, -1));
                int index = getLiteralIndex(literalValue, littab);
                writer.write("(L," + index + ")");
            }
            else if (symbolExists(operand2, symboltab)) {
                int index = getIndex(operand2, symboltab);
                writer.write("(S," + index + ")");
            }
        }
        writer.write("\n");
    }

    public static void assignLiteralAddresses(List<tableentry> littab, intWrapper loc_cntr, BufferedWriter writer) throws IOException {
        for (tableentry entry : littab) {
            if (entry.address == -1) { 
                writer.write(loc_cntr.value + " (DL,02)" + "(C," + entry.name + ")");
                writer.write("\n");
                entry.address = loc_cntr.value++;
            }
        }
        loc_cntr.value -= 1;
    }

    public static int getSymbolAddress(String label, List<tableentry> symboltab) {
        for (tableentry entry : symboltab) {
            if (entry.name.equals(label)) {
                return entry.address;
            }
        }
        return -1;
    }

    public static int getIndex(String a, List<tableentry> b) {
        for (int i = 0; i < b.size(); i++) {
            if (b.get(i).name.equals(a)) {
                return i;
            }
        }
        return -1;
    }

    public static int getLiteralIndex(String a, List<tableentry> b) {
        for (int i = 0; i < b.size(); i++) {
            if (b.get(i).name.equals(a) && b.get(i).address == -1) {
                return i;
            }
        }
        return -1;
    }
    public static boolean symbolExists(String symbol, List<tableentry> symboltab) {
        for (tableentry entry : symboltab) {
            if (entry.name.equals(symbol)) {
                return true;
            }
        }
        return false;
    }    
}