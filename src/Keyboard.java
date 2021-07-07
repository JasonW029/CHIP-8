import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Keyboard extends KeyAdapter {

    public enum KeyState {
        PRESSED,
        NOT_PRESSED
    }

    HashMap<Integer, KeyState> keyState = new HashMap<>();
    // Maps guest keys (emulated keys) to host keys (keyboard keys)
    private final HashMap<Byte, Integer> guestToHostKey = new HashMap<>();
    // Maps host keys (keyboard keys) to guest keys (emulated keys)
    private final HashMap<Integer, Byte> hostToGuestKey = new HashMap<>();
    // A key being pressed. If multiple keys are pressed, only the last is detected. Is null if no key is pressed.
    Integer currKey;

    private static final int[] ALLOWED_KEYS = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
            KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R,
            KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F,
            KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V
    };

    private static final byte[] EMULATED_KEYS = {
            // keys are listed in order, corresponding to the host keys in ALLOWED_KEYS
            1, 2, 3, 0xC,
            4, 5, 6, 0xD,
            7, 8, 9, 0xE,
            0xA, 0, 0xB, 0xF
    };

    public Keyboard() {
        for (int i = 0; i < ALLOWED_KEYS.length; ++i) {
            keyState.put(ALLOWED_KEYS[i], KeyState.NOT_PRESSED);
            guestToHostKey.put(EMULATED_KEYS[i], ALLOWED_KEYS[i]);
            hostToGuestKey.put(ALLOWED_KEYS[i], EMULATED_KEYS[i]);
        }
    }

    public Integer translateToHostKey(Byte guestKey) {
        return guestToHostKey.get(guestKey);
    }

    public Byte translateToGuestKey(Integer hostKey) {
        return hostToGuestKey.get(hostKey);
    }

    private boolean validKeyPressed(int keyPressed) {
        for (int allowed_key : ALLOWED_KEYS) {
            if (keyPressed == allowed_key) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!validKeyPressed(key)) {
            return;
        }
        keyState.put(key, KeyState.PRESSED);
        currKey = key;
        System.out.println(key + "being pressed!");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (!validKeyPressed(key)) {
            return;
        }
        keyState.put(key, KeyState.NOT_PRESSED);
        // Need to check for null here since currKey is an Integer (object) which can be null
        if (currKey != null && currKey == key) {
            currKey = null;
        }
    }
}
