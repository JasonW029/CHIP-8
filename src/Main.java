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
			chip8.loadProgram("CHIP-8 Files/INVADERS");
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("The specified file cannot be found.");
		}

		System.out.println(Arrays.toString(chip8.memory));
		for (int i = Chip8.GAME_LOAD_ADDR; i < chip8.memory.length; i++) {
			System.out.print(Integer.toHexString(chip8.memory[i] & 0xFF) + ", ");
		}

		// Start emulation loop
		while (true) {
			short opcode = chip8.cpu.fetch(chip8);
			chip8.cpu.decode(chip8, opcode);
		}

	} // main()
} // Main
