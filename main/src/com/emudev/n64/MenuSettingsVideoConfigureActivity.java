package com.emudev.n64;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

// TODO: Comment thoroughly
public class MenuSettingsVideoConfigureActivity extends ListActivity
{
    public static MenuSettingsVideoConfigureActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // array of menu options
    private Config gles2n64_conf;
    private boolean enableFog = false;
    private boolean enableTribuffer = false;
    private boolean forceScreenClear = true;
    private boolean alphaTest = true;

    private boolean isRice = false;
    private boolean riceSkipFrame = false;
    private boolean riceFastTextureCRC = true;
    private boolean riceFastTexture = false;
    private boolean hackZ = false;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        // TODO: standardize this, using the gui config.  This is too much of a hack.
        isRice = false;
        String filename = MenuActivity.mupen64plus_cfg.get( "UI-Console", "VideoPlugin" );
        if( filename != null )
        {
            filename = filename.replace( "\"", "" );
            int x = filename.lastIndexOf( "/" );
            if( x > -1 && x < (filename.length() - 1) )
            {
                String currentPlugin = filename.substring( x + 1, filename.length() );
                if( currentPlugin != null && currentPlugin.equals( "libgles2rice.so" ) )
                    isRice = true;
            }
        }
        //

        gles2n64_conf = new Config( Globals.DataDir + "/data/gles2n64.conf" );
        String val = gles2n64_conf.get( "[<sectionless!>]", "enable fog" );

        if( val != null )
            enableFog = ( val.equals( "1" ) ? true : false );

        val = gles2n64_conf.get( "[<sectionless!>]", "force screen clear" );
        if( val != null )
            forceScreenClear = ( val.equals( "1" ) ? true : false );

        val = gles2n64_conf.get( "[<sectionless!>]", "enable alpha test" );
        if( val != null )
            alphaTest = ( val.equals( "1" ) ? true : false );

        val = gles2n64_conf.get( "[<sectionless!>]", "tribuffer opt" );
        if( val != null )
            enableTribuffer = ( val.equals( "1" ) ? true : false );
        val = gles2n64_conf.get( "[<sectionless!>]", "hack z" );
        if( val != null )
            hackZ = ( val.equals( "1" ) ? true : false );

        val = MenuActivity.mupen64plus_cfg.get( "Video-Rice", "SkipFrame" );
        if( val != null )
            riceSkipFrame = ( val.equals( "1" ) ? true : false );
        val = MenuActivity.mupen64plus_cfg.get( "Video-Rice", "FastTextureCRC" );
        if( val != null )
            riceFastTextureCRC = ( val.equals( "1" ) ? true : false );
        val = MenuActivity.mupen64plus_cfg.get( "Video-Rice", "FastTextureLoading" );
        if( val != null )
            riceFastTexture = ( val.equals( "1" ) ? true : false );

        List<MenuOption>optionList = new ArrayList<MenuOption>();
      if( isRice )
      {
        optionList.add( new MenuOption( "Enable Skip Frame", "(improves speed at cost of FPS)",
                                        "menuSettingsVideoConfigureSkipFrame", riceSkipFrame ) );
        optionList.add( new MenuOption( "Enable Fast Texture CRC", "(disabling improves 2D elements)",
                                        "menuSettingsVideoConfigureFastTextureCRC", riceFastTextureCRC ) );
        optionList.add( new MenuOption( "Enable Fast Texture Loading", "(fixes circle transitions)",
                                        "menuSettingsVideoConfigureFastTexture", riceFastTexture ) );
      }
      else
      {
        optionList.add( new MenuOption( "Stretch Screen", "(may skew aspect ratio)",
                                        "menuSettingsVideoConfigureStretch", Globals.screen_stretch ) );
        optionList.add( new MenuOption( "Auto Frameskip", "(auto-adjusts based on speed)",
                                        "menuSettingsVideoConfigureAutoFrameskip", Globals.auto_frameskip ) );
        optionList.add( new MenuOption( "Max Frameskip", "=" + Globals.max_frameskip + " (disable auto frameskip to set manually)",
                                        "menuSettingsVideoConfigureMaxFrameskip" ) );
        optionList.add( new MenuOption( "Enable Fog", "(needs work)",
                                        "menuSettingsVideoConfigureFog", enableFog ) );
        optionList.add( new MenuOption( "Enable Tribuffer Opt", "(disable if Zelda crashes on startup)",
                                        "menuSettingsVideoConfigureTribuffer", enableTribuffer ) );
        optionList.add( new MenuOption( "Force Screen Clear", "(clears some junk graphics)",
                                        "menuSettingsVideoConfigureScreenClear", forceScreenClear ) );
        optionList.add( new MenuOption( "Enable Alpha Test", "(disable for speed hack, black textures)",
                                        "menuSettingsVideoConfigureAlpha", alphaTest ) );
        optionList.add( new MenuOption( "Hack Z", "(enable to fix flashing background)",
                                        "menuSettingsVideoConfigureHackZ", hackZ ) );
      }
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
        if( menuOption.info.equals( "menuSettingsVideoConfigureStretch" ) ) 
        {
            Globals.screen_stretch = !Globals.screen_stretch;
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Stretch Screen", "(may skew aspect ratio)",
                                           "menuSettingsVideoConfigureStretch", Globals.screen_stretch ), position );
            MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "screen_stretch", Globals.screen_stretch ? "1" : "0" );
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureAutoFrameskip" ) ) 
        {
            Globals.auto_frameskip = !Globals.auto_frameskip;
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Auto Frameskip", "(gles2n64 only)",
                                           "menuSettingsVideoConfigureAutoFrameskip", Globals.auto_frameskip ), position );
            MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "auto_frameskip", Globals.auto_frameskip ? "1" : "0" );
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureMaxFrameskip" ) ) 
        {
            Globals.max_frameskip++;
            if( Globals.max_frameskip > 5 )
                Globals.max_frameskip = 0;
            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Max Frameskip", "=" + 
                                                    Globals.max_frameskip + " (disable auto frameskip to set manually)",
                                                    "menuSettingsVideoConfigureMaxFrameskip" ), position );
            MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "max_frameskip", "" + Globals.max_frameskip );
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureFog" ) ) 
        {
            enableFog = !enableFog;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Enable Fog", "(needs work)",
                                        "menuSettingsVideoConfigureFog", enableFog ), position );
            gles2n64_conf.put( "[<sectionless!>]", "enable fog", (enableFog ? "1" : "0") );
            gles2n64_conf.save();
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureTribuffer" ) ) 
        {
            enableTribuffer = !enableTribuffer;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Enable Tribuffer Opt",
                                                       "(disable if Zelda crashes on startup)",
                                                       "menuSettingsVideoConfigureTribuffer",
                                                       enableTribuffer ), position );
            gles2n64_conf.put( "[<sectionless!>]", "tribuffer opt", (enableTribuffer ? "1" : "0") );
            gles2n64_conf.save();
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureScreenClear" ) ) 
        {
            forceScreenClear = !forceScreenClear;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Force Screen Clear", "(clears some junk graphics)",
                                        "menuSettingsVideoConfigureScreenClear", forceScreenClear ), position );
            gles2n64_conf.put( "[<sectionless!>]", "force screen clear", (forceScreenClear ? "1" : "0") );
            gles2n64_conf.save();
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureAlpha" ) ) 
        {
            alphaTest = !alphaTest;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Enable Alpha Test", "(disable for speed hack, black textures)",
                                        "menuSettingsVideoConfigureAlpha", alphaTest ), position );
            gles2n64_conf.put( "[<sectionless!>]", "enable alpha test", (alphaTest ? "1" : "0") );
            gles2n64_conf.save();
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureHackZ" ) ) 
        {
            hackZ = !hackZ;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.add( new MenuOption( "Hack Z", "(enable to fix flashing background)",
                                        "menuSettingsVideoConfigureHackZ", hackZ ) );
            gles2n64_conf.put( "[<sectionless!>]", "hack z", (hackZ ? "1" : "0") );
            gles2n64_conf.save();
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureSkipFrame" ) ) 
        {
            riceSkipFrame = !riceSkipFrame;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Enable Skip Frame", "(improves speed at cost of FPS)",
                                                  "menuSettingsVideoConfigureSkipFrame", riceSkipFrame ), position );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "SkipFrame", (riceSkipFrame ? "1" : "0") );
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureFastTextureCRC" ) ) 
        {
            riceFastTextureCRC = !riceFastTextureCRC;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.insert( new MenuOption( "Enable Fast Texture CRC", "(disabling improves 2D elements)",
                                        "menuSettingsVideoConfigureFastTextureCRC", riceFastTextureCRC ), position );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureCRC", (riceFastTextureCRC ? "1" : "0") );
        }
        else if( menuOption.info.equals( "menuSettingsVideoConfigureFastTexture" ) ) 
        {
            riceFastTexture = !riceFastTexture;

            optionArrayAdapter.remove( menuOption );
            optionArrayAdapter.add( new MenuOption( "Enable Fast Texture Loading", "(fixes circle transitions)",
                                        "menuSettingsVideoConfigureFastTexture", riceFastTexture ) );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureLoading", (riceFastTexture ? "1" : "0") );
        }
    }
}
