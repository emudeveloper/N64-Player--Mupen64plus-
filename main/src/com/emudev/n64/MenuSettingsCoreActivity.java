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
public class MenuSettingsCoreActivity extends ListActivity implements IFileChooser
{
    public static MenuSettingsCoreActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        optionList.add( new MenuOption( "Change Core", "choose another core", "menuSettingsCoreChange" ) );

        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, optionList );
        setListAdapter( optionArrayAdapter );
    }

    public void fileChosen( String filename )
    {
        //TODO: implement
/*
        currentPlugin = "(none)";

        if( filename != null )
        {
            MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "last_choice", "\"" + filename + "\"" );
            MenuActivity.mupen64plus_cfg.put( "UI-Console", "VideoPlugin", "\"" + filename + "\"" );
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
        optionArrayAdapter.insert( new MenuOption( "Change", currentPlugin, "menuSettingsVideoChange" ), 0 );
*/
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
        if( menuOption.info.equals( "menuSettingsCoreChange" ) )
        {  // open the menu to choose a core
            Intent intent = new Intent( mInstance, MenuSettingsCoreChangeActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
    }
}
