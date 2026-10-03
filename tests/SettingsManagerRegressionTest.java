import com.crfmanagement.settings.SettingsManager;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsManagerRegressionTest {
    private static final Path FILE = Path.of("settings.properties");

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        String scenario = args[0];
        if (scenario.startsWith("bad-file-")) {
            String[] bad = {"broken", "300,0,0", "", "1,2,3,4", "#xyzxyz"};
            int index = Integer.parseInt(scenario.substring(9));
            Files.writeString(FILE, "background.color=" + bad[index] + "\ncustom=value\n");
            SettingsManager manager = SettingsManager.getInstance();
            check(Color.WHITE.equals(manager.getBackgroundColor()), "invalid file color must use white");
            check("value".equals(manager.getSetting("custom", "")), "keep unrelated settings");
            check("fallback".equals(manager.getSetting("background.color", "fallback")), "discard invalid color in memory");
        } else {
            SettingsManager manager = SettingsManager.getInstance();
            switch (scenario) {
                case "defaults":
                    check(Color.WHITE.equals(manager.getBackgroundColor()), "default white");
                    check(!Files.exists(FILE), "reading defaults must not create a file");
                    break;
                case "hex":
                    manager.setSetting("background.color", "#123456");
                    check(new Color(0x123456).equals(manager.getBackgroundColor()), "hex round trip");
                    break;
                case "rgb":
                    manager.setSetting("background.color", " 12, 34, 56 ");
                    check(new Color(12, 34, 56).equals(manager.getBackgroundColor()), "RGB round trip");
                    break;
                case "invalid-write":
                    manager.setBackgroundColor(Color.BLUE);
                    String saved = Files.readString(FILE);
                    boolean rejected = false;
                    try { manager.setSetting("background.color", "broken"); }
                    catch (IllegalArgumentException expected) { rejected = true; }
                    check(rejected, "invalid color must be rejected");
                    check(Color.BLUE.equals(manager.getBackgroundColor()), "retain previous color");
                    check("#0000ff".equals(manager.getSetting("background.color", "")), "retain previous property");
                    check(saved.equals(Files.readString(FILE)), "invalid input must not rewrite settings");
                    break;
                case "null-color":
                    manager.setBackgroundColor(Color.BLUE);
                    try { manager.setBackgroundColor(null); } catch (NullPointerException expected) { }
                    check(Color.BLUE.equals(manager.getBackgroundColor()), "null must not mutate color");
                    break;
                case "reload-invalid":
                    manager.setBackgroundColor(Color.BLUE);
                    Files.writeString(FILE, "background.color=broken\n");
                    manager.loadSettings();
                    check(Color.WHITE.equals(manager.getBackgroundColor()), "invalid reload uses default");
                    break;
                case "event":
                    AtomicInteger changes = new AtomicInteger();
                    manager.addPropertyChangeListener("backgroundColor", event -> {
                        check(Color.RED.equals(event.getNewValue()), "typed color event");
                        changes.incrementAndGet();
                    });
                    manager.setSetting("background.color", "#ff0000");
                    check(changes.get() == 1, "one UI background event");
                    break;
                case "plain-setting":
                    manager.setSetting("enable.notifications", "false");
                    manager.loadSettings();
                    check("false".equals(manager.getSetting("enable.notifications", "true")), "persist ordinary setting");
                    break;
                default: throw new IllegalArgumentException(scenario);
            }
        }
        System.out.println("PASS " + scenario);
    }
}
