import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public class Display extends JPanel {

    int scale;
    boolean[][] screen;

    public static void main(String[] args) {
        //TODO: remove
        boolean[][] screenCtx = new boolean[64][32];
        for (boolean[] row : screenCtx) {
            Arrays.fill(row, true);
        }

        new Display(10, screenCtx);
    }

    public Display(int scale, boolean[][] screen) {
        this.scale = scale;
        this.screen = screen;

        JFrame frame = new JFrame("Test");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(64 * this.scale, 32 * this.scale);
        frame.setVisible(true);
//        frame.setResizable(false);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
//        g.drawRect(0, 0, 200, 200);
        for (int i = 0; i < this.screen.length; i++) {
            for (int j = 0; j < this.screen[i].length; j++) {
                if (this.screen[i][j]) {
                    g.drawRect(i * this.scale, j * this.scale, this.scale, this.scale);
                }
            }
        }
    } // paintComponent

} // Display
