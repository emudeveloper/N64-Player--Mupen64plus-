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
public class MenuSettingsInputActivity extends ListActivity implements IFileChooser
{
    public static MenuSettingsInputActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options
    public static String currentPlugin = "(none)";
    public static boolean enabled = true;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        currentPlugin = "(none)";
        String filename = MenuActivity.mupen64plus_cfg.get( "UI-Console", "InputPlugin" );
        if( filename == null || filename.length() < 1 || filename.equals( "\"\"" ) || filename.equals( "\"dummy\"" ) )
            filename = MenuActivity.gui_cfg.get( "INPUT_PLUGIN", "last_choice" );
        if( filename != null )
        {
            MenuActivity.gui_cfg.put( "INPUT_PLUGIN", "last_choice", filename );
            filename = filename.replace( "\"", "" );
            int x = filename.lastIndexOf( "/" );
            if( x > -1 && x < (filename.length() - 1) )
            {
                currentPlugin = filename.substring( x + 1, filename.length() );
                if( currentPlugin == null || currentPlugin.length() < 1 )
                    currentPlugin = "(none)";
            }
        }
        String en = MenuActivity.gui_cfg.get( "INPUT_PLUGIN", "enabled" );
        if( en != null )
            enabled = en.equals( "1" ) ? true : false;

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        optionList.add( new MenuOption( "Change Plug-in", currentPlugin, "menuSettingsInputChange" ) );
        optionList.add( new MenuOption( "Map Buttons", "map controller buttons", "menuSettingsInputConfigure" ) );
//        optionList.add( new MenuOption( "Advanced Button Actions", "turbo or toggle behavior", "menuSettingsInputAdvanced" ) );
        optionList.add( new MenuOption( "Enable", "use this plug-in", "menuSettingsInputEnabled", enabled ) );

        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, optionList );
        setListAdapter( optionArrayAdapter );
    }

    public void fileChosen( String filename )
    {
        currentPlugin = "(none)";

        if( filename != null )
        {
            MenuActivity.gui_cfg.put( "INPUT_PLUGIN", "last_choice", "\"" + filename + "\"" );
            MenuActivity.mupen64plus_cfg.put( "UI-Console", "InputPlugin", "\"" + filename + "\"" );
            int x = filename.lastIndexOf( "/" );
            if( x > -1 && x < ( filename.length() - 1 ) )
            {
                currentPlugin = filename.substring( x + 1, filename.length() );
                if( currentPlugin == null || currentPlugin.length() < 1 )
                    currentPlugin = "(none)";
            }
            else
                currentPlugin = filename;
        }

        optionArrayAdapter.remove( optionArrayAdapter.getItem( 0 ) );
        optionArrayAdapter.insert( new MenuOption( "Change", currentPlugin, "menuSettingsInputChange" ), 0 );
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
        if( menuOption.info.equals( "menuSettingsInputChange" ) )
        {  // open the menu to choose a plug-in
            Intent intent = new Intent( mInstance, MenuSettingsInputChangeActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsInputConfigure" ) ) 
        {
            Intent intent = new Intent( mInstance, MenuSettingsInputConfigureActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else if( menuOption.info.equals( "menuSettingsInputAdvanced" ) ) 
        {
            //TODO: implement
        }
        else if( menuOption.info.equals( "menuSettingsInputEnabled" ) ) 
        {
            enabled = !enabled;
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.add( new MenuOption( "Enable", "use this plug-in", "menuSettingsInputEnabled",
                                                    enabled ) );
            MenuActivity.gui_cfg.put( "INPUT_PLUGIN", "enabled", (enabled ? "1" : "0") );
            MenuActivity.mupen64plus_cfg.put( "UI-Console", "InputPlugin",
                (enabled ? MenuActivity.gui_cfg.get( "INPUT_PLUGIN", "last_choice" ) : "\"dummy\"") );
        }
    }
}
