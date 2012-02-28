package com.emudev.n64;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

// TODO: Comment thoroughly
public class MainActivity extends Activity
{
    public static MainActivity mInstance = null;
    public static boolean _isPaused = false;

    // Data Downloader
    public static DataDownloader downloader = null;
    public ImageView _img = null;
    public TextView _tv = null;
    public LinearLayout _layout = null;
    public LinearLayout _layout2 = null;
    public FrameLayout _videoLayout = null;


    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;
        // fullscreen mode
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
        if( Globals.InhibitSuspend )
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                                  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        _layout = new LinearLayout( this );
        _layout.setOrientation( LinearLayout.VERTICAL );
        _layout.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT,
                                                                ViewGroup.LayoutParams.FILL_PARENT ) );

        _layout2 = new LinearLayout( this );
        _layout2.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT,
                                                                 ViewGroup.LayoutParams.WRAP_CONTENT ) );
        _layout.addView( _layout2 );
        _img = new ImageView( this );
        _img.setScaleType( ImageView.ScaleType.FIT_CENTER );  // FIT_XY
        try
        {
            _img.setImageDrawable( Drawable.createFromStream( getAssets().open( "logo.png" ), "logo.png" ) );
        }
        catch( Exception e )
        {
            _img.setImageResource( R.drawable.publisherlogo );
        }
        _img.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT,
                                                          ViewGroup.LayoutParams.FILL_PARENT ) );
        _layout.addView( _img );
        _videoLayout = new FrameLayout( this );
        _videoLayout.addView( _layout );
        setContentView( _videoLayout );

        class Callback implements Runnable
        {
            MainActivity p;
            Callback( MainActivity _p )
            {
                p = _p;
            }
            public void run()
            {
                p.startDownloader();
            }
        };

        Thread downloaderThread = null;
        downloaderThread = new Thread( new Callback( this ) );
        downloaderThread.start();
    }

    @Override
    protected void onPause()
    {
        if( downloader != null )
        {
            synchronized( downloader )
            {
                downloader.setStatusField( null );
            }
        }
        _isPaused = true;
        super.onPause();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if( downloader != null )
        {
            synchronized( downloader )
            {
                downloader.setStatusField( _tv );
                if( downloader.DownloadComplete )
                    downloaderFinished();
                else if( downloader.DownloadFailed )
                {
                    downloader.DownloadFailed = false;
                    class Callback implements Runnable
                    {
                        MainActivity p;
                        Callback( MainActivity _p )
                        {
                            p = _p;
                        }
                        public void run()
                        {
                            p.startDownloader();
                        }
                    };

                    Thread downloaderThread = null;
                    downloaderThread = new Thread( new Callback( this ) );
                    downloaderThread.start();
                }
            }
        }
        _isPaused = false;
    }
    @Override
    protected void onDestroy() 
    {
        if( downloader != null )
        {
            synchronized( downloader )
            {
                downloader.setStatusField( null );
            }
        }
        super.onDestroy();
    }

    public void setUpStatusLabel()
    {
        MainActivity Parent = this;
        if( Parent._tv == null )
        {
            Parent._tv = new TextView( Parent );
            Parent._tv.setMaxLines( 1 );
            Parent._tv.setText( R.string.init );
            Parent._layout2.addView( Parent._tv );
        }
    }

    public void startDownloader()
    {
        System.out.println( "libSDL: Starting data downloader" );
        class Callback implements Runnable
        {
            public MainActivity Parent;
            public void run()
            {
                setUpStatusLabel();
                System.out.println("libSDL: Starting downloader");
//                if( MainActivity.downloader == null )
                    MainActivity.downloader = new DataDownloader( Parent, Parent._tv );
            }
        }
        Callback cb = new Callback();
        cb.Parent = this;
        this.runOnUiThread( cb );
    }
    public void downloaderFinished()
    {
        downloader = null;
        MenuActivity.mupen64plus_cfg = new Config( Globals.DataDir + "/mupen64plus.cfg" );
        MenuActivity.InputAutoCfg_ini = new Config( Globals.DataDir + "/data/InputAutoCfg.ini" );
        MenuActivity.gui_cfg = new Config( Globals.DataDir + "/data/gui.cfg" );
        MenuActivity.error_log = new Config( Globals.DataDir + "/error.log" );
        Intent intent = new Intent( mInstance, MenuActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        startActivity( intent );
        mInstance.finish();
        mInstance = null;
    }
}

