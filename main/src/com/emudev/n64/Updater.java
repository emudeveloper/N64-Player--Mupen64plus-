package com.emudev.n64;

import android.app.Activity;
import java.io.File;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

class Updater
{
    public static boolean checkv1_7( Activity mInstance )
    {
        String upgraded = MenuActivity.gui_cfg.get( "GENERAL", "upgraded_1.7" );

        if( upgraded == null || !upgraded.equals( "1" ) )
        {
            MenuActivity.gui_cfg.put( "GENERAL", "upgraded_1.7", "1" );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureCRC", "0" );
            Config gles2n64_conf = new Config( Globals.DataDir + "/data/gles2n64.conf" );
            gles2n64_conf.put( "[<sectionless!>]", "force screen clear", "0" );
            File f = new File( Globals.StorageDir );
            if( !f.exists() )
            {
               Log.e( "Updater", "SD Card not accessable in method checkv1_7" );
               return false;
            }
            MenuActivity.mupen64plus_cfg.save();
            MenuActivity.gui_cfg.save();
            gles2n64_conf.save();
        }
        return true;
    }
    public static boolean checkv1_5( Activity mInstance )
    {
        String upgraded = MenuActivity.gui_cfg.get( "GENERAL", "upgraded_1.5" );

        if( upgraded == null || !upgraded.equals( "1" ) )
        {
            MenuActivity.gui_cfg.put( "GENERAL", "upgraded_1.5", "1" );
            MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Pause", "0" );
            MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Mute", "0" );
            MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Fast Forward", "0" );
            MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Gameshark", "0" );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "SkipFrame", "1" );
            MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureLoading", "1" );

            Config gles2n64_conf = new Config( Globals.DataDir + "/data/gles2n64.conf" );
            gles2n64_conf.put( "[<sectionless!>]", "tribuffer opt", "1" );
            gles2n64_conf.put( "[<sectionless!>]", "force screen clear", "0" );
            gles2n64_conf.put( "[<sectionless!>]", "hack z", "0" );
            File f = new File( Globals.StorageDir );
            if( !f.exists() )
            {
               Log.e( "Updater", "SD Card not accessable in method checkv1_5" );
               return false;
            }
            MenuActivity.mupen64plus_cfg.save();
            MenuActivity.InputAutoCfg_ini.save();
            MenuActivity.gui_cfg.save();
            gles2n64_conf.save();
        }
        return true;
    }
    public static boolean checkv1_4( Activity mInstance )
    {
        String upgraded = MenuActivity.gui_cfg.get( "GENERAL", "upgraded_1.4" );

        if( upgraded == null || !upgraded.equals( "1" ) )
        {
            File f = new File( Globals.DataDir + "/plug-ins" );
            f.mkdirs();
            if( f.exists() )
            {
                MenuActivity.gui_cfg.put( "GENERAL", "upgraded_1.4", "1" );
                MenuActivity.gui_cfg.save();
                Config cfg = new Config( Globals.DataDir + "/plug-ins/audio_list.ini" );
                cfg.put(  Globals.LibsDir + "/lib/libaudio-sdl.so",
                                    "name", "audio-sdl, v1.4" );
                cfg.put(  Globals.LibsDir + "/lib/libaudio-sdl.so",
                                    "info", "Android port of Mupen64Plus 1.99.4" );
                cfg.put(  Globals.LibsDir + "/lib/libaudio-sdl.so",
                                    "author", "Hactarux, JttL, Ebenblues, Richard Goedeken" );
                cfg.save();
                cfg = new Config( Globals.DataDir + "/plug-ins/core_list.ini" );
                cfg.put(  Globals.LibsDir + "/lib/libcore.so",
                                    "name", "core, v1.4" );
                cfg.put(  Globals.LibsDir + "/lib/libcore.so",
                                    "info", "Android port of Mupen64Plus 1.99.4" );
                cfg.put(  Globals.LibsDir + "/lib/libcore.so",
                                    "author", "Hactarux, JttL, Ebenblues, Richard Goedeken, Paul Lamb" );
                cfg.save();
                cfg = new Config( Globals.DataDir + "/plug-ins/input_list.ini" );
                cfg.put(  Globals.LibsDir + "/lib/libinput-sdl.so",
                                    "name", "input-sdl, v1.4" );
                cfg.put(  Globals.LibsDir + "/lib/libinput-sdl.so",
                                    "info", "Android port of Mupen64Plus 1.99.4" );
                cfg.put(  Globals.LibsDir + "/lib/libinput-sdl.so",
                                    "author", "Hactarux, JttL, Ebenblues, Richard Goedeken, Paul Lamb" );
                cfg.save();
                cfg = new Config( Globals.DataDir + "/plug-ins/rsp_list.ini" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle.so",
                                    "name", "rsp-hle, v1.4" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle.so",
                                    "info", "Android port of Mupen64Plus 1.99.4" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle.so",
                                    "author", "Hactarux, JttL, Ebenblues, Richard Goedeken" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle-nosound.so",
                                    "name", "rsp-hle-nosound, v1.4" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle-nosound.so",
                                    "info", "No audio processing (speed hack)" );
                cfg.put(  Globals.LibsDir + "/lib/librsp-hle-nosound.so",
                                    "author", "Hactarux, JttL, Ebenblues, Richard Goedeken, Paul Lamb" );
                cfg.save();
                cfg = new Config( Globals.DataDir + "/plug-ins/video_list.ini" );
                cfg.put( Globals.LibsDir + "/lib/libgles2n64.so",
                                    "name", "gles2n64, v1.4" );
                cfg.put( Globals.LibsDir + "/lib/libgles2n64.so",
                                    "info", "Android GLES2 port of GLN64" );
                cfg.put( Globals.LibsDir + "/lib/libgles2n64.so",
                                    "author", "Orkin, Adventus, Yongzh, Paul Lamb" );
                cfg.put( Globals.LibsDir + "/lib/libgles2rice.so",
                                    "name", "gles2rice, v0.0.9" );
                cfg.put( Globals.LibsDir + "/lib/libgles2rice.so",
                                    "info", "GLES2 port of Rice Video" );
                cfg.put( Globals.LibsDir + "/lib/libgles2rice.so",
                                    "author", "Rice, Kris (Metricity)" );
                cfg.save();
            }
            return restoreDefaults( mInstance );
        }
        return true;
    }

    public static boolean checkFirstRun( Activity mInstance )
    {
        String first_run = MenuActivity.gui_cfg.get( "GENERAL", "first_run" );
        int width, height;

        if( first_run != null && first_run.equals( "1" ) )
        { // this gets run the first time the app runs only!
            MenuActivity.gui_cfg.put( "GENERAL", "first_run", "0" );
            DisplayMetrics metrics = new DisplayMetrics();
            mInstance.getWindowManager().getDefaultDisplay().getMetrics( metrics );
            if( metrics.widthPixels > metrics.heightPixels )
            {
                width = metrics.widthPixels;
                height = metrics.heightPixels;
            }
            else
            {
                width = metrics.heightPixels;
                height = metrics.widthPixels;
            }
            // pick a virtual gamepad layout based on screen resolution:
            if( width > 960 )
                MenuActivity.gui_cfg.put( "GAME_PAD", "which_pad", "Mupen64Plus-AE-Analog-Tablet" );
            else if( width <= 320 )
                MenuActivity.gui_cfg.put( "GAME_PAD", "which_pad", "Mupen64Plus-AE-Analog-Tiny" );
            else if( width < 800 )
                MenuActivity.gui_cfg.put( "GAME_PAD", "which_pad", "Mupen64Plus-AE-Analog-Small" );
            else
                MenuActivity.gui_cfg.put( "GAME_PAD", "which_pad", "Mupen64Plus-AE-Analog" );

            String romFolder;
            if( (new File( Globals.StorageDir + "/roms/n64" )).isDirectory() )
                romFolder = Globals.StorageDir + "/roms/n64";
            else
                romFolder = Globals.StorageDir;
            MenuActivity.gui_cfg.put( "LAST_SESSION", "rom_folder", romFolder );
            MenuActivity.gui_cfg.put( "GENERAL", "auto_save", "1" );
            File f = new File( Globals.StorageDir );
            if( !f.exists() )
            {
               Log.e( "Updater", "SD Card not accessable in method restoreDefaults" );
               return false;
            }
            return true;
        }
        return true;
    }

    public static boolean restoreDefaults( Activity mInstance )
    {
        MenuActivity.mupen64plus_cfg.put( "Core", "OnScreenDisplay", "True" );
        MenuActivity.mupen64plus_cfg.put( "Core", "R4300Emulator", "2" );
        MenuActivity.mupen64plus_cfg.put( "Core", "NoCompiledJump", "False" );
        MenuActivity.mupen64plus_cfg.put( "Core", "DisableExtraMem", "False" );
        MenuActivity.mupen64plus_cfg.put( "Core", "AutoStateSlotIncrement", "False" );
        MenuActivity.mupen64plus_cfg.put( "Core", "EnableDebugger", "False" );
        MenuActivity.mupen64plus_cfg.put( "Core", "CurrentStateSlot", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "ScreenshotPath", "\"\"" );
        MenuActivity.mupen64plus_cfg.put( "Core", "SaveStatePath", "\"\"" );
        MenuActivity.mupen64plus_cfg.put( "Core", "SharedDataPath", "\"\"" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Stop", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Fullscreen", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Save State", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Load State", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Increment Slot", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Reset", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Speed Down", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Speed Up", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Screenshot", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Pause", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Mute", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Increase Volume", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Decrease Volume", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Fast Forward", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Frame Advance", "0" );
        MenuActivity.mupen64plus_cfg.put( "Core", "Kbd Mapping Gameshark", "0" );

        MenuActivity.mupen64plus_cfg.put( "UI-Console", "PluginDir", Globals.LibsDir + "/lib/" );
        MenuActivity.mupen64plus_cfg.put( "UI-Console", "VideoPlugin", Globals.LibsDir + "/lib/libgles2n64.so" );
        MenuActivity.mupen64plus_cfg.put( "UI-Console", "AudioPlugin", Globals.LibsDir + "/lib/libaudio-sdl.so" );
        MenuActivity.mupen64plus_cfg.put( "UI-Console", "InputPlugin", Globals.LibsDir + "/lib/libinput-sdl.so" );
        MenuActivity.mupen64plus_cfg.put( "UI-Console", "RspPlugin", Globals.LibsDir + "/lib/librsp-hle.so" );

        MenuActivity.mupen64plus_cfg.put( "Video-Rice", "SkipFrame", "1" );
        MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureLoading", "1" );
        MenuActivity.mupen64plus_cfg.put( "Video-Rice", "FastTextureCRC", "0" );

        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "plugged", "True" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "plugin", "2" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "mouse", "False" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "DPad R", "key(100)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "DPad L", "key(97)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "DPad D", "key(115)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "DPad U", "key(119)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "Start", "key(13)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "Z Trig", "key(122)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "B Button", "key(306)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "A Button", "key(304)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "C Button R", "key(108)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "C Button L", "key(106)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "C Button D", "key(107)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "C Button U", "key(105)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "R Trig", "key(99)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "L Trig", "key(120)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "Mempak switch", "key(44)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "Rumblepak switch", "key(46)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "X Axis", "key(276,275)" );
        MenuActivity.InputAutoCfg_ini.put( "Keyboard", "Y Axis", "key(273,274)" );

        MenuActivity.gui_cfg.clear();
        MenuActivity.gui_cfg.put( "GENERAL", "first_run", "1" );
        MenuActivity.gui_cfg.put( "GENERAL", "upgraded_1.4", "1" );
        MenuActivity.gui_cfg.put( "GAME_PAD", "redraw_all", "0" );
        MenuActivity.gui_cfg.put( "GAME_PAD", "analog_octagon", "1" );
        MenuActivity.gui_cfg.put( "GAME_PAD", "show_fps", "0" );
        MenuActivity.gui_cfg.put( "GAME_PAD", "enabled", "1" );
        MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "enabled", "1" );
        MenuActivity.gui_cfg.put( "VIDEO_PLUGIN", "rgba8888", "0" );
        MenuActivity.gui_cfg.put( "KEYS", "disable_volume_keys", "0" );

        Config gles2n64_conf = new Config( Globals.DataDir + "/data/gles2n64.conf" );
        gles2n64_conf.put( "[<sectionless!>]", "enable fog", "0" );
        gles2n64_conf.put( "[<sectionless!>]", "enable alpha test", "1" );
        gles2n64_conf.put( "[<sectionless!>]", "tribuffer opt", "1" );
        gles2n64_conf.put( "[<sectionless!>]", "force screen clear", "0" );
        gles2n64_conf.put( "[<sectionless!>]", "hack z", "0" );

        File f = new File( Globals.StorageDir );
        if( !f.exists() )
        {
           Log.e( "Updater", "SD Card not accessable in method restoreDefaults" );
           return false;
        }
        MenuActivity.mupen64plus_cfg.save();
        MenuActivity.InputAutoCfg_ini.save();
        MenuActivity.gui_cfg.save();
        gles2n64_conf.save();
        return checkFirstRun( mInstance );
    }
}
