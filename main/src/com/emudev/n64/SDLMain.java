package com.emudev.n64;

import java.lang.Runnable;

/**
    Simple nativeInit() runnable
*/
class SDLMain implements Runnable
{
    public void run()
    {
        // Runs SDL_main()
        SDLActivity.nativeInit();

        //Log.v("SDL", "SDL thread terminated");
    }
}

