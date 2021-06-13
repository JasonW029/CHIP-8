import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Chip8Test {

    Chip8 chip8;

    @BeforeEach
    public void setup() throws Exception {
        chip8 = new Chip8(10, false);
    }

    @Test
    public void test1NNN() {
        // jump program counter to address 0xNNN
        short opcode = 0x1AF6;
        chip8.cpu.decode(chip8, opcode);
        assertEquals(0xAF6, chip8.cpu.pc);
    }

    @Test
    public void test6XNN() {
        // set register X to NN
        short opcode = 0x6AFF;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b11111111, chip8.cpu.V[0xA]);
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
    public void test8XY0() {
        // set register X to the value in register Y
        short opcode = (short) 0x8AB0;
        chip8.cpu.V[0xB] = (byte) 0b10001000;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b10001000, chip8.cpu.V[0xA]);
    }

    @Test
    public void test8XY1() {
        // bitwise-OR the value in register X to the value in register Y
        short opcode = (short) 0x8AB1;
        chip8.cpu.V[0xA] = (byte) 0b11101000;
        chip8.cpu.V[0xB] = (byte) 0b10000011;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b11101011, chip8.cpu.V[0xA]);
    }

    @Test
    public void test8XY2() {
        // bitwise-AND the value in register X to the value in register Y
        short opcode = (short) 0x8AB2;
        chip8.cpu.V[0xA] = (byte) 0b11101000;
        chip8.cpu.V[0xB] = (byte) 0b10000011;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b10000000, chip8.cpu.V[0xA]);
    }

    @Test
    public void test8XY3() {
        // bitwise-XOR the value in register X to the value in register Y
        short opcode = (short) 0x8AB3;
        chip8.cpu.V[0xA] = (byte) 0b11101000;
        chip8.cpu.V[0xB] = (byte) 0b10000011;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b01101011, chip8.cpu.V[0xA]);
    }

    @Test
    public void test8XY4_carry() {
        // set register X to its own value PLUS the value in register Y
        // if the value overflows 8 bits, set the carry flag in VF
        short opcode = (short) 0x8AB4;
        chip8.cpu.V[0xA] = (byte) 255;
        chip8.cpu.V[0xB] = (byte) 100;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 99, chip8.cpu.V[0xA]);
        assertEquals(1, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY4_no_carry() {
        // set register X to its own value PLUS the value in register Y
        // if the result overflows 8 bits, set the carry flag in VF
        short opcode = (short) 0x8AB4;
        chip8.cpu.V[0xA] = (byte) 50;
        chip8.cpu.V[0xB] = (byte) 3;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 53, chip8.cpu.V[0xA]);
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY5_underflow() {
        // set register X to its own value MINUS the value in register Y
        // if the result is positive, set the carry flag in VF
        short opcode = (short) 0x8AB5;
        chip8.cpu.V[0xA] = (byte) 0;
        chip8.cpu.V[0xB] = (byte) 200;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) -200, chip8.cpu.V[0xA]);
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY5_no_underflow() {
        // set register X to its own value MINUS the value in register Y
        // if the result is positive, set the carry flag in VF
        short opcode = (short) 0x8AB5;
        chip8.cpu.V[0xA] = (byte) 255;
        chip8.cpu.V[0xB] = (byte) 200;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 55, chip8.cpu.V[0xA]);
        assertEquals(1, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY6_shift_out_0() {
        // set register X to its own value BITSHIFTED RIGHT by 1
        // set VF to the bit that is shifted out
        short opcode = (short) 0x8A36;
        chip8.cpu.V[0xA] = (byte) 0b10000010;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b01000001, chip8.cpu.V[0xA]);
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY6_shift_out_1() {
        // set register X to its own value BITSHIFTED RIGHT by 1
        // set VF to the bit that is shifted out
        short opcode = (short) 0x8A36;
        chip8.cpu.V[0xA] = (byte) 0b10000011;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b01000001, chip8.cpu.V[0xA]);
        assertEquals(1, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY7_underflow() {
        // set register X to the value in register Y MINUS the value in register X
        // if the result is positive, set the carry flag in VF
        short opcode = (short) 0x8AB7;
        chip8.cpu.V[0xA] = (byte) 200;
        chip8.cpu.V[0xB] = (byte) 0;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) -200, chip8.cpu.V[0xA]);
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XY7_no_underflow() {
        // set register X to the value in register Y MINUS the value in register X
        // if the result is positive, set the carry flag in VF
        short opcode = (short) 0x8AB7;
        chip8.cpu.V[0xA] = (byte) 200;
        chip8.cpu.V[0xB] = (byte) 255;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 55, chip8.cpu.V[0xA]);
        assertEquals(1, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XYE_shift_out_0() {
        // set register X to its own value BITSHIFTED LEFT by 1
        // set VF to the bit that is shifted out
        short opcode = (short) 0x8A3E;
        chip8.cpu.V[0xA] = (byte) 0b01111111;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b11111110, chip8.cpu.V[0xA]);
        assertEquals(0, chip8.cpu.V[0xF]);
    }

    @Test
    public void test8XYE_shift_out_1() {
        // set register X to its own value BITSHIFTED LEFT by 1
        // set VF to the bit that is shifted out
        short opcode = (short) 0x8A3E;
        chip8.cpu.V[0xA] = (byte) 0b11111111;
        chip8.cpu.decode(chip8, opcode);
        assertEquals((byte) 0b11111110, chip8.cpu.V[0xA]);
        assertEquals(1, chip8.cpu.V[0xF]);
    }

    @Test
    public void testANNN() {
        // set the index register to NNN
        short opcode = (short) 0xA598;
        chip8.cpu.decode(chip8, opcode);
        assertEquals(0x0598, chip8.cpu.indexReg);
    }


}
