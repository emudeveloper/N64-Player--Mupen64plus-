package com.emudev.n64;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.*;

import java.io.IOException;
import java.io.InputStream;

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
    SDLSurface. This is what we draw on, so we need to know when it's created
    in order to do anything useful. 

    Because of this, that's where we set up the SDL thread
*/
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback, 
    View.OnKeyListener, View.OnTouchListener, SensorEventListener
{
    // Controlled by IME special keys used for analog input:
    private boolean[] mp64pButtons = new boolean[14];
    private int axisX = 0;
    private int axisY = 0;

    // This is what SDL runs in. It invokes SDL_main(), eventually
    private static Thread mSDLThread;
    private static int glMajorVersion;
    private static int glMinorVersion;
    
    // EGL private objects
    private EGLContext  mEGLContext;
    private EGLSurface  mEGLSurface;
    private EGLDisplay  mEGLDisplay;

    // Sensors
    private static SensorManager mSensorManager;

    public boolean[] pointers = new boolean[256];
    public int[] pointerX = new int[256];
    public int[] pointerY = new int[256];
    public boolean buffFlipped = false;

    // Startup    
    //public SDLSurface( Context context )
    public SDLSurface( Context context, AttributeSet attribs )
    {
        //super( context );
        super( context, attribs );

        int x;
        for( x = 0; x < 256; x++ )
        {
            pointers[x] = false;
            pointerX[x] = -1;
            pointerY[x] = -1;
        }
        for( x = 0; x < 14; x++ )
            mp64pButtons[x] = false;

        getHolder().addCallback( this ); 
    
        setFocusable( true );
        setFocusableInTouchMode( true );
        requestFocus();
        setOnKeyListener( this ); 
        setOnTouchListener( this );   

        mSensorManager = (SensorManager) context.getSystemService( "sensor" );  
        this.requestFocus();
        this.setFocusableInTouchMode( true );
    }

    // Called when we have a valid drawing surface
    public void surfaceCreated( SurfaceHolder holder )
    {
        enableSensor( Sensor.TYPE_ACCELEROMETER, true );
    }

    // Called when we lose the surface
    public void surfaceDestroyed( SurfaceHolder holder )
    {
        // Send a quit message to the application
        SDLActivity.nativeQuit();
        // Now wait for the SDL thread to quit
        if( mSDLThread != null )
        {
            try
            {
                mSDLThread.join();
            }
            catch( Exception e )
            {
                Log.v( "SDLSurface", "Problem stopping SDL thread: " + e );
            }
            mSDLThread = null;
        }
        enableSensor( Sensor.TYPE_ACCELEROMETER, false );
    }

    // Called when the surface is resized
    public void surfaceChanged( SurfaceHolder holder,
                                int format, int width, int height )
    {
        Log.v( "SDLSurface", "SDLSurface changed" );

        int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
        switch( format )
        {
            case PixelFormat.A_8:
                break;
            case PixelFormat.LA_88:
                break;
            case PixelFormat.L_8:
                break;
            case PixelFormat.RGBA_4444:
                sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
                break;
            case PixelFormat.RGBA_5551:
                sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
                break;
            case PixelFormat.RGBA_8888:
                sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
                break;
            case PixelFormat.RGBX_8888:
                sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
                break;
            case PixelFormat.RGB_332:
                sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
                break;
            case PixelFormat.RGB_565:
                sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
                break;
            case PixelFormat.RGB_888:
                // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
                sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
                break;
            case PixelFormat.OPAQUE:
                /* Not sure this is right, Android API says, "Sytem chooses an
                   opaque format", but how do we know which one?? */
                break;
            default:
                Log.v( "SDLSurface", "pixel format unknown " + format );
                break;
        }
        SDLActivity.onNativeResize( width, height, sdlFormat );

        mSDLThread = new Thread( new SDLMain(), "SDLThread" ); 
        mSDLThread.start();

        if( SDLActivity.resumeLastSession )
        {
            new Thread( "ResumeSessionThread" )
            {
                @Override
                public void run()
                {
                    int c = 0;

                    while( !SDLActivity.finishedReading )
                    {
                        try{Thread.sleep( 40 );}catch(InterruptedException e){}
                        c++;
                    }
                    try{Thread.sleep( 40 );}catch(InterruptedException e){}
                    c = 0;
                    int state = SDLActivity.stateEmulator();
                    while( state != SDLActivity.EMULATOR_STATE_RUNNING )
                    {
                        try{Thread.sleep( 40 );}catch(InterruptedException e){}
                        state = SDLActivity.stateEmulator();
                        c++;
                    }
                    buffFlipped = false;

                    c = 0;
                    while( !buffFlipped )
                    { // wait for the game to have started, as indicated
                      // by a call to flip the EGL buffers.
                        try{Thread.sleep( 20 );}catch(InterruptedException e){}
                        c++;
                    }
                    try{Thread.sleep( 40 );}catch(InterruptedException e){}  // just to be sure..
                    Log.v( "SDLSurface", "Resuming last session" );
                    SDLActivity.showToast( "Resuming game" );
                    SDLActivity.fileLoadEmulator( "Mupen64PlusAE_LastSession.sav" );
                }
            }.start();
        }
    }

    // unused
    public void onDraw( Canvas canvas )
    {}


    // EGL functions
    public boolean initEGL( int majorVersion, int minorVersion )
    {
//Log.v( "**SDLSurface**", "inside initEGL" );

        Log.v( "SDLSurface", "Starting up OpenGL ES " + majorVersion + "." + minorVersion );
        glMajorVersion = majorVersion;
        glMinorVersion = minorVersion;

        try
        {
//Log.v( "**SDLSurface**", "(1)" );
            EGL10 egl = (EGL10)EGLContext.getEGL();

//Log.v( "**SDLSurface**", "(2)" );
            EGLDisplay dpy = egl.eglGetDisplay( EGL10.EGL_DEFAULT_DISPLAY );

//Log.v( "**SDLSurface**", "(3)" );
            int[] version = new int[2];
            egl.eglInitialize( dpy, version );

//Log.v( "**SDLSurface**", "(4)" );
            int EGL_OPENGL_ES_BIT = 1;
            int EGL_OPENGL_ES2_BIT = 4;
            int renderableType = 0;
            if( majorVersion == 2 )
            {
                renderableType = EGL_OPENGL_ES2_BIT;
            }
            else if( majorVersion == 1 )
            {
                renderableType = EGL_OPENGL_ES_BIT;
            }
//Log.v( "**SDLSurface**", "(5)" );
            int[] configSpec;
            if( SDLActivity.rgba8888 )
                configSpec = new int[]
                {
                    EGL10.EGL_RED_SIZE,   8,  // paulscode: get a config with red 8
                    EGL10.EGL_GREEN_SIZE,   8,  // paulscode: get a config with green 8
                    EGL10.EGL_BLUE_SIZE,   8,  // paulscode: get a config with blue 8
                    EGL10.EGL_ALPHA_SIZE,   8,  // paulscode: get a config with alpha 8
                    EGL10.EGL_DEPTH_SIZE,   16,  // paulscode: get a config with depth 16
                    EGL10.EGL_RENDERABLE_TYPE, renderableType,
                    EGL10.EGL_NONE
                };
            else
                configSpec = new int[]
                {
                    EGL10.EGL_DEPTH_SIZE,   16,  // paulscode: get a config with depth 16
                    EGL10.EGL_RENDERABLE_TYPE, renderableType,
                    EGL10.EGL_NONE
                };

//Log.v( "**SDLSurface**", "(6)" );
            EGLConfig[] configs = new EGLConfig[1];
            int[] num_config = new int[1];
            if( !egl.eglChooseConfig( dpy, configSpec, configs, 1, num_config ) || num_config[0] == 0 )
            {
                Log.e( "SDLSurface", "No EGL config available" );
                return false;
            }
//Log.v( "**SDLSurface**", "(7)" );
            EGLConfig config = configs[0];
            // paulscode, GLES2 fix:
                int EGL_CONTEXT_CLIENT_VERSION=0x3098;
                int contextAttrs[] = new int[]
                {
                    EGL_CONTEXT_CLIENT_VERSION, majorVersion,
                    EGL10.EGL_NONE
                };
//Log.v( "**SDLSurface**", "(8)" );
                EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, contextAttrs);
            // end GLES2 fix
                //EGLContext ctx = egl.eglCreateContext( dpy, config, EGL10.EGL_NO_CONTEXT, null );

//Log.v( "**SDLSurface**", "(9)" );
            if( ctx == EGL10.EGL_NO_CONTEXT )
            {
                Log.e( "SDLSurface", "Couldn't create context" );
                return false;
            }

//Log.v( "**SDLSurface**", "(10)" );
            EGLSurface surface = egl.eglCreateWindowSurface( dpy, config, this, null );
            if( surface == EGL10.EGL_NO_SURFACE )
            {
                Log.e( "SDLSurface", "Couldn't create surface" );
                return false;
            }
//Log.v( "**SDLSurface**", "(11)" );

            if( !egl.eglMakeCurrent( dpy, surface, surface, ctx ) )
            {
                Log.e( "SDLSurface", "Couldn't make context current" );
                return false;
            }

//Log.v( "**SDLSurface**", "(12)" );
            mEGLContext = ctx;
            mEGLDisplay = dpy;
            mEGLSurface = surface;
        }
        catch( Exception e )
        {
            Log.v("SDLSurface", e + "");
            for( StackTraceElement s : e.getStackTrace() )
            {
                Log.v( "SDLSurface", s.toString() );
            }
        }
//Log.v( "**SDLSurface**", "leaving initEGL" );

        return true;
    }

    // EGL buffer flip
    public void flipEGL()
    {
        try
        {
            EGL10 egl = (EGL10) EGLContext.getEGL();

            egl.eglWaitNative( EGL10.EGL_NATIVE_RENDERABLE, null );

            // drawing here

            egl.eglWaitGL();

            egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);

            
        }
        catch( Exception e )
        {
            Log.v( "SDLSurface", "flipEGL(): " + e );
            for( StackTraceElement s : e.getStackTrace() )
            {
                Log.v( "SDLSurface", s.toString() );
            }
        }
        buffFlipped = true;
    }

    public boolean onSDLKey( int keyCode, int action )
    {
        if( SDLActivity.mSingleton == null || SDLActivity.mSingleton.noInputPlugin )
            return false;

        if( action == KeyEvent.ACTION_DOWN )
        {
            SDLActivity.onNativeSDLKeyDown( keyCode );
            return true;
        }
        else if( action == KeyEvent.ACTION_UP )
        {
            SDLActivity.onNativeSDLKeyUp( keyCode );
            return true;
        }
        
        return false;
    }

    // Key events
    public boolean onKey( View  v, int keyCode, KeyEvent event )
    {
        if( SDLActivity.mSingleton == null || SDLActivity.mSingleton.noInputPlugin )
            return false;


        int key = keyCode;
        float str = 0;
        if( keyCode > 255 && Globals.analog_100_64 )
        {
            key = (int) ( keyCode / 100 );
            if( event.getAction() == KeyEvent.ACTION_DOWN )
                str = ( (float) keyCode - ( (float) key * 100.0f ) );
        }
        else if( event.getAction() == KeyEvent.ACTION_DOWN )
            str = 64.0f;
        int scancode;
        if( key < 0 || key > 255 )
            scancode = 0;
        else
            scancode = key;

        if( Globals.analog_100_64 && ( scancode == Globals.ctrl1[0] || scancode == Globals.ctrl1[1] ||
                                       scancode == Globals.ctrl1[2] || scancode == Globals.ctrl1[3] ) )
        {
            if( scancode == Globals.ctrl1[0] )
                axisX = (int) (80.0f * (str / 64.0f));
            else if( scancode == Globals.ctrl1[1] )
                axisX = (int) (-80.0f * (str / 64.0f));
            else if( scancode == Globals.ctrl1[2] )
                axisY = (int) (-80.0f * (str / 64.0f));
            else if( scancode == Globals.ctrl1[3] )
                axisY = (int) (80.0f * (str / 64.0f));
            SDLActivity.updateVirtualGamePadStates( mp64pButtons, axisX, axisY );
            return true;
        }
        // TODO: implement controllers 2 - 4
        else
        {
            if( event.getAction() == KeyEvent.ACTION_DOWN )
            {
                if( key == KeyEvent.KEYCODE_MENU )
                    return false;
                if( key == KeyEvent.KEYCODE_VOLUME_UP ||
                    key == KeyEvent.KEYCODE_VOLUME_DOWN )
                {
                    if( Globals.volumeKeysDisabled )
                    {
                        SDLActivity.onNativeKeyDown( key );
                        return true;
                    }
                    return false;
                }
                SDLActivity.onNativeKeyDown( key );
                return true;
            }
            else if( event.getAction() == KeyEvent.ACTION_UP )
            {
                if( key == KeyEvent.KEYCODE_MENU )
                    return false;

                if( key == KeyEvent.KEYCODE_VOLUME_UP ||
                    key == KeyEvent.KEYCODE_VOLUME_DOWN )
                {
                    if( Globals.volumeKeysDisabled )
                    {
                        SDLActivity.onNativeKeyUp( key );
                        return true;
                    }
                    return false;
                }
                SDLActivity.onNativeKeyUp( key );
                return true;
            }
        }        
        return false;
    }

    public boolean onTouch( View v, MotionEvent event )
    {
        if( SDLActivity.mSingleton == null || SDLActivity.mSingleton.noInputPlugin )
            return false;
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        float x = event.getX();
        float y = event.getY();
        float p = event.getPressure();

        // TODO: Anything else we need to pass?        
        SDLActivity.onNativeTouch( action, x, y, p );

        int maxPid = 0;
        int pid, i;
        if( actionCode == MotionEvent.ACTION_POINTER_DOWN )
        {
            pid = event.getPointerId( action >> MotionEvent.ACTION_POINTER_ID_SHIFT );
            if( pid > maxPid )
                maxPid = pid;
            pointers[pid] = true;
        }
        else if( actionCode == MotionEvent.ACTION_POINTER_UP )
        {
            pid = event.getPointerId( action >> MotionEvent.ACTION_POINTER_ID_SHIFT );
            if( pid > maxPid )
                maxPid = pid;
            pointers[pid] = false;
        }
        else if( actionCode == MotionEvent.ACTION_DOWN )
        {
            for( i = 0; i < event.getPointerCount(); i++ )
            {
                pid = event.getPointerId(i);
                if( pid > maxPid )
                    maxPid = pid;
                pointers[pid] = true;
            }
        }
        else if( actionCode == MotionEvent.ACTION_UP ||
                 actionCode == MotionEvent.ACTION_CANCEL )
        {
            for( i = 0; i < 256; i++ )
            {
                pointers[i] = false;
                pointerX[i] = -1;
                pointerY[i] = -1;
            }
        }

        for( i = 0; i < event.getPointerCount(); i++ )
        {
            pid = event.getPointerId(i);
            if( pointers[pid] )
            {
                if( pid > maxPid )
                    maxPid = pid;
                pointerX[pid] = (int) event.getX(i);
                pointerY[pid] = (int) event.getY(i);
//Log.v( "SDLSurface.java", "Pointer " + pid + " (" + pointerX[pid] + "," + pointerY[pid] + ")" );
            }
        }
        SDLActivity.mGamePad.updatePointers( pointers, pointerX, pointerY, maxPid );
        return true;
    }

    // Sensor events
    public void enableSensor(int sensortype, boolean enabled)
    {
        // TODO: This uses getDefaultSensor - what if we have >1 accels?
        if( enabled )
        {
            mSensorManager.registerListener( this, 
                            mSensorManager.getDefaultSensor( sensortype ), 
                            SensorManager.SENSOR_DELAY_GAME, null );
        }
        else
        {
            mSensorManager.unregisterListener( this, 
                            mSensorManager.getDefaultSensor( sensortype ) );
        }
    }
    
    public void onAccuracyChanged( Sensor sensor, int accuracy )
    {
        // TODO
    }

    public void onSensorChanged(SensorEvent event)
    {
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
            SDLActivity.onNativeAccel( event.values[0],
                                       event.values[1],
                                       event.values[2] );
        }
    }
}

