package com.emudev.n64;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.lang.Integer;
import java.lang.NumberFormatException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream; 

import android.content.res.Configuration;

import android.app.*;
import android.content.*;
import android.view.*;
import android.os.*;
import android.util.Log;
import android.graphics.*;
import android.text.method.*;
import android.text.*;
import android.util.AttributeSet;
import android.media.*;
import android.hardware.*;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.lang.*;


/**
    SDL Activity
*/
public class SDLActivity extends Activity
{
    // Menu items
    private static final int MAIN_MENU_ITEM = Menu.FIRST;
    private static final int SLOT_MENU_ITEM = MAIN_MENU_ITEM + 1;
    private static final int SAVE_MENU_ITEM = SLOT_MENU_ITEM + 1;
    private static final int LOAD_MENU_ITEM = SAVE_MENU_ITEM + 1;
    private static final int CLOSE_MENU_ITEM = LOAD_MENU_ITEM + 1;

    public static final int EMULATOR_STATE_UNKNOWN = 0;
    public static final int EMULATOR_STATE_STOPPED = 1;
    public static final int EMULATOR_STATE_RUNNING = 2;
    public static final int EMULATOR_STATE_PAUSED = 3;

    private static NotificationManager notificationManager = null;

    // Main components
    public static SDLActivity mSingleton = null;
    public static SDLSurface mSurface = null;
    public static boolean _isPaused = false;
    public static boolean resumeLastSession = false;
    public static boolean finishedReading = false;
    public static int saveSlot = 0;
    public static boolean rgba8888 = false;

    // Virtual gamepad
    public static GamePad mGamePad = null;
    public static GamePad.GamePadListing mGamePadListing = null;
    public static int whichPad = 0;
    private static boolean[] previousKeyStates = new boolean[GamePad.MAX_BUTTONS];

    // Audio
    private static Thread mAudioThread = null;
    private static AudioTrack mAudioTrack = null;

    boolean sdlInited = false;

    // Toast Messages:
    private static Toast toast = null;
    private static Runnable toastMessager = null;

    private static int frameCount = -1;
    private static int fpsRate = 15;
    private static long lastFPSCheck = 0;

    private static String tmpFile = null;
    public boolean noInputPlugin = false;

    // Load the .so's
    static
    {
	try
	{
        	System.loadLibrary("SDL");
	}
	catch( UnsatisfiedLinkError e )
	{
		Log.e( "SDLActivity", "Unable to load native library 'SDL'" );
	}
	try
	{
        	System.loadLibrary("core");
	}
	catch( UnsatisfiedLinkError e )
	{
		Log.e( "SDLActivity", "Unable to load native library 'core'" );
	}
	try
	{
        	System.loadLibrary("front-end");
	}
	catch( UnsatisfiedLinkError e )
	{
		Log.e( "SDLActivity", "Unable to load native library 'front-end'" );
	}

	// Other .so's we could load if needed:
        //System.loadLibrary("SDL_image");
        //System.loadLibrary("SDL_mixer");
        //System.loadLibrary("SDL_ttf");
    }

    // Setup
    protected void onCreate( Bundle savedInstanceState )
    {
        // paulscode, place an icon into the status bar:
        if( notificationManager == null )
            notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        int statusIcon = R.drawable.status;
        CharSequence text = "Mupen64Plus AE is running";
        CharSequence contentTitle = "Mupen64Plus AE";
        CharSequence contentText = "Mupen64Plus AE";
        long when = System.currentTimeMillis();
        Context context = getApplicationContext();

        Intent intent = new Intent( this, MenuActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        PendingIntent contentIntent = PendingIntent.getActivity( this, 0, intent, 0 );
        Notification notification = new Notification( statusIcon, text, when );
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo( context, contentTitle, contentText, contentIntent );
        notificationManager.notify( Globals.NOTIFICATION_ID, notification );

        // paulscode, load the native libraries:
        loadNativeSO( MenuActivity.mupen64plus_cfg.get( "UI-Console", "VideoPlugin" ) );
        loadNativeSO( MenuActivity.mupen64plus_cfg.get( "UI-Console", "AudioPlugin" ) );
        loadNativeSO( MenuActivity.mupen64plus_cfg.get( "UI-Console", "InputPlugin" ) );
        loadNativeSO( MenuActivity.mupen64plus_cfg.get( "UI-Console", "RspPlugin" ) );

        /// paulscode, fix potential crash when input plug-in is disabled
        String inp = MenuActivity.mupen64plus_cfg.get( "UI-Console", "InputPlugin" );
        if( inp != null )
        {
            inp = inp.replace( "\"", "" );
            if( inp.equalsIgnoreCase( "dummy" ) )
                noInputPlugin = true;
        }
        ///

        // paulscode, gather's information about the device, and chooses a hardware profile (used to customize settings)
        readCpuInfo();
        int x;
        // paulscode, clears the virtual gamepad key states
        for( x = 0; x < 30; x++ )
        {
            previousKeyStates[x] = false;
        }

        super.onCreate(savedInstanceState);

        // fullscreen mode
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
        if( Globals.InhibitSuspend )
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                                  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        sdlInited = true;

        // So we can call stuff from static callbacks
        mSingleton = this;

        setContentView( R.layout.main );
        mSurface = (SDLSurface) findViewById( R.id.my_surface );
        SurfaceHolder holder = mSurface.getHolder();
        holder.setType( SurfaceHolder.SURFACE_TYPE_GPU );

        mGamePad = (GamePad) findViewById( R.id.my_gamepad );
        mGamePad.setResources( getResources() );
        mGamePadListing = new GamePad.GamePadListing( Globals.DataDir + "/skins/gamepads/gamepad_list.ini" );

        // make sure the gamepad preferences are loaded;
        String val = MenuActivity.gui_cfg.get( "GAME_PAD", "analog_octagon" );
        if( val != null )
            MenuSkinsGamepadActivity.analogAsOctagon = ( val.equals( "1" ) ? true : false );
        val = MenuActivity.gui_cfg.get( "GAME_PAD", "show_fps" );
        if( val != null )
            MenuSkinsGamepadActivity.showFPS = ( val.equals( "1" ) ? true : false );
        val = MenuActivity.gui_cfg.get( "GAME_PAD", "enabled" );
        if( val != null )
            MenuSkinsGamepadActivity.enabled = ( val.equals( "1" ) ? true : false );
        MenuSkinsGamepadActivity.chosenGamepad = MenuActivity.gui_cfg.get( "GAME_PAD", "which_pad" );
        val = MenuActivity.gui_cfg.get( "VIDEO_PLUGIN", "rgba8888" );
        if( val != null )
            rgba8888 = ( val.equals( "1" ) ? true : false );
         
        // look up any special codes for the analog controls
        if( Globals.analog_100_64 )
        {
            val = MenuActivity.InputAutoCfg_ini.get( "Keyboard", "X Axis" );
            if( val == null )
            {
                if( new File( Globals.StorageDir ).exists() )
                {
                    MenuActivity.InputAutoCfg_ini = new Config( Globals.DataDir + "/data/InputAutoCfg.ini" );
                    val = MenuActivity.InputAutoCfg_ini.get( "Keyboard", "X Axis" );
                }
                else
                {
                    Log.e( "SDLActivity", "No access to storage, probably in USB Mass Storage mode" );
                    showToast( "App data not accessible (cable plugged in \"USB Mass Storage Device\" mode?)" );
                }
            }
            if( val != null )
            {
                x = val.indexOf( "(" );
                int y = val.indexOf( ")" );
                if( x >= 0 && y >= 0 && y > x )
                {
                    val = val.substring( x + 1, y ).trim();
                    x = val.indexOf( "," );
                    if( x >= 0 )
                    {
                        Globals.ctrl1[0] = toInt( val.substring( x + 1, val.length() ), 0 );
                        Globals.ctrl1[1] = toInt( val.substring( 0, x ), 0 );
                    }
                }
                val = MenuActivity.InputAutoCfg_ini.get( "Keyboard", "Y Axis" );
                x = val.indexOf( "(" );
                y = val.indexOf( ")" );
                if( x >= 0 && y >= 0 && y > x )
                {
                    val = val.substring( x + 1, y ).trim();
                    x = val.indexOf( "," );
                    if( x >= 0 )
                    {
                        Globals.ctrl1[2] = toInt( val.substring( x + 1, val.length() ), 0 );
                        Globals.ctrl1[3] = toInt( val.substring( 0, x ), 0 );
                    }
                }
            }
        }

        if( !MenuSkinsGamepadActivity.enabled )
            mGamePad.loadPad( null );
        else if( MenuSkinsGamepadActivity.chosenGamepad != null && MenuSkinsGamepadActivity.chosenGamepad.length() > 0 )
            mGamePad.loadPad( MenuSkinsGamepadActivity.chosenGamepad );
        else if( mGamePadListing.numPads > 0 )
            mGamePad.loadPad( mGamePadListing.padNames[0] );
        else
        {
            mGamePad.loadPad( null );
            Log.v( "SDLActivity", "No gamepad skins found" );
        }
            
        showToast( "Mupen64Plus Started" );
    }

    private static int toInt( String val, int fail )
    {
        if( val == null || val.length() < 1 )
            return fail;  // not a number
        try
        {
            return Integer.valueOf( val ).intValue();  // convert to integer
        }
        catch( NumberFormatException nfe )
        {}

        return fail;  // conversion failed
    }

    private void loadNativeSO( String filepath )
    {
        String filename = null;
        if( filepath != null && filepath.length() > 0 )
        {
            filename = filepath.replace( "\"", "" );
            if( filename.equalsIgnoreCase( "dummy" ) )
                return;
            int x = filename.lastIndexOf( "/" );
            if( x > -1 && x < (filename.length() - 1) )
            {
                filename = filename.substring( x + 1, filename.length() );
            }
            filename = filename.replace( ".so", "" );
            filename = filename.replace( "lib", "" );
        }
        if( filename != null && filename.length() > 0 )
        {
	    try
            {
                System.loadLibrary( filename );
            }
	    catch( UnsatisfiedLinkError e )
	    {
                Log.e( "SDLActivity", "Unable to load native library '" + filename + "'" );
	    }
        }
    }

    // paulscode, add the menu options:
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        saveSlot = 0;
        menu.add( 0, MAIN_MENU_ITEM, 0, "Menu" );
        menu.add( 0, SLOT_MENU_ITEM, 0, "Slot (0)" );
        menu.add( 0, SAVE_MENU_ITEM, 0, "Save" );
        menu.add( 0, LOAD_MENU_ITEM, 0, "Load" );
        menu.add( 0, CLOSE_MENU_ITEM, 0, "Close" );
        return super.onCreateOptionsMenu( menu );
    }

    // paulscode, add the menu options:
    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        menu.clear();
        menu.add( 0, MAIN_MENU_ITEM, 0, "Menu" );
        menu.add( 0, SLOT_MENU_ITEM, 0, "Slot++ (" + saveSlot + ")" );
        menu.add( 0, SAVE_MENU_ITEM, 0, "Save" );
        menu.add( 0, LOAD_MENU_ITEM, 0, "Load" );
        menu.add( 0, CLOSE_MENU_ITEM, 0, "Close" );
        return super.onCreateOptionsMenu( menu );
    }

    // paulscode, add the menu options:
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case MAIN_MENU_ITEM:
                saveSession();  // Workaround, allows us to force-close later
//
                notificationManager.cancel( Globals.NOTIFICATION_ID );
                Intent intent = new Intent( mSingleton, MenuActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                startActivity( intent );

//                this.finish();  // This causes menu to crash, why??
                System.exit( 0 );  // Workaround, force-close (what about SDL thread?)
                break;
            case SLOT_MENU_ITEM:
                saveSlot++;
                if( saveSlot > 9 )
                    saveSlot = 0;
                stateSetSlotEmulator( saveSlot );
                showToast( "Save-Game Slot " + saveSlot );
                break;
            case SAVE_MENU_ITEM:
                stateSaveEmulator();
                break;
            case LOAD_MENU_ITEM:
                stateLoadEmulator();
                break;
            case CLOSE_MENU_ITEM:
//                notificationManager.cancel( Globals.NOTIFICATION_ID );
//                this.finish();  // This doesn't save; closes to quickly maybe?
                saveSession();  // Workaround, wait for fileSaveEmulator to finish first
                notificationManager.cancel( Globals.NOTIFICATION_ID );
//                this.finish();  // Gles2rice doesn't stop, why??
                System.exit( 0 );  // Workaround, force-close (what about SDL thread?)
//
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    public void saveSession()
    {
        if( tmpFile != null )
        {
            try
            {
                new File( tmpFile ).delete();
            }
            catch( Exception e )
            {}
        }
        if( !Globals.auto_save )
            return;
        showToast( "Saving game" );
        fileSaveEmulator( "Mupen64PlusAE_LastSession.sav" );
        try{Thread.sleep( 500 );}catch(InterruptedException e){}  // wait a bit
        int c = 0;
        int state = SDLActivity.stateEmulator();
        while( state == SDLActivity.EMULATOR_STATE_PAUSED && c < 120 )
        {  // it should be paused while saving the session.
            try{Thread.sleep( 500 );}catch(InterruptedException e){}
            state = SDLActivity.stateEmulator();
            c++;
        }
        mSurface.buffFlipped = false;
        c = 0;
        while( !mSurface.buffFlipped )
        { // wait for the game to have resumed, as indicated
          // by a call to flip the EGL buffers.
            try{Thread.sleep( 20 );}catch(InterruptedException e){}
            c++;
        }
        try{Thread.sleep( 40 );}catch(InterruptedException e){}  // just to be sure..
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )  // this executes when the device configuration changes
    { 
        super.onConfigurationChanged( newConfig );
    }

    @Override
    public void onUserLeaveHint()  // this executes when Home is pressed (can't detect it in onKey).
    { 
//
//        fileSaveEmulator( "Mupen64PlusAE_LastSession.sav" );  // immediate resume causes problems!
        saveSession();  // Workaround, allows us to force-close later
//
        super.onUserLeaveHint();  // weird bug if chosing "Close" from menu.  Force-close here?
        
        System.exit( 0 );  // Workaround, force-close (what about SDL thread?)

        /* How to go home using an intent:
                Intent intent = new Intent( Intent.ACTION_MAIN );
                intent.addCategory( Intent.CATEGORY_HOME );
                startActivity( intent );
        */
    }

    @Override
    protected void onPause()
    {
        _isPaused = true;
        super.onPause();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        if( mSurface != null || mGamePad != null )
        {
            setContentView( R.layout.main );

            mSurface = (SDLSurface) findViewById( R.id.my_surface );
            SurfaceHolder holder = mSurface.getHolder();
            holder.setType( SurfaceHolder.SURFACE_TYPE_GPU );

            mGamePad = (GamePad) findViewById( R.id.my_gamepad );
            mGamePad.setResources( getResources() );
            mGamePadListing = new GamePad.GamePadListing( Globals.DataDir + "/skins/gamepads/gamepad_list.ini" );
            if( !MenuSkinsGamepadActivity.enabled )
                mGamePad.loadPad( null );
            else if( MenuSkinsGamepadActivity.chosenGamepad != null && MenuSkinsGamepadActivity.chosenGamepad.length() > 0 )
                mGamePad.loadPad( MenuSkinsGamepadActivity.chosenGamepad );
            else if( mGamePadListing.numPads > 0 )
                mGamePad.loadPad( mGamePadListing.padNames[0] );

        }
        _isPaused = false;
    }
    
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
    }
    
    // Messages from the SDLMain thread
    static int COMMAND_CHANGE_TITLE = 1;

    // Handler for the messages
    Handler commandHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == COMMAND_CHANGE_TITLE) {
                setTitle((String)msg.obj);
            }
        }
    };

    // Send a message from the SDLMain thread
    void sendCommand(int command, Object data) {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        commandHandler.sendMessage(msg);
    }

    // C functions we call
    public static native void nativeInit();
    public static native void nativeQuit();
    public static native void onNativeResize(int x, int y, int format);
    public static native void onNativeKeyDown(int keycode);
    public static native void onNativeKeyUp(int keycode);
    public static native void onNativeSDLKeyDown(int keycode);
    public static native void onNativeSDLKeyUp(int keycode);
    public static native void onNativeTouch(int action, float x, 
                                            float y, float p);
    public static native void onNativeAccel(float x, float y, float z);
    public static native void nativeRunAudioThread();

    // from the N64 func ref: The 3D Stick data is of type signed char and in
    // the range between 80 and -80. (32768 / 409 = ~80.1)
    public static native void updateVirtualGamePadStates( boolean[] buttons, int axisX, int axisY );
    public static native void pauseEmulator();
    public static native void resumeEmulator();
    public static native void stopEmulator();
    public static native void stateSetSlotEmulator( int slotID );
    public static native void stateSaveEmulator();
    public static native void stateLoadEmulator();
    public static native void fileSaveEmulator( String filename );
    public static native void fileLoadEmulator( String filename );
    public static native int stateEmulator();

    // TODO: The proper native way to reset crashes the emulator!
    public static void resetEmulator()
    { // restart the entire SDLActivity (slower, but it works)
        if( mSingleton == null )
            return;  // can't do it statically
        Intent intent = new Intent( mSingleton, SDLActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        mSingleton.startActivity( intent );
    }

    public static void updateSDLButtonStates( boolean[] SDLButtonPressed, int[] SDLButtonCodes, int SDLButtonCount )
    {
        if( mSurface == null )
            return;
        for( int x = 0; x < SDLButtonCount; x++ )
        {
            if( SDLButtonPressed[x] != previousKeyStates[x] )
            {
                previousKeyStates[x] = SDLButtonPressed[x];
                if( SDLButtonPressed[x] )
                    mSurface.onSDLKey( SDLButtonCodes[x], KeyEvent.ACTION_DOWN );
                else
                    mSurface.onSDLKey( SDLButtonCodes[x], KeyEvent.ACTION_UP );
            }
        }
    }

    // Java functions called from C
    public static boolean createGLContext(int majorVersion, int minorVersion) {
        return mSurface.initEGL(majorVersion, minorVersion);
    }
    public static boolean useRGBA8888()
    {
        return rgba8888;
    }

    public static void flipBuffers()
    {
        mSurface.flipEGL();
        if( frameCount < 0 )
        {
            frameCount = 0;
            lastFPSCheck = System.currentTimeMillis();
        }
        frameCount++;
        if( (mGamePad != null && frameCount >= mGamePad.fpsRate) ||
            (mGamePad == null && frameCount >= fpsRate) )
        {
            long currentTime = System.currentTimeMillis();
            float fFPS = ( (float) frameCount / (float) (currentTime - lastFPSCheck) ) * 1000.0f;
            if( mGamePad != null )
                mGamePad.updateFPS( (int) fFPS );
            frameCount = 0;
            lastFPSCheck = currentTime;
        }
    }

    public static void setActivityTitle(String title) {
        // Called from SDLMain() thread and can't directly affect the view
        mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
    }

    public static Object getDataDir()
    {
        return (Object) Globals.DataDir;
    }
    public static int getHardwareType()
    {
        return Globals.hardwareType;
    }

    public static Object getROMPath()
    {
        finishedReading = false;
        if( Globals.chosenROM == null || Globals.chosenROM.length() < 1 )
        {
            finishedReading = true;
            //return (Object) (Globals.DataDir + "/roms/mupen64plus.v64");
            System.exit( 0 );
        }
        else if( Globals.chosenROM.substring( Globals.chosenROM.length() - 3, Globals.chosenROM.length() ).equalsIgnoreCase( "zip" ) )
        {
            // create the tmp folder if it doesn't exist:
            File tmpFolder = new File( Globals.DataDir + "/tmp" );
            tmpFolder.mkdir();
            // clear the folder if anything is in there:
            String[] children = tmpFolder.list();
            for( int i=0; i < children.length; i++ )
            {
                deleteFolder( new File( tmpFolder, children[i] ) );
            }
            tmpFile = unzipFirstROM( new File( Globals.chosenROM ), Globals.DataDir + "/tmp" );
            if( tmpFile == null )
            {
                Log.v( "SDLActivity", "Unable to play zipped ROM: '" + Globals.chosenROM + "'" ); 
                notificationManager.cancel( Globals.NOTIFICATION_ID );
                if( Globals.errorMessage != null )
                {
                    MenuActivity.error_log.put( "OPEN_ROM", "fail_crash", Globals.errorMessage );
                    MenuActivity.error_log.save();
                }
                Intent intent = new Intent( mSingleton, MenuActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                mSingleton.startActivity( intent );
                finishedReading = true;
                System.exit( 0 );
            }
            else
            {
                finishedReading = true;
                return (Object) tmpFile;
            }
        }
        else
        {
            finishedReading = true;
            return (Object) Globals.chosenROM;
        }
        finishedReading = true;
        return (Object) Globals.chosenROM;
    }

    public static boolean deleteFolder( File folder )
    {
        if( folder.isDirectory() )
        {
            String[] children = folder.list();
            for( int i=0; i < children.length; i++ )
            {
                boolean success = deleteFolder( new File( folder, children[i] ) );
                if( !success )
                    return false;
            }
        }
        return folder.delete();
    }

    public static String unzipFirstROM( File archive, String outputDir )
    {
        String romName, romExt;
        String supportedExt = ".z64.v64.n64";

        if( archive == null )
            Globals.errorMessage = "Zip file null in method unzipFirstROM";
        else if( !archive.exists() )
            Globals.errorMessage = "Zip file '" + archive.getAbsolutePath() + "' does not exist";
        else if( !archive.isFile() )
            Globals.errorMessage = "Zip file '" + archive.getAbsolutePath() + "' is not a file (method unzipFirstROM)";

        if( Globals.errorMessage != null )
        {
            Log.e( "SDLActivity", Globals.errorMessage );
            return null;
        }
        try
        {
//Log.v( "SDLActivity (unzipFirstROM)", "(1)" );
            ZipFile zipfile = new ZipFile( archive );
//Log.v( "SDLActivity (unzipFirstROM)", "(2)" );
            Enumeration e = zipfile.entries();
//Log.v( "SDLActivity (unzipFirstROM)", "(3)" );
            while( e.hasMoreElements() )
            {
//Log.v( "SDLActivity (unzipFirstROM)", "(3.a)" );
                ZipEntry entry = (ZipEntry) e.nextElement();
//Log.v( "SDLActivity (unzipFirstROM)", "(3.b)" );
                if( entry != null && !entry.isDirectory() )
                {
                    romName = entry.getName();
//Log.v( "SDLActivity (unzipFirstROM)", "(3.b.1)" );
                    if( romName != null && romName.length() > 3 )
                    {
//Log.v( "SDLActivity (unzipFirstROM)", "(3.b.1.a)" );
                        romExt = romName.substring( romName.length() - 4, romName.length() ).toLowerCase();
//Log.v( "SDLActivity (unzipFirstROM)", "(3.b.1.b)" );
                        if( supportedExt.contains( romExt ) )
                            return unzipEntry( zipfile, entry, outputDir );
                    }
                }
            }
        }
        catch( ZipException ze )
        {
            Globals.errorMessage = "Zip Error!  Ensure file is a valid .zip archive and is not corrupt";
            Log.e( "SDLActivity", "ZipException in method unzipFirstROM", ze );
            return null;
        }
        catch( IOException ioe )
        {
            Globals.errorMessage = "IO Error!  Please report, so problem can be fixed in future update";
            Log.e( "SDLActivity", "IOException in method unzipFirstROM", ioe );
            return null;
        }
        catch( Exception e )
        {
            Globals.errorMessage = "Error! Please report, so problem can be fixed in future update";
            Log.e( "SDLActivity", "Unzip error", e );
            return null;
        }
        Globals.errorMessage = "No compatible ROMs found in .zip archive";
        Log.e( "SDLActivity", Globals.errorMessage );
        return null;
    }
 
    public static boolean unzipAll( File archive, String outputDir )
    {
        if( archive == null )
            Globals.errorMessage = "Zip file null in method unzipAll";
        else if( !archive.exists() )
            Globals.errorMessage = "Zip file '" + archive.getAbsolutePath() + "' does not exist";
        else if( !archive.isFile() )
            Globals.errorMessage = "Zip file '" + archive.getAbsolutePath() + "' is not a file (method unzipFirstROM)";

        if( Globals.errorMessage != null )
        {
            Log.e( "SDLActivity", Globals.errorMessage );
            return false;
        }
        try
        {
            File f;
            ZipFile zipfile = new ZipFile( archive );
            Enumeration e = zipfile.entries();
            while( e.hasMoreElements() )
            {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if( entry != null && !entry.isDirectory() )
                {
                    f = new File( outputDir + "/" + entry.toString() );
                    f = f.getParentFile();
                    if( f != null )
                    {
                        f.mkdirs();
                        unzipEntry( zipfile, entry, f.getAbsolutePath() );
                    }
                }
            }
        }
        catch( ZipException ze )
        {
            Globals.errorMessage = "Zip Error!  Ensure file is a valid .zip archive and is not corrupt";
            Log.e( "SDLActivity", "ZipException in method unzipAll", ze );
            return false;
        }
        catch( IOException ioe )
        {
            Globals.errorMessage = "IO Error!  Please report, so problem can be fixed in future update";
            Log.e( "SDLActivity", "IOException in method unzipAll", ioe );
            return false;
        }
        catch( Exception e )
        {
            Globals.errorMessage = "Error! Please report, so problem can be fixed in future update";
            Log.e( "SDLActivity", "Unzip error", e );
            return false;
        }
        return true;
    }

    private static String unzipEntry( ZipFile zipfile, ZipEntry entry, String outputDir ) throws IOException
    {
//Log.v( "SDLActivity (unzipEntry)", "    (1)" );
        if( entry.isDirectory() )
        {
//Log.v( "SDLActivity (unzipEntry)", "    (1.a)" );
            Globals.errorMessage = "Error! .zip entry '" + entry.getName() + "' is a directory, not a file";
//Log.v( "SDLActivity (unzipEntry)", "    (1.b)" );
            Log.e( "SDLActivity", Globals.errorMessage );
//Log.v( "SDLActivity (unzipEntry)", "    (1.c)" );
            return null;
        }

//Log.v( "SDLActivity (unzipEntry)", "    (2)" );
        File outputFile = new File( outputDir, entry.getName() );
//Log.v( "SDLActivity (unzipEntry)", "    (3)" );
        String newFile = outputFile.getAbsolutePath();

//Log.v( "SDLActivity (unzipEntry)", "    (4)" );
        BufferedInputStream inputStream = new BufferedInputStream( zipfile.getInputStream( entry ) );
//Log.v( "SDLActivity (unzipEntry)", "    (5)" );
        BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream( outputFile ) );
//Log.v( "SDLActivity (unzipEntry)", "    (6)" );
        byte b[] = new byte[1024];
        int n;
//Log.v( "SDLActivity (unzipEntry)", "    (7)" );
        while( ( n = inputStream.read( b, 0, 1024 ) ) >= 0 )
        {
            outputStream.write( b, 0, n );
        }
//Log.v( "SDLActivity (unzipEntry)", "    (8)" );
        outputStream.close();
//Log.v( "SDLActivity (unzipEntry)", "    (9)" );
        inputStream.close();
//Log.v( "SDLActivity (unzipEntry)", "    (10)" );

        return newFile;
    }

// TODO: Move this from here.. too hackish
    public static boolean getScreenStretch()
    {
        return Globals.screen_stretch;
    }
    public static boolean getAutoFrameSkip()
    {
        return Globals.auto_frameskip;
    }
    public static int getMaxFrameSkip()
    {
        return Globals.max_frameskip;
    }
//

    public static void showToast( String message )
    {
        if( mSingleton == null )
            return;
        if( toast != null )
            toast.setText( new String( message ) );
        else
        {
            toast = Toast.makeText( mSingleton, new String( message ), Toast.LENGTH_SHORT );
            toast.setGravity( Gravity.BOTTOM, 0, 0 );
        }
        // Toast messages must be run on the UiThread, which looks ugly as hell, but works:
        if( toastMessager == null )
            toastMessager = new Runnable()
                            {
                                public void run()
                                {
                                    if( toast != null )
                                        toast.show();
                                }
                            };
        mSingleton.runOnUiThread( toastMessager );
    }

    // Audio
    private static Object buf;
    
    public static Object audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);
        
        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);
        
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);
        
        audioStartThread();
        
        if (is16Bit) {
            buf = new short[desiredFrames * (isStereo ? 2 : 1)];
        } else {
            buf = new byte[desiredFrames * (isStereo ? 2 : 1)]; 
        }
        return buf;
    }
    
    public static void audioStartThread() {
        mAudioThread = new Thread(new Runnable() {
            public void run() {
                try
                {
                    mAudioTrack.play();
                    nativeRunAudioThread();
                }
                catch( IllegalStateException ise )
                {
                    Log.e( "SDLActivity", "audioStartThread IllegalStateException", ise );
                    showToast( "Audio track illegal state.  Please report at google.com" );
                }
            }
        });
        
        // I'd take REALTIME if I could get it!
        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
    }
    
    public static void audioWriteShortBuffer(short[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }
    
    public static void audioWriteByteBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }

    public static void audioQuit() {
        if (mAudioThread != null) {
            try {
                mAudioThread.join();
            } catch(Exception e) {
                Log.v("SDL", "Problem stopping audio thread: " + e);
            }
            mAudioThread = null;

            //Log.v("SDL", "Finished waiting for audio thread");
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }
    private static void readCpuInfo()
    {
        Log.v( "SDLActivity.java", "CPU info available from file /proc/cpuinfo:" );
        ProcessBuilder cmd;
        try
        {
            String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
            cmd = new ProcessBuilder( args );
            java.lang.Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            String line;
            String[] lines;
            String[] splitLine;
            String processor = null;
            String features = null;
            String hardware = null;
            int x;
            if( in.read( re ) != -1 )
            {
                line = new String( re );
                Log.v( "SDLActivity.java", line );
                lines = line.split( "\\r\\n|\\n|\\r" );
                if( lines != null )
                {
                    for( x = 0; x < lines.length; x++ )
                    {
                        splitLine = lines[x].split( ":" );
                        if( splitLine != null && splitLine.length == 2 )
                        {
                            if( processor == null && splitLine[0].trim().toLowerCase().equals( "processor" ) )
                                processor = splitLine[1].trim().toLowerCase();
                            else if( features == null && splitLine[0].trim().toLowerCase().equals( "features" ) )
                                features = splitLine[1].trim().toLowerCase();
                            else if( hardware == null && splitLine[0].trim().toLowerCase().equals( "hardware" ) )
                                hardware = splitLine[1].trim().toLowerCase();
                        }
                    }
                }
            }
            if( hardware != null && ( hardware.indexOf( "mapphone" ) != -1 ||
                                      hardware.indexOf( "smdkv" ) != -1 ||
                                      hardware.indexOf( "herring" ) != -1 ||
                                      hardware.indexOf( "aries" ) != -1 ) )
                Globals.hardwareType = Globals.HARDWARE_TYPE_OMAP;
            else if( hardware != null && ( hardware.indexOf( "liberty" ) != -1 ||
                                           hardware.indexOf( "gt-s5830" ) != -1 ||
                                           hardware.indexOf( "zeus" ) != -1 ) )
                Globals.hardwareType = Globals.HARDWARE_TYPE_QUALCOMM;
            else if( hardware != null && hardware.indexOf( "imap" ) != -1 )
                Globals.hardwareType = Globals.HARDWARE_TYPE_IMAP;
            else if( ( hardware != null && ( hardware.indexOf( "tegra 2" ) != -1 ||
                                             hardware.indexOf( "meson-m1" ) != -1 ||
                                             hardware.indexOf( "smdkc" ) != -1 ) ) ||
                     ( features != null && features.indexOf( "vfpv3d16" ) != -1 ) )
                Globals.hardwareType = Globals.HARDWARE_TYPE_TEGRA2;
            in.close();
        }
        catch( IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

}
