package org.programus.android.clipblacklist;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Settings
 * @author programus
 *
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.addPreferencesFromResource(R.xml.settings);
    }

}
