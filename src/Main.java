import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		Chip8 chip8 = new Chip8();
		// System.out.println(Arrays.toString(chip8.RAM));
		for (int i = 0x200; i < chip8.RAM.length; i++) {
			System.out.print(Integer.toHexString(chip8.RAM[i] & 0xFF) + ", ");
		}
		System.out.println();
		short opcode = chip8.cpu.fetch(chip8);
		chip8.cpu.decode(chip8, opcode);
	}

};
