# Assembler

**Assembler Pass 1 Implementation**

This repository contains the implementation of Pass 1 of a two-pass assembler. The assembler reads an assembly code file (input.txt) and processes it using the machine operation table (MOT) from mot.txt. It generates intermediate code, symbol tables, and literal tables as part of its operation.

Features:

1. Tokenization: Splits assembly instructions into tokens using both spaces and commas.
2. MOT Lookup: Fetches information about machine operations from an external MOT file.
3. Symbol Table: Tracks all labels and symbols used in the assembly code.
4. Literal Table: Identifies and manages literals in the code.
5. Intermediate Code Generation: Produces intermediate code for further processing in Pass 2 of the assembler.

Files:

1. mot.txt: Machine Operation Table containing opcode and instruction details.
2. source.txt: Assembly program input file.
3. Pass1.java: Main implementation of Pass 1, handling all the processing.
4. symbol_table.txt: Output file containing the symbol table.
5. literal_table.txt: Output file containing the literal table.
6. intermediate.txt: Output file with the generated intermediate code.

**Assembler Pass 2 Implementation**

This repository contains the implementation of Pass 2 of a two-pass assembler. In Pass 2, the assembler takes the intermediate code, symbol table, and literal table generated in Pass 1 and produces the final machine code.

Features:

1. Intermediate Code Processing: Reads the intermediate code generated in Pass 1.
2. Symbol and Literal Resolution: Resolves addresses for symbols and literals using the symbol table and literal table from Pass 1.
3. Machine Code Generation: Converts the intermediate code into actual machine code based on the resolved addresses.
4. Error Handling: Detects and reports any errors, such as undefined symbols.

Files:

1. intermediate.txt: Input file containing the intermediate code generated from Pass 1.
2. symbol_table.txt: Input file containing the symbol table generated from Pass 1.
3. literal_table.txt: Input file containing the literal table generated from Pass 1.
4. mot.txt: Machine Operation Table used for instruction lookups.
5. machine_code.txt: Output file containing the final machine code.
