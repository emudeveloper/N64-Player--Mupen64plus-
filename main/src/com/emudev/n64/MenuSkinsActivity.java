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
public class MenuSkinsActivity extends ListActivity
{
    public static MenuSkinsActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        optionList.add( new MenuOption( "Virtual Gamepad", "configure the virtual gamepad", "menuSkinsGamepad" ) );

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
        if( menuOption.info.equals( "menuSkinsGamepad" ) )
        {
            Intent intent = new Intent( mInstance, MenuSkinsGamepadActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
    }
}

