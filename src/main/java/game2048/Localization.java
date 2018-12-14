package game2048;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javafx.scene.control.Labeled;

/**
 * Loads a resource bundle with the internationalization of the labels and texts
 * used within the app. If translation is not found, it uses the default
 * (English).
 * 
 * This class also supportes changing locale during runtime, as long Labeled
 * components are configured using the method i18n(String key, Class<T extends
 * Labeled>).
 * 
 * @author Bruno Borges
 */
public final class Localization {

    private static final Localization INSTANCE = new Localization();
    private static final String BUNDLE_NAME = "labels%s.properties";

    private Map<Labeled, String> labeledObjects = new HashMap<>();
    private Properties i18n;
    private Locale instanceLocale = Locale.getDefault();

    public static void changeLocale(Locale loc) {
        INSTANCE.instanceLocale = loc;
        INSTANCE.initializeLocale();
        INSTANCE.refreshLabeledObjects();
    }

    private void refreshLabeledObjects() {
        labeledObjects.entrySet().stream().forEach(e -> {
            var labeled = e.getKey();
            var i18nkey = e.getValue();

            labeled.setText(getLabel(i18nkey));
        });
    }

    public static String getLabel(String key) {
        return INSTANCE.i18n.getProperty(key, key);
    }

    public static <T extends Labeled> T i18n(String key, Class<T> type) {
        return INSTANCE.i18n_impl(key, type);
    }

    public static <T extends Labeled> T i18n(String key, T type) {
        return INSTANCE.i18n_impl(key, type);
    }

    private <T extends Labeled> T i18n_impl(String key, Class<T> type) {
        try {
            var labeled = type.getDeclaredConstructor().newInstance();
            return i18n_impl(key, labeled);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        // Not gonna happen
        return null;
    }

    private <T extends Labeled> T i18n_impl(String key, T labeled) {
        var label = getLabel(key);
        labeled.setText(label);
        labeledObjects.put(labeled, key);
        return labeled;
    }

    private Localization() {
        i18n = new Properties();
        initializeLocale();
    }

    private void initializeLocale() {
        i18n.clear();

        try (var bundle = openBundleWithDefaultFallback()) {
            i18n.load(bundle);
        } catch (IOException e) {
            throw new RuntimeException("No bundle found.");
        }
    }

    private InputStream openBundleWithDefaultFallback() throws IOException {
        var bundle = Game2048.class.getResource(figureOutBundleWithLocale());
        if (bundle == null) {
            bundle = Game2048.class.getResource(String.format(BUNDLE_NAME, ""));
        }

        return bundle.openStream();
    }

    private String figureOutBundleWithLocale() {
        return String.format(BUNDLE_NAME, instanceLocale.getLanguage());
    }

}