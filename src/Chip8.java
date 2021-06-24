import javax.swing.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {
	
	byte[] memory = new byte[0x1000];  // CHIP-8 has 4096 bytes of memory
	Processor cpu;
	Display display;
	Keyboard keyboard = new Keyboard();
	boolean usingModernImpl;
	final static byte[] FONTS = new byte[] {
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
	final static short GAME_LOAD_ADDR = 0x200; // programs start at memory addr 0x200
	
	public Chip8(int scale, boolean usingModernImpl) throws InterruptedException, InvocationTargetException {
		// setup CPU
		this.cpu = new Processor(GAME_LOAD_ADDR, 600);
		// setup DISPLAY
		SwingUtilities.invokeAndWait( () -> this.display = new Display(scale, this.keyboard) );
		// setup OTHER OPTIONS
		this.usingModernImpl = usingModernImpl;
		// setup FONTS into memory
		setupFonts();
	}
	
	private void setupFonts() {
		for (short memAddr = 0x050, fontIndex = 0; memAddr <= 0x09F && fontIndex < FONTS.length; memAddr++, fontIndex++) {
			setMem(memAddr, FONTS[fontIndex]);
		}
	}
	
	public void loadProgram(String filepath) throws IOException {
		File program = new File(filepath);
		byte[] programBytes = Files.readAllBytes(program.toPath());
		for (int i = 0; i < programBytes.length; i++) {
			setMem(i + 0x200, programBytes[i]);
		}
	}
	
	public byte getFromMem(int addr) {
		return this.memory[addr];
	}
	
	public void setMem(int addr, byte newValue) {
		this.memory[addr] = newValue;
	}
}
