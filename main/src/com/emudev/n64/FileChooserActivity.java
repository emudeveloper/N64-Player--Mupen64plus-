package com.emudev.n64;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The FileChooserActivity class is used to navigate the file system
 * and choose a file.  Each menu option consists of two lines: the
 * file or folder name and a comment stating either that it is a
 * folder or the file's size in bytes.  Each menu option also stores
 * the full path to that file or folder.
 *
 * Author: Paul Lamb
 * 
 * http://www.google.com
 * 
 */
public class FileChooserActivity extends ListActivity
{
    private File currentFolder;  // folder being browsed at the moment
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options

    public static String startPath = null;
    public static String extensions = null;
    public static IFileChooser parentMenu = null;;

    /*
     * Populates the menu with the current directory
     * @param savedInstanceState Used by Android.
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        if( startPath == null )
            startPath = Globals.StorageDir;
        currentFolder = new File( startPath ); // get the start folder
        populate( currentFolder );
    }

    /*
     * Populates the menu with the specified folder
     * @param folder Folder containing the files to list.
     */
    private void populate( File folder )
    {
        if( folder == null || !folder.isDirectory() )
        {
            folder = new File( Globals.StorageDir );
            if( !folder.isDirectory() )
            {
                Log.e( "FileChooserActivity", "SD Card not accessable in method populate" );
                Runnable toastMessager = new Runnable()
                {
                    public void run()
                    {
                        Toast toast = Toast.makeText( MenuActivity.mInstance, "App data not accessible (cable plugged in \"USB Mass Storage Device\" mode?)", Toast.LENGTH_LONG );
                        toast.setGravity( Gravity.BOTTOM, 0, 0 );
                        toast.show();
                    }
                };
                this.runOnUiThread( toastMessager );
                return;
            }
        }
        File[] fileList = folder.listFiles();
        setTitle( "Current Folder: "+ folder.getName() );
        List<MenuOption>folders = new ArrayList<MenuOption>();
        List<MenuOption>files = new ArrayList<MenuOption>();
        String filename, ext;
        try
        { // separate the folders and files
            for( File file: fileList )
            { // check if it is a folder or a file
                if( file.isDirectory() )
                    folders.add( new MenuOption( file.getName(), "Folder", file.getAbsolutePath() ) );
                else
                {
                    filename = file.getName();
                    if( filename != null && filename.length() > 2 )
                    { // check what type of file it is
                        ext = filename.substring( filename.length() - 3, filename.length() ).toLowerCase();
                        if( extensions.contains( ext) )
                            files.add( new MenuOption( filename, "File Size: " + file.length(), file.getAbsolutePath() ) );
                    }
                }
            }
        }
        catch( Exception e )
        {}
        // sort the folders and files separately before recombining them
        Collections.sort( folders );
        Collections.sort( files );
        folders.addAll( files );
        if( folder.getName() != null && !folder.getName().equals( "" ) ) // make sure we aren't at root folder
            folders.add( 0, new MenuOption( "..", "Parent folder", folder.getParent() ) );
        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, folders );
        setListAdapter( optionArrayAdapter );
    }

    /*
     * Determines what to do, based on if the user chose a folder or a file 
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
        if( menuOption.comment.equals( "Folder" ) || menuOption.comment.equals( "Parent folder" ) )
        {  // repopulate the menu with the folder that was chosen
            currentFolder = new File( menuOption.info );
            if( (!extensions.equals( ".so" )) && (currentFolder != null) )
                MenuActivity.gui_cfg.put( "LAST_SESSION", "rom_folder", currentFolder.getAbsolutePath() );
            populate( currentFolder );
        }
        else
        { // User picked a file
            onFileClick( menuOption );
        }
    }
    /*
     * Performs the desired action using the file chosen by the user.
     * @param menuOption Which file the user chose.
     */
    private void onFileClick( MenuOption menuOption )
    {
        String ext;
        String filename = menuOption.info;
        ext = filename.substring( filename.length() - 3, filename.length() ).toLowerCase();
        if( parentMenu != null )
            parentMenu.fileChosen( menuOption.info );
        else
        {
            Globals.chosenROM = menuOption.info;
            File f = new File( Globals.StorageDir );
            if( !f.exists() )
            {
                Log.e( "FileChooserActivity", "SD Card not accessable in method onFileClick" );
                Runnable toastMessager = new Runnable()
                {
                    public void run()
                    {
                        Toast toast = Toast.makeText( MenuActivity.mInstance, "App data not accessible (cable plugged in \"USB Mass Storage Device\" mode?)", Toast.LENGTH_LONG );
                        toast.setGravity( Gravity.BOTTOM, 0, 0 );
                        toast.show();
                    }
                };
                this.runOnUiThread( toastMessager );
                return;
            }
            MenuActivity.mupen64plus_cfg.save();
            MenuActivity.InputAutoCfg_ini.save();
            MenuActivity.gui_cfg.put( "LAST_SESSION", "rom", Globals.chosenROM );
            MenuActivity.gui_cfg.save();
            SDLActivity.resumeLastSession = false;
            Intent intent = new Intent( this, SDLActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
            if( MenuActivity.mInstance != null )
            {
                MenuActivity.mInstance.finish();
                MenuActivity.mInstance = null;
            }
        }
        finish();
    }
}
