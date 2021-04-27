import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

// TRUE = ON (WHITE)
// FALSE = OFF (BLACK)

public class Display extends JPanel {

    int scale;
    boolean[][] screen;

    public static void main(String[] args) {
        Display display = new Display(10);
        boolean[][] screenCtx = new boolean[64][32];
        for (boolean[] row : screenCtx) {
            Arrays.fill(row, true);
        }
        display.screen = screenCtx;
        // display implicitly calls paintComponent
        display.updateScreen();
    }


    public Display(int scale) {
        this.scale = scale;

        boolean[][] screen = new boolean[64][32];
        for (boolean[] row : screen) {
            Arrays.fill(row, false);
        }
        this.screen = screen;

        this.setPreferredSize(new Dimension(64 * this.scale, 32 * this.scale));
//        this.setSize(64 * this.scale, 32 * this.scale);

        JFrame frame = new JFrame("Test");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(64 * this.scale, 32 * this.scale);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public void flipPixel(int x, int y) {
        this.screen[x][y] = !this.screen[x][y];
        updateScreen();
    }

    public void clearScreen() {
        boolean[][] screen = new boolean[64][32];
        for (boolean[] row : screen) {
            Arrays.fill(row, false);
        }
        this.screen = screen;
        updateScreen();
    }

    public void updateScreen() {
        this.removeAll();

        this.revalidate();
        this.repaint();
        // drawScreen();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
//        g.drawRect(0, 0, 200, 200);
        for (int i = 0; i < this.screen.length; i++) {
            for (int j = 0; j < this.screen[i].length; j++) {
                boolean pixelOn = this.screen[i][j];
                if (!pixelOn) {
                    // g.drawRect(i * this.scale, j * this.scale, this.scale, this.scale);
                    g.fillRect(i * this.scale, j * this.scale, this.scale, this.scale);
                }
            }
        }
    } // paintComponent

} // Display
