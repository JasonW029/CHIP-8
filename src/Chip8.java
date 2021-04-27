import javax.swing.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {
	
	byte[] RAM = new byte[0x1000];  // CHIP-8 has 4096 bytes of RAM
	Processor cpu = new Processor();
	Display display = new Display(10);
	
	byte[] fonts = new byte[]
    {
    	(byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0, // 0
    	(byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70, // 1
    	(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // 2
    	(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 3
    	(byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10, // 4
    	(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 5
    	(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 6
    	(byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40, // 7
    	(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 8
    	(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 9
    	(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90, // A
    	(byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0, // B
    	(byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0, // C
    	(byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0, // D
    	(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // E
    	(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80  // F
	};
	
	public Chip8(int scale) throws InterruptedException, InvocationTargetException {
		initialize(scale);
		setupFonts();
		try {
			loadProgram("Chip-8 Files/IBM Logo.ch8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initialize(int scale) throws InterruptedException, InvocationTargetException {
		cpu.pc = 0x200;  // programs start at RAM addr 0x200
		cpu.indexReg = 0;
		cpu.stack = new Stack<>();
		// initialize display on the EDT
		SwingUtilities.invokeAndWait( () -> display = new Display(scale) );
	}
	
	private void setupFonts() {
		for (short ramAddr = 0x050, fontIndex = 0; ramAddr <= 0x09F && fontIndex < fonts.length; ramAddr++, fontIndex++) {
			RAM[ramAddr] = fonts[fontIndex];
		}
	}
	
	public void loadProgram(String filepath) throws IOException {
		File program = new File(filepath);
		byte[] programBytes = Files.readAllBytes(program.toPath());
		for (int i = 0; i < programBytes.length; i++) {
			setRAM(i + 0x200, programBytes[i]);
		}
	}
	
	public byte getFromRAM(int addr) {
		return this.RAM[addr];
	}
	
	public void setRAM(int addr, byte newValue) {
		this.RAM[addr] = newValue;
	}
}
