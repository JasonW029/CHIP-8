
public class Processor {
	
	byte[] V = new byte[16];  // var registers 0 thru F
	short indexReg;  // points at locations in RAM
	short pc;  // program counter (points at current instruction)
	short[] stack;  // 16-item deep stack
	byte delayTimer;
	byte soundTimer;
	
	public Processor() {
	}
	
	public short fetch(Chip8 chip8) {
		// combine two 8-bit instructions to get a 16-bit opcode
		short opcode = (short) ((chip8.getFromRAM(pc) << 8) & 0xff | chip8.getFromRAM(pc + 1) & 0xff);
		byte firstByte = chip8.getFromRAM(pc);
		byte secondByte = chip8.getFromRAM(pc + 1);
		pc += 2;
		return opcode;
	}

	public void printHexByte(byte b) {
		System.out.println(getHexString(b));
	}
	
	public void printHexShort(short s) {
		byte firstByte = (byte) (((s & 0xFF00) >>> 8) & 0xFF);
		byte secondByte = (byte) (s & 0x00FF);
		System.out.println(getHexString(firstByte) + getHexString(secondByte));
	}
	
	public String getHexString(byte b) {
		if (b >>> 7 == 1) {
			return Integer.toHexString(~(b - 1) & 0xff);
		} else {
			return Integer.toHexString(b & 0xff);
		}
	}
	
	public void decode(Chip8 chip8, short opcode) {
		printHexShort(opcode);
		byte firstNybble = (byte) ((opcode & 0xF000) >>> 12);
		printHexByte(firstNybble);
		byte secondNybble = (byte) ((opcode & 0x0F00) >>> 8);
		printHexByte(secondNybble);
		byte thirdNybble = (byte) ((opcode & 0x00F0) >>> 4);
		printHexByte(thirdNybble);
		byte fourthNybble = (byte) (opcode & 0x000F);
		printHexByte(fourthNybble);
		
		switch (firstNybble) {
			case 0x0:
				if (secondNybble != 0x0) {
					throw new UnsupportedOperationException("'call' not handled!");
				} else if (fourthNybble == 0x0) {
					throw new UnsupportedOperationException("'cls' not handled!");
				} else if (fourthNybble == 0xE) {
					throw new UnsupportedOperationException("'return' not handled!");
				}
				break;
			case 0x1:
				throw new UnsupportedOperationException("'jump' not handled!");
				// break;
			case 0x2:
				throw new UnsupportedOperationException("'call' not handled!");
				// break;
			case 0x3:
				throw new UnsupportedOperationException("'skip-eq-num' not handled!");
				// break;
			case 0x4:
				throw new UnsupportedOperationException("'skip-neq-num' not handled!");
				// break;
			case 0x5:
				throw new UnsupportedOperationException("'skip-eq-reg' not handled!");
				// break;
			case 0x6:
				throw new UnsupportedOperationException("'set-num' not handled!");
				// break;
			case 0x7:
				throw new UnsupportedOperationException("'add-num-nocarry' not handled!");
				// break;
			case 0x8:  // arithmetic opcodes
				switch (fourthNybble) {
					case 0x0: // set
						V[secondNybble] = V[thirdNybble];
						break;
					case 0x1: // logical OR
						V[secondNybble] = (byte) (V[secondNybble] & 0xf | V[thirdNybble] & 0xf);
						break;
					case 0x2:  // logical AND
						V[secondNybble] = (byte) (V[secondNybble] & 0xf & V[thirdNybble] & 0xf);
						break;
					case 0x3:  // logical XOR
						V[secondNybble] = (byte) (V[secondNybble] & 0xf ^ V[thirdNybble] & 0xf);
						break;
					case 0x4:  // add w/ carry
						V[0xF] = 0;
						int sum = (V[secondNybble] & 0xf + V[thirdNybble] & 0xf);
						V[secondNybble] = (byte) sum;
						if (sum > 255) {  // invoke carry flag
							V[0xF] = 1;
						}
						break;
					case 0x5:  // subtract w/ underflow
						V[0xF] = 0;
						if ((V[secondNybble] & 0xf) > (V[thirdNybble] & 0xf)) {  // invoke carry flag
							V[0xF] = 1;
						}
						int difference = (V[secondNybble] & 0xf - V[thirdNybble] & 0xf);
						V[secondNybble] = (byte) difference;
						break;
					case 0x6:  // bit-shift right by 1
						byte leastSigBit = (byte) (V[secondNybble] & 0x1);
						V[secondNybble] = (byte) (V[secondNybble] >>> 1);
						V[0xF] = leastSigBit;
						break;
					case 0x7:  // reverse subtract w/ underflow
						V[0xF] = 0;
						if ((V[thirdNybble] & 0xf) > (V[secondNybble] & 0xf)) {  // invoke carry flag
							V[0xF] = 1;
						}
						int differenceReverse = (V[thirdNybble] & 0xf - V[secondNybble] & 0xf);
						V[secondNybble] = (byte) differenceReverse;
						break;
					case 0xE:  // bit-shift left by 1
						byte mostSigBit = (byte) ((V[secondNybble] & 0b10000000) >>> 7);
						V[secondNybble] = (byte) (V[secondNybble] << 1);
						V[0xF] = mostSigBit;
						break;
				}
			case 0x9:
				throw new UnsupportedOperationException("'skip-neq-reg' not handled!");
				// break;
			case 0xA:
				throw new UnsupportedOperationException("'set-index' not handled!");
				// break;
			case 0xB:
				throw new UnsupportedOperationException("'jump + offset of V0' not handled!");
				// break;
			case 0xC:
				throw new UnsupportedOperationException("'bitwise-and-random' not handled!");
				// break;
			case 0xD:
				throw new UnsupportedOperationException("'draw-sprite' not handled!");
				// break;
			case 0xE:
				if (thirdNybble == 0x9) {
					throw new UnsupportedOperationException("'skip-key-pressed' not handled!");
					// break;
				} else if (thirdNybble == 0xA) {
					throw new UnsupportedOperationException("'skip-key-not-pressed' not handled!");
					// break;
				}
			case 0xF:
				if (thirdNybble == 0x0) {
					if (fourthNybble == 0x7) {
						throw new UnsupportedOperationException("'set-reg-to-delay' not handled!");
						// break;
					} else if (fourthNybble == 0xA) {
						throw new UnsupportedOperationException("'set-reg-key-blocking' not handled!");
						// break;
					}
				} else if (thirdNybble == 0x1) {
					if (fourthNybble == 0x5) {
						throw new UnsupportedOperationException("'set-delay-timer' not handled!");
						// break;
					} else if (fourthNybble == 0x8) {
						throw new UnsupportedOperationException("'set-sound-timer' not handled!");
						// break;
					} else if (fourthNybble == 0xE) {
						throw new UnsupportedOperationException("'add-reg-to-index-nocarry' not handled!");
						// break;
					}
				} else if (thirdNybble == 0x2) {
					throw new UnsupportedOperationException("'set-index-sprite' not handled!");
					// break;
				} else if (thirdNybble == 0x3) {
					throw new UnsupportedOperationException("'binary-coded-decimal' not handled!");
					// break;
				} else if (thirdNybble == 0x5) {
					throw new UnsupportedOperationException("'store-reg-to-mem' not handled!");
					// break;
				} else if (thirdNybble == 0x6) {
					throw new UnsupportedOperationException("'fill-reg-from-mem' not handled!");
					// break;
				}
			default:
				throw new UnsupportedOperationException("Opcode " + Integer.toHexString((opcode & 0xffff)) + " not handled!");
		}
	}
} // Processor
