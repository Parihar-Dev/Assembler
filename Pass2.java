package Assembler;

import java.util.*;
import java.io.*;

class tableentry {
    public int index;
    public String name;
    public int address;
    public tableentry(int index, String name, int address) {
        this.index = index;
        this.name = name;
        this.address = address;
    }
    @Override
    public String toString() {
        return "{"+index+","+name+","+address+"}";
    }
}
class IntermediateLine {
    int loc_cntr;
    String opType;
    String opCode;
    String operand1Type;
    String operand1Value;
    String operand2Type;
    String operand2Value;

    public IntermediateLine(int loc_cntr,String opType, String opCode, String operand1Type, String operand1Value, String operand2Type, String operand2Value) {
        this.loc_cntr = loc_cntr;
        this.opType = opType;
        this.opCode = opCode;
        this.operand1Type = operand1Type;
        this.operand1Value = operand1Value;
        this.operand2Type = operand2Type;
        this.operand2Value = operand2Value;
    }

    @Override
    public String toString() {
        return loc_cntr + " (" + opType + "," + opCode + ")(" + operand1Type + "," + operand1Value + ")" +
               (operand2Type != null ? "(" + operand2Type + "," + operand2Value + ")" : "");
    }
}

class Pass2 {
    public static void main(String[] args) {
        List<tableentry> symbolTable = new ArrayList<>();
        List<tableentry> literalTable = new ArrayList<>();
        List<IntermediateLine> intermediateLine = new ArrayList<>();
        
        // Reading symbol table
        try (Scanner symbolScanner = new Scanner(new File("symbol_table.txt"))) {
            symbolScanner.nextLine(); // Skip header line
            while (symbolScanner.hasNextLine()) {
                String[] parts = symbolScanner.nextLine().replace("{", "").replace("}", "").split(",");
                int index = Integer.parseInt(parts[0]);
                String symbol = parts[1];
                int address = Integer.parseInt(parts[2]);
                symbolTable.add(new tableentry(index, symbol, address));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Symbol table file not found");
            return;
        }

        // Reading literal table
        try (Scanner literalScanner = new Scanner(new File("literal_table.txt"))) {
            literalScanner.nextLine(); // Skip header line
            while (literalScanner.hasNextLine()) {
                String[] parts = literalScanner.nextLine().replace("{", "").replace("}", "").split(",");
                int index = Integer.parseInt(parts[0]);
                String literal = parts[1];
                int address = Integer.parseInt(parts[2]);
                literalTable.add(new tableentry(index, literal, address));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Literal table file not found");
            return;
        }

        try (BufferedWriter machineCodeWriter = new BufferedWriter(new FileWriter("machine_code.txt"));
             Scanner scanner = new Scanner(new File("intermediate.txt"))) {
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                int loc_cntr = Integer.parseInt(parts[0]);

                if (parts.length < 2) {
                    continue;
                }

                String[] operands = parts[1].split("\\)\\(");
                operands[0] = operands[0].replace("(", "");
                operands[operands.length - 1] = operands[operands.length - 1].replace(")", "");

                String[] opDetails = operands[0].split(",");
                String opType = opDetails[0];
                String opCode = opDetails[1];

                String operand1Type = null;
                String operand1Value = null;
                if (operands.length > 1) {
                    String[] operand1Details = operands[1].split(",");
                    operand1Type = operand1Details[0];
                    operand1Value = operand1Details[1];
                }

                String operand2Type = null;
                String operand2Value = null;
                if (operands.length > 2) {
                    String[] operand2Details = operands[2].split(",");
                    operand2Type = operand2Details[0];
                    operand2Value = operand2Details[1];
                }

                IntermediateLine lineEntry = new IntermediateLine(loc_cntr, opType, opCode, operand1Type, operand1Value, operand2Type, operand2Value);
                intermediateLine.add(lineEntry);
                generateMachineCode(machineCodeWriter, loc_cntr, opType, opCode, operand1Type, operand1Value, operand2Type, operand2Value, symbolTable, literalTable);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Intermediate file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateMachineCode(BufferedWriter writer,int loc_cntr, String opType, String opCode, String operand1Type, String operand1Value, String operand2Type, String operand2Value, List<tableentry> symbolTable, List<tableentry> literalTable ) throws IOException {
        if ("IS".equals(opType)) {
            if (operand1Value != null) {
                writer.write("+"+opCode+" "+operand1Value);
            }
            if (operand2Type != null) {
                if ("S".equals(operand2Type)) {
                    int add = symbolTable.get(Integer.parseInt(operand2Value)).address;
                    writer.write(" "+String.valueOf(add));
                }
                else if ("L".equals(operand2Type)) {
                    int add = literalTable.get(Integer.parseInt(operand2Value)).address;
                    writer.write(" "+String.valueOf(add));
                }
            }
            if ("00".equals(opCode)) {
                writer.write("+00 00 000");
            }
            writer.write("\n");
        }
        else if("DL".equals(opType)) {
            writer.write("+00 00 00"+operand1Value);
            writer.write("\n");
        }
    }
}
