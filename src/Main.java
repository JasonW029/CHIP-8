import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main {

	@SuppressWarnings("InfiniteLoopStatement")
	public static void main(String[] args) throws Exception {
		Chip8 chip8;
		try {
			chip8 = new Chip8(10, true);
		} catch (InterruptedException | InvocationTargetException e) {
			e.printStackTrace();
			throw new Exception("Couldn't initialize CHIP-8.");
		}

		try {
			chip8.loadProgram("CHIP-8 Files/TETRIS");
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("The specified file cannot be found.");
		}

		System.out.println(Arrays.toString(chip8.memory));
		for (int i = 0x200; i < chip8.memory.length; i++) {
			System.out.print(Integer.toHexString(chip8.memory[i] & 0xFF) + ", ");
		}
//		System.out.println();
//		chip8.cpu.V[0] = (byte) 0b10000000;
//		chip8.cpu.V[1] = (byte) 0b00000111;
//		System.out.println(chip8.cpu.V[0]);
//		System.out.println(chip8.cpu.V[1]);
//		short opcode = (short)0x8011;
////		short opcode = chip8.cpu.fetch(chip8);
//		chip8.cpu.decode(chip8, opcode);
//		System.out.println(chip8.cpu.V[0]);
//		System.out.println(chip8.cpu.V[1]);

		while (true) {
			short opcode = chip8.cpu.fetch(chip8);
			chip8.cpu.decode(chip8, opcode);
		}

//		for (int i = 0; i < 8; ++i) {
//			short opcode = chip8.cpu.fetch(chip8);
//			chip8.cpu.decode(chip8, opcode);
//			System.out.println("V0: " + chip8.cpu.getHexString(chip8.cpu.V[0x0]));
//			System.out.println("V1: " + chip8.cpu.getHexString(chip8.cpu.V[0x1]));
//			System.out.println("Index Reg: " + chip8.cpu.getHexString(chip8.cpu.indexReg));
//		}
	}
	

}
