package org.pocketworkstation.pckeyboard;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.res.Resources;

/**
 * Global current settings for the keyboard.
 * 
 * <p>
 * Yes, globals are evil. But the persisted shared preferences are global data
 * by definition, and trying to hide this by propagating the current manually
 * just adds a lot of complication. This is especially annoying due to Views
 * getting constructed in a way that doesn't support adding additional
 * constructor arguments, requiring post-construction method calls, which is
 * error-prone and fragile.
 * 
 * <p>
 * The comments below indicate which class is responsible for updating the
 * value, and for recreating keyboards or views as necessary. Other classes
 * MUST treat the fields as read-only values, and MUST NOT attempt to save
 * these values or results derived from them across re-initializations.

 * 
 * @author klaus.weidner@gmail.com
 */
public final class GlobalKeyboardSettings {
    /* Simple prefs updated by this class */
    //
    // Read by Keyboard
    public boolean addShiftToPopup = false;
    //
    // Read by LatinKeyboardView
    public boolean showTouchPos = false;
    
    /* Updated by LatinIME */
    //
    // Read by KeyboardSwitcher
    public boolean wantFullInPortrait = false;
    public boolean isPortrait = false;
    //
    // Read by LatinKeyboardView
    public float rowHeightPercent = 10.0f; // percent of screen height
    //
    // Read by LatinKeyboardBaseView
    public int hintMode = 0;
    public int renderMode = 1;
    //
    // Read by PointerTracker
    public boolean sendSlideKeys = false;
    public int longpressTimeout = 400;
    //
    // Read by LatinIMESettings
    // These are cached values for informational display, don't use for other purposes
    public String editorPackageName; 
    public String editorFieldName; 
    public int editorFieldId; 
    public int editorInputType;

    /* Updated by KeyboardSwitcher */
    //
    // Used by LatinKeyboardBaseView and LatinIME
    public float labelScalePref = 1.0f;
    public float labelScale = 1.0f;

    /* Updated by LanguageSwitcher */
    //
    // Used by Keyboard and KeyboardSwitcher
    public Locale inputLocale = Locale.getDefault();

    // Auto pref implementation follows
    private Map<String, BooleanPref> mBoolPrefs = new HashMap<String, BooleanPref>();
    
    private interface BooleanPref {
        void set(boolean val);
        boolean getDefault();
        boolean needReload();
    }

    public void initPrefs(SharedPreferences prefs, Resources resources) {
        final Resources res = resources;

        addBooleanPref("pref_touch_pos", new BooleanPref() {
            public void set(boolean val) { showTouchPos = val; }
            public boolean getDefault() { return false; }
            public boolean needReload() { return false; }
        });

        addBooleanPref("pref_add_shift_to_popup", new BooleanPref() {
            public void set(boolean val) { addShiftToPopup = val; }
            public boolean getDefault() { return res.getBoolean(R.bool.default_add_shift_to_popup); }
            public boolean needReload() { return false; }
        });

        // Set initial values
        for (String key : mBoolPrefs.keySet()) {
            BooleanPref pref = mBoolPrefs.get(key);
            pref.set(prefs.getBoolean(key, pref.getDefault()));
        }
    }
    
    public boolean sharedPreferenceChanged(SharedPreferences prefs, String key) {
        boolean needReload = false;
        BooleanPref pref = mBoolPrefs.get(key);
        if (pref != null) {
            pref.set(prefs.getBoolean(key, pref.getDefault()));
            if (pref.needReload()) needReload = true; 
        }
        return needReload;
    }

    private void addBooleanPref(String key, BooleanPref setter) {
        mBoolPrefs.put(key, setter);
    }
}
