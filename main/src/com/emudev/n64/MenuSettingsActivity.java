package com.emudev.n64;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// TODO: Comment thoroughly
public class MenuSettingsActivity extends ListActivity
{
    public static MenuSettingsActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        optionList.add( new MenuOption( "Video", "configure the graphics", "menuSettingsVideo" ) );
        optionList.add( new MenuOption( "Audio", "choose audio settings", "menuSettingsAudio" ) );
        optionList.add( new MenuOption( "Input", "map controller buttons", "menuSettingsInput" ) );
        optionList.add( new MenuOption( "Virtual Gamepad", "configure the virtual gamepad", "menuSkinsGamepad" ) );
        optionList.add( new MenuOption( "RSP", "rapid system prototyping", "menuSettingsRSP" ) );
        optionList.add( new MenuOption( "Core", "emulator core settings", "menuSettingsCore" ) );
        optionList.add( new MenuOption( "Reset Default Settings", "reset all to default values", "menuSettingsResetDefaults" ) );
        optionList.add( new MenuOption( "Restore App Data", "saves will be lost!", "menuSettingsRestoreAppData" ) );
        optionList.add( new MenuOption( "Enable Auto-Save", "automatically save game on exit",
                                        "menuSettingsAutoSave", Globals.auto_save ) );

        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, optionList );
        setListAdapter( optionArrayAdapter );
    }
    /*
     * Determines what to do, based on what option the user chose 
     * @param listView Used by Android.
     * @param view Used by Android.
     * @param position Which item the user chose.
     * @param id Used by Android.
     */
    @Override
    protected void onListItemClick( ListView listView, View view, int position, long id )
    {
        super.onListItemClick( listView, view, position, id );
        MenuOption menuOption = optionArrayAdapter.getOption( position );
        if( menuOption.info.equals( "menuSettingsVideo" ) )
        {
            Intent intent = new Intent( mInstance, MenuSettingsVideoActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsAudio" ) ) 
        {
            Intent intent = new Intent( mInstance, MenuSettingsAudioActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsInput" ) ) 
        {
            Intent intent = new Intent( mInstance, MenuSettingsInputActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSkinsGamepad" ) )
        {
            Intent intent = new Intent( mInstance, MenuSkinsGamepadActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsRSP" ) ) 
        {
            Intent intent = new Intent( mInstance, MenuSettingsRSPActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsCore" ) ) 
        {
            Intent intent = new Intent( mInstance, MenuSettingsCoreActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsResetDefaults" ) ) 
        {
            if( !Updater.restoreDefaults( this ) )
            {
                Runnable toastMessager = new Runnable()
                {
                    public void run()
                    {
                        Toast toast = Toast.makeText( MenuActivity.mInstance, "Problem resetting defaults (see logcat)", Toast.LENGTH_LONG );
                        toast.setGravity( Gravity.BOTTOM, 0, 0 );
                        toast.show();
                    }
                };
                this.runOnUiThread( toastMessager );
                return;
            }
            mInstance.finish();
        }
        else if( menuOption.info.equals( "menuSettingsRestoreAppData" ) ) 
        {
            File appData = new File( Globals.DataDir );
            SDLActivity.deleteFolder( appData );
            Intent intent = new Intent( mInstance, MainActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
            mInstance.finish();
            MenuActivity.mInstance.finish();
        }
        else if( menuOption.info.equals( "menuSettingsAutoSave" ) ) 
        {
            Globals.auto_save = !Globals.auto_save;
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.add( new MenuOption( "Enable Auto-Save", "automatically save game on exit",
                                                    "menuSettingsAutoSave", Globals.auto_save ) );
            MenuActivity.gui_cfg.put( "GENERAL", "auto_save", ( Globals.auto_save ? "1" : "0") );
        }
    }
}

