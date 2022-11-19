package com.filemonitor.prueba;

import android.content.SharedPreferences;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        File root = new File("/mnt/sdcard");

        if(isReadyToLoad()){
            cargarArchivosTest(root); //cargamos archivos desde la raiz
            Toast.makeText(getApplicationContext(),"Los archivos test han sido creados", Toast.LENGTH_LONG).show();
        }else{
            borrarArchivosTest(root); //cargamos archivos desde la raiz
            Toast.makeText(getApplicationContext(),"Los archivos test han sido eliminados", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean isReadyToLoad(){ //comprueba que se ha activado los test files para su carga
        SharedPreferences p = getApplicationContext().getSharedPreferences("MyPref",0);
        return p.getBoolean("test_files",false);
    }

    private void cargarArchivosTest(File f){ //la primera sera la raiz, funcion recursiva

        File[] files = f.listFiles();

        try {

            File filepath = new File(f, "test.txt");
            FileWriter wr = new FileWriter(filepath);
            wr.flush();
            wr.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && hasPermissions(file)) {
                    cargarArchivosTest(file);
                }
            }
        }


    }

    private void borrarArchivosTest(File f){
        File [] files = f.listFiles();

        try {
            File filepath = new File(f,"test.txt");
            if(filepath.exists()){
                filepath.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (files != null){
            for (File file : files){
                //excluir las carpetas obb y data
                if(file.isDirectory() && hasPermissions(file)){
                    borrarArchivosTest(file);
                }
            }
        }
    }

    private Boolean hasPermissions(File file){
        return  file.getAbsolutePath() != "/mnt/sdcard/Android/obb" && !file.getAbsolutePath().equals("/mnt/sdcard/Android/data");
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SharedPreferences prefs = this.getActivity().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = prefs.edit();



            Preference path = findPreference("path");
            Preference notification = findPreference("notifications");
            Preference test_file = findPreference("test_files");



            path.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

                    if (isPathValid((String) newValue)){
                        editor.putString("path", (String) newValue);
                        editor.apply();
                    }
                    return true;
                }
            });

            notification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    editor.putBoolean("notifications", (Boolean) newValue);
                    editor.apply();
                    return true;
                }
            });

            test_file.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    editor.putBoolean("test_files", (Boolean) newValue);
                    editor.apply();
                    return true;
                }
            });
        }


        //Check if path is valid for saving file
        public static boolean isPathValid(String path) {
            try {
                Paths.get(path);
            } catch (InvalidPathException ex) {
                return false;
            }
            return true;
        }


    }



}