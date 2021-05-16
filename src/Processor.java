
import javax.swing.*;
import java.util.Stack;

public class Processor {
	
	byte[] V = new byte[16];  // var registers 0 thru F
	short indexReg;  // points at locations in RAM
	short pc;  // program counter (points at current instruction)
	Stack<Short> stack; // 16-item deep stack
	byte delayTimer;
	byte soundTimer;
	
	public Processor() {
	}
	
	public short fetch(Chip8 chip8) {
		// combine two 8-bit instructions to get a 16-bit opcode
		short opcode = (short) ((chip8.getFromRAM(pc) << 8) & 0xffff | chip8.getFromRAM(pc + 1) & 0xff);
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
		return Integer.toHexString(b & 0xff);
	}

	public String getHexString(short s) {
		return Integer.toHexString(s & 0xffff);
	}
	
	public byte getHighestOrderBit(byte b) {
		return (byte) ((b & 0b10000000) >>> 7);
	}

	public byte bitshiftRight(byte b) {
		int shifted_b = b >>> 1;
		// the leading sign bit may not be accurate due to int widening, so we will force it to be 0
		return (byte) (shifted_b & 0b01111111);
	}

	public byte bitshiftLeft(byte b) {
		int shifted_b = b << 1;
		return (byte) (shifted_b);
	}
	
	public void decode(Chip8 chip8, short opcode) {
		printHexShort(opcode);
		// note: nthNybble variables are always unsigned because they only take up 4 bits (not 8)
		byte firstNybble = (byte) ((opcode & 0xF000) >>> 12);
		printHexByte(firstNybble);
		byte secondNybble = (byte) ((opcode & 0x0F00) >>> 8);
		printHexByte(secondNybble);
		byte thirdNybble = (byte) ((opcode & 0x00F0) >>> 4);
		printHexByte(thirdNybble);
		byte fourthNybble = (byte) (opcode & 0x000F);
		printHexByte(fourthNybble);
		
		// Note that any bitwise operation or boolean operation on bytes cause implicit widening
		// into ints, so any bytes with a 1 in front will be carried into any inserted bits (so
		// 0b10000000 will be cast to 0b111111111111111111111111110000000) so if we do not ignore the
		// inserted bits, if we cast into anything bigger than a byte (e.g. a short), we will have a 
		// wrong value (since in reality, those 1s should really be 0s). Thus, to recast into a 
		// short (or larger) we have to only retrieve the lowest n bits (AND with 0x(n * 'f')).

		// IBM Logo Required Opcodes:
		// 00E0 cls
		// 1NNN jump
		// 6XNN set
		// 7XNN add
		// ANNN set I
		// DXYN display

		switch (firstNybble) {
			case 0x0:
				if (secondNybble != 0x0) {
					throw new UnsupportedOperationException("'call' not handled!");
				} else if (fourthNybble == 0x0) {  // cls
					// request EDT to clear screen - may not happen instantly
					SwingUtilities.invokeLater(() -> chip8.display.clearScreen());
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
			case 0x6: // set-register-to-num
				chip8.cpu.V[secondNybble] = (byte) (((thirdNybble << 4) & 0xf0) | (fourthNybble & 0x0f));
				break;
			case 0x7: { // add-num-nocarry
				int sum = (chip8.cpu.V[secondNybble] & 0xff) + (((thirdNybble << 4) & 0xf0) | (fourthNybble & 0x0f));
				// note: if sum overflows out of a byte, we do not care about the carry bit
				// the shortening from int to byte may lose info, but this opcode behaves this way
				chip8.cpu.V[secondNybble] = (byte) sum;
				break;
			}
			case 0x8:  // arithmetic opcodes
				// note that any boolean operations implicit widen the bytes into ints, but when casting
				// back into a byte, all but the lowest 8 bits are discarded so the outcome still results
			    // in expected behaviour
				switch (fourthNybble) {
					case 0x0: // set
						V[secondNybble] = V[thirdNybble];
						break;
					case 0x1: // logical OR
						V[secondNybble] = (byte) (V[secondNybble] | V[thirdNybble]);
						break;
					case 0x2:  // logical AND
						V[secondNybble] = (byte) (V[secondNybble] & V[thirdNybble]);
						break;
					case 0x3:  // logical XOR
						V[secondNybble] = (byte) (V[secondNybble] ^ V[thirdNybble]);
						break;
					case 0x4:  // add w/ carry
						V[0xF] = 0;
						int sum = ((V[secondNybble] & 0xff) + (V[thirdNybble] & 0xff));
						V[secondNybble] = (byte) sum;
						if (sum > 255) {  // invoke carry flag
							V[0xF] = 1;
						}
						break;
					case 0x5:  // subtract w/ underflow
						V[0xF] = 0;
						if ((V[secondNybble] & 0xff) > (V[thirdNybble] & 0xff)) {  // invoke carry flag
							V[0xF] = 1;
						}
						int difference = ((V[secondNybble] & 0xff) - (V[thirdNybble] & 0xff));
						V[secondNybble] = (byte) difference;
						break;
					case 0x6:  // bit-shift right by 1
						byte leastSigBit = (byte) (V[secondNybble] & 0x1);
						V[secondNybble] = bitshiftRight(V[secondNybble]);
						V[0xF] = leastSigBit;
						break;
					case 0x7:  // reverse subtract w/ underflow
						V[0xF] = 0;
						if ((V[thirdNybble] & 0xff) > (V[secondNybble] & 0xff)) {  // invoke carry flag
							V[0xF] = 1;
						}
						int differenceReverse = ((V[thirdNybble] & 0xff) - (V[secondNybble] & 0xff));
						V[secondNybble] = (byte) differenceReverse;
						break;
					case 0xE:  // bit-shift left by 1
						byte mostSigBit = (byte) ((V[secondNybble] >>> 7) & 0x1);
						V[secondNybble] = bitshiftLeft(V[secondNybble]);
						V[0xF] = mostSigBit;
						break;
				}
				break;
			case 0x9:
				throw new UnsupportedOperationException("'skip-neq-reg' not handled!");
				// break;
			case 0xA: // set-index-register
				chip8.cpu.indexReg = (short) (((secondNybble << 8) & 0xf00) |
						((thirdNybble << 4) & 0x0f0) | (fourthNybble & 0x00f));
				break;
			case 0xB:
				throw new UnsupportedOperationException("'jump + offset of V0' not handled!");
				// break;
			case 0xC:
				throw new UnsupportedOperationException("'bitwise-and-random' not handled!");
				// break;
			case 0xD: // draw-sprite
				chip8.cpu.V[0xF] = 0;
				int x = (V[secondNybble] & 0xff) % chip8.display.SCREEN_WIDTH; // x-coord's start position wraps
				int y = (V[thirdNybble] & 0xff) % chip8.display.SCREEN_HEIGHT; // y-coord's start position wraps
				int spriteHeight = fourthNybble & 0xff;

				byte[] spriteList = new byte[spriteHeight]; // holds all sprite to be written in row order
				// fill spriteList with sprite data
				for (int i = 0; i < spriteHeight; ++i) {
					spriteList[i] = chip8.RAM[indexReg + i];
				}
				// draw sprite to display
				V[0xF] = (byte) (chip8.display.drawSprite(spriteList, x, y, spriteHeight) ? 1 : 0);
				break;
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
