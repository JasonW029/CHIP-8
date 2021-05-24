import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Chip8Test {

    Chip8 chip8;

    @BeforeEach
    public void setup() throws Exception {
        chip8 = new Chip8(10);
    }

    @Test
    public void test7XNN() {
        // add NN to register X without setting register F if the sum overflows 8 bits
        short opcode = 0x7A03;
        chip8.cpu.V[0xA] = (byte) 255;
        chip8.cpu.decode(chip8, opcode);
        assertEquals(2, chip8.cpu.V[0xA]);
        // note: this is only true because we initialize a new CHIP-8 object each test
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void testANNN() {
        // set the index register to NNN
        short opcode = (short) 0xA598;
        chip8.cpu.decode(chip8, opcode);
        assertEquals(0x0598, chip8.cpu.indexReg);
    }


}
