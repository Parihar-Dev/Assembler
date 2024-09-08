# Assembler

Assembler Pass 1 Implementation

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
