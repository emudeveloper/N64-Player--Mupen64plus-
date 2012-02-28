
/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>

// Called before SDL_main() to initialize JNI bindings in SDL library
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);
// Used to look up which ROM to run
extern "C" char * Android_JNI_GetROMPath();

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    return JNI_VERSION_1_4;
}

// Start up the SDL app
extern "C" void Java_com_emudev_n64_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jobject obj)
{
    /* This interface could expand with ABI negotiation, calbacks, etc. */
    SDL_Android_Init(env, cls);

    /* Run the application code! */
    int status;

    /* Let's play Mario 64 */
    char *argv[4];

//    argv[0] = strdup("mupen64plus");
//    argv[1] = strdup("--saveoptions");
//    argv[2] = strdup( /*"roms/mario.n64"*/ Android_JNI_GetROMPath() );
//    argv[3] = NULL;
//    status = SDL_main(3, argv);

    argv[0] = strdup("mupen64plus");
    argv[1] = strdup( /*"roms/mario.n64"*/ Android_JNI_GetROMPath() );
    argv[2] = NULL;
    status = SDL_main(2, argv);

    /* We exit here for consistency with other platforms. */
    exit(status);
}

/* vi: set ts=4 sw=4 expandtab: */
