import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		Chip8 chip8 = new Chip8(10);
		System.out.println(Arrays.toString(chip8.RAM));
		for (int i = 0x200; i < chip8.RAM.length; i++) {
			System.out.print(Integer.toHexString(chip8.RAM[i] & 0xFF) + ", ");
		}
		System.out.println();
		chip8.cpu.V[0] = (byte) 0b10000000;
		chip8.cpu.V[1] = (byte) 0b00000111;
		System.out.println(chip8.cpu.V[0]);
		System.out.println(chip8.cpu.V[1]);
		short opcode = (short)0x8011;
//		short opcode = chip8.cpu.fetch(chip8);
		chip8.cpu.decode(chip8, opcode);
		System.out.println(chip8.cpu.V[0]);
		System.out.println(chip8.cpu.V[1]);
	}
	

};
