
import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Stack;

public class Processor {

	byte[] V = new byte[16];  // var registers 0 thru F
	short indexReg;  // points at locations in memory
	short pc;  // program counter (points at current instruction)
	Stack<Short> stack; // 16-item deep stack
	byte delayTimer;
	byte soundTimer;
	Random rand = new Random();
	private Duration timerDeficit = Duration.ZERO; // represents how far behind we are to ticking at exactly 60 fps
	private Duration instrDeficit = Duration.ZERO; // represents how far behind we are to executing opcodes at exactly
												   // targetInstructionsPerSecond
	private Instant lastTimerUpdate = Instant.now();
	private Instant lastInstrUpdate = Instant.now();
	private static final long NANOS_PER_TIMER_TICK = Duration.ofNanos(16666667).toNanos(); // nanos equiv. to 16.67ms
	private final long nanosPerInstr;
	private static final int ONE_MILLION = 1000000000;

	public Processor(short initial_pc, int targetInstructionsPerSecond) {
		this.pc = initial_pc;
		this.indexReg = 0;
		this.stack = new Stack<>();
		this.nanosPerInstr = (long) (1.0/targetInstructionsPerSecond * ONE_MILLION);
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

	private byte bitshiftRight(byte b) {
		int shifted_b = b >>> 1;
		// the leading sign bit may not be accurate due to int widening, so we will force it to be 0
		return (byte) (shifted_b & 0b01111111);
	}

	private byte bitshiftLeft(byte b) {
		int shifted_b = b << 1;
		return (byte) (shifted_b);
	}

	private void updateTimers() {
		Instant now = Instant.now();
		Duration timeSinceLastUpdate = Duration.between(this.lastTimerUpdate, now).plus(this.timerDeficit);
		long nanosSinceLastUpdate = timeSinceLastUpdate.toNanos();
		if (nanosSinceLastUpdate >= NANOS_PER_TIMER_TICK) {
			int intDelayTimer = (delayTimer & 0xff);
			int intSoundTimer = (soundTimer & 0xff);
			if (intDelayTimer > 0) {
				intDelayTimer -= 1;
				this.delayTimer = (byte) intDelayTimer;
			}
			if (intSoundTimer > 0) {
				intSoundTimer -= 1;
				this.soundTimer = (byte) intSoundTimer;
			}
			this.timerDeficit = Duration.ofNanos(nanosSinceLastUpdate - NANOS_PER_TIMER_TICK);
			this.lastTimerUpdate = now;
		}
	}

	/**
	 * Returns whether enough time has passed to allow another opcode to run. Limiting the number of opcodes allowed
	 * to be executed every second fixes incredibly high speeds in games.
	 * @return Whether enough time has passed to allow another opcode to run.
	 */
	private boolean shouldExecuteOpcode() {
		Instant now = Instant.now();
		Duration timeSinceLastUpdate = Duration.between(this.lastInstrUpdate, now).plus(this.instrDeficit);
		long nanosSinceLastUpdate = timeSinceLastUpdate.toNanos();
		if (nanosSinceLastUpdate >= nanosPerInstr) {
			this.instrDeficit = Duration.ofNanos(nanosSinceLastUpdate - nanosPerInstr);
			this.lastInstrUpdate = now;
			return true;
		}
		return false;
	}

	public short fetch(Chip8 chip8) {
		// combine two 8-bit instructions to get a 16-bit opcode
		short opcode = (short) ((chip8.getFromMem(pc) << 8) & 0xffff | chip8.getFromMem(pc + 1) & 0xff);
		return opcode;
	}
	
	public void decode(Chip8 chip8, short opcode) {
		this.updateTimers();
		if (!shouldExecuteOpcode()) {
			return;
		}
		pc += 2;
		// printHexShort(opcode);
		// note: nthNybble variables are always unsigned because they only take up 4 bits (not 8)
		byte firstNybble = (byte) ((opcode & 0xF000) >>> 12);
		// printHexByte(firstNybble);
		byte secondNybble = (byte) ((opcode & 0x0F00) >>> 8);
		// printHexByte(secondNybble);
		byte thirdNybble = (byte) ((opcode & 0x00F0) >>> 4);
		// printHexByte(thirdNybble);
		byte fourthNybble = (byte) (opcode & 0x000F);
		// printHexByte(fourthNybble);
		
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
					// should call a subroutine on original cpu, we don't need to handle this - simply
					// ignore and continue
					assert true;
				} else if (fourthNybble == 0x0) {  // cls
					// request EDT to clear screen - may not happen instantly
					SwingUtilities.invokeLater(() -> chip8.display.clearScreen());
				} else if (fourthNybble == 0xE) {
					// return from subroutine
					chip8.cpu.pc = chip8.cpu.stack.pop();
				}
				break;
			case 0x1: // jump
				chip8.cpu.pc = (short) (((secondNybble << 8) & 0xf00) | ((thirdNybble << 4) & 0x0f0) |
										fourthNybble & 0x00f);
				break;
			case 0x2: // call subroutine
				chip8.cpu.stack.push(chip8.cpu.pc);
				chip8.cpu.pc = (short) (((secondNybble << 8) & 0xf00) | ((thirdNybble << 4) & 0x0f0) |
						fourthNybble & 0x00f);
				break;
			case 0x3: // skip next instruction if VX == NN
				if (V[secondNybble] == (byte) ((thirdNybble << 4) & 0xf0 | (fourthNybble & 0x0f))) {
					chip8.cpu.pc += 2;
				}
				break;
			case 0x4: // skip next instruction if VX != NN
				if (V[secondNybble] != (byte) ((thirdNybble << 4) & 0xf0 | (fourthNybble & 0x0f))) {
					chip8.cpu.pc += 2;
				}
				break;
			case 0x5: // skip next instruction if VX == VY
				if (V[secondNybble] == V[thirdNybble]) {
					chip8.cpu.pc += 2;
				}
				break;
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
						if (!chip8.usingModernImpl) {
							V[secondNybble] = V[thirdNybble];
						}
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
			case 0x9: // skip next instruction if VX != VY
				if (V[secondNybble] != V[thirdNybble]) {
					chip8.cpu.pc += 2;
				}
				break;
			case 0xA: // set-index-register
				chip8.cpu.indexReg = (short) (((secondNybble << 8) & 0xf00) |
						((thirdNybble << 4) & 0x0f0) | (fourthNybble & 0x00f));
				break;
			case 0xB: // jump with register offset
				int base_addr = (((secondNybble << 8) & 0xf00) | ((thirdNybble << 4) & 0x0f0) | fourthNybble & 0x00f);
				if (chip8.usingModernImpl) {
					chip8.cpu.pc = (short) ((int)V[secondNybble] + base_addr);
				} else {
					chip8.cpu.pc = (short) ((int)V[0] + base_addr);
				}
				break;
			case 0xC: // generate randnum, then set VX to randnum AND NN
				int rand_num = rand.nextInt(255);
				chip8.cpu.V[secondNybble] = (byte) (rand_num & ((thirdNybble << 4) & 0xf0 | (fourthNybble & 0x0f)));
				break;
			case 0xD: { // draw-sprite
				chip8.cpu.V[0xF] = 0;
				int x = (V[secondNybble] & 0xff) % Display.SCREEN_WIDTH; // x-coord's start position wraps
				int y = (V[thirdNybble] & 0xff) % Display.SCREEN_HEIGHT; // y-coord's start position wraps
				int spriteHeight = fourthNybble & 0xff;

				byte[] spriteList = new byte[spriteHeight]; // holds all sprite to be written in row order
				// fill spriteList with sprite data
				for (int i = 0; i < spriteHeight; ++i) {
					spriteList[i] = chip8.memory[indexReg + i];
				}
				// draw sprite to display
				boolean bitTurnedOff = chip8.display.drawSprite(spriteList, x, y, spriteHeight);
				V[0xF] = (byte) (bitTurnedOff ? 1 : 0);
				break;
			}
			case 0xE:
				if (thirdNybble == 0x9) { // skip next instruction if key corresponding to value in VX is pressed
					int hostKeyTarget = chip8.keyboard.translateToHostKey(V[secondNybble]);
					if (chip8.keyboard.keyState.get(hostKeyTarget) == Keyboard.KeyState.PRESSED) {
						pc += 2;
					}
					break;
				} else if (thirdNybble == 0xA) { // skip next instr if key corresponding to value in VX is NOT pressed
					int hostKeyTarget = chip8.keyboard.translateToHostKey(V[secondNybble]);
					if (chip8.keyboard.keyState.get(hostKeyTarget) == Keyboard.KeyState.NOT_PRESSED) {
						pc += 2;
					}
					break;
				}
			case 0xF:
				if (thirdNybble == 0x0) {
					if (fourthNybble == 0x7) { // set VX to the value of the delay timer
						V[secondNybble] = delayTimer;
						break;
					} else if (fourthNybble == 0xA) { // Wait until a key is pressed, then store the key into VX
						if (chip8.keyboard.currKey == null) {
							// No key is being pressed, so block execution
							pc -= 2;
						} else {
							byte guestKey = chip8.keyboard.translateToGuestKey(chip8.keyboard.currKey);
							V[secondNybble] = guestKey;
						}
						break;
					}
				} else if (thirdNybble == 0x1) {
					if (fourthNybble == 0x5) { // set delay timer to value in VX
						delayTimer = V[secondNybble];
						break;
					} else if (fourthNybble == 0x8) { // set sound timer to value in VX
						soundTimer = V[secondNybble];
						break;
					} else if (fourthNybble == 0xE) { // set I to I + VX
						// this opcode is weird, check for regressions
						V[0xf] = 0;
						int result = indexReg + (int) (V[secondNybble] & 0xff);
						if (result > 0xfff) {
							V[0xf] = 1;
						}
						indexReg = (short) result;
						break;
					}
				} else if (thirdNybble == 0x2) { // set I to the address of the hex character in VX
					// note: the hex character in VX is found by the lowest 4 bits
					byte hex_char = (byte) (V[secondNybble] & 0x0f);
					indexReg = (short) (0x050 + (5 * hex_char)); // since there are 5 bytes per character
					break;
				} else if (thirdNybble == 0x3) { // load BCD into memory
					int num = V[secondNybble] & 0xff;
					byte hundreds_digit = (byte) (num / 100);
					byte tens_digit = (byte) ((num / 10) % 10);
					byte ones_digit = (byte) (num % 10);
					chip8.setMem(indexReg, hundreds_digit);
					chip8.setMem(indexReg + 1, tens_digit);
					chip8.setMem(indexReg + 2, ones_digit);
					break;
				} else if (thirdNybble == 0x5) { // store register values to memory starting at I
					// NOTE: this opcode would permanently alter the index register on the original COSMAC VIP,
					// but not modern interpreters
					if (chip8.usingModernImpl) {
						for (int n = 0; n <= secondNybble; ++n) {
							chip8.setMem(indexReg + n, V[n]);
						}
					} else {
						for (int n = 0; n <= secondNybble; ++n) {
							chip8.setMem(indexReg, V[n]);
							++indexReg;
						}
					}
					break;
				} else if (thirdNybble == 0x6) { // load memory (starting at I) into registers
					// NOTE: this opcode would permanently alter the index register on the original COSMAC VIP,
					// but not modern interpreters
					if (chip8.usingModernImpl) {
						for (int n = 0; n <= secondNybble; ++n) {
							V[n] = chip8.getFromMem(indexReg + n);
						}
					} else {
						for (int n = 0; n <= secondNybble; ++n) {
							V[n] = chip8.getFromMem(indexReg);
							++indexReg;
						}
					}
					break;
				}
			default:
				throw new UnsupportedOperationException("Opcode " + Integer.toHexString((opcode & 0xffff)) + " not handled!");
		}
	}
	
} // Processor
