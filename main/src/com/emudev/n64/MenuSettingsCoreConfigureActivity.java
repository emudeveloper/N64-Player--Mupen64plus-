package com.emudev.n64;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

// TODO: Comment thoroughly
public class MenuSettingsCoreConfigureActivity extends ListActivity implements IScancodeListener
{
    public static MenuSettingsCoreConfigureActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options
    private ScancodeDialog scancodeDialog = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        addOption( optionList, "Stop", "Kbd Mapping Stop" );
        addOption( optionList, "Save State", "Kbd Mapping Save State" );
        addOption( optionList, "Load State", "Kbd Mapping Load State" );
        addOption( optionList, "Increment Slot", "Kbd Mapping Increment Slot" );
        addOption( optionList, "Reset", "Kbd Mapping Reset" );
        addOption( optionList, "Speed Down", "Kbd Mapping Speed Down" );
        addOption( optionList, "Speed Up", "Kbd Mapping Speed Up" );
        addOption( optionList, "Pause", "Kbd Mapping Pause" );
        addOption( optionList, "Fast Forward", "Kbd Mapping Fast Forward" );
        addOption( optionList, "Frame Advance", "Kbd Mapping Frame Advance" );
        addOption( optionList, "Gameshark", "Kbd Mapping Gameshark" );
        optionList.add( new MenuOption( "Disable Volume Keys", "use as core functions", "menuSettingsCoreConfigureVolume",
                                        Globals.volumeKeysDisabled ) );
        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, optionList );
        setListAdapter( optionArrayAdapter );
    }

    public void addOption( List<MenuOption> optionList, String name, String info )
    {
        if( info == null )
            return;

        int scancode = 0;
        String val = MenuActivity.mupen64plus_cfg.get( "Core", info );
        if( val != null )
        {
            try
            {  // make sure a valid integer was entered
                scancode = Integer.valueOf( val ).intValue();
            }
            catch( NumberFormatException nfe )
            {}  // skip it if this happens
        }
        optionList.add( new MenuOption( name,
                                        ((scancode > 0) ? ("scancode " + scancode) : "(not mapped)"),
                                        info ) );
    }

    public void returnCode( int scancode, int codeType )
    {
        MenuActivity.mupen64plus_cfg.put( "Core", ScancodeDialog.menuItemInfo, "" + scancode );
        optionArrayAdapter.remove( optionArrayAdapter.getOption( ScancodeDialog.menuItemPosition ) );
        optionArrayAdapter.insert( new MenuOption( ScancodeDialog.menuItemName, "scancode " + scancode,
                                   ScancodeDialog.menuItemInfo ), ScancodeDialog.menuItemPosition );
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
        if( scancodeDialog == null )
            scancodeDialog = new ScancodeDialog( mInstance );

        if( menuOption.info.equals( "menuSettingsCoreConfigureVolume" ) ) 
        {
            Globals.volumeKeysDisabled = !Globals.volumeKeysDisabled;
            MenuActivity.gui_cfg.put( "KEYS", "disable_volume_keys", Globals.volumeKeysDisabled ? "1" : "0" );
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.add( new MenuOption( "Disable Volume Keys", "use as core functions", "menuSettingsCoreConfigureVolume",
                                                    Globals.volumeKeysDisabled ) );
        }
        else
        {
            ScancodeDialog.parent = this;
            ScancodeDialog.menuItemName = menuOption.name;
            ScancodeDialog.menuItemInfo = menuOption.info;
            ScancodeDialog.menuItemPosition = position;
            scancodeDialog.show();
        }
    }
}
