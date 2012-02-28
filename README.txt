The setup for this project is slightly more complex than a normal Android project.  There are two reasons for this complexity, which I will try to explain.

Firstly, this project has two branches.  The main branch is for the majority of Android devices.  A separate branch is customized specifically for the unique features of the Xperia Play.  In the interest of sharing the majority of the code, I've placed only the Xperia Play-specific code in "xperia-play", set up the Android.mk files to link with the native libraries in "main", and added symbolic links to the common Java files in "main".  There may be a better way to set this up so that all the code could be contained in a single project, where the Xperia Play components are only linked when a flag or boolean is set.  Then simply changing the flag or boolean to build one branch or the other would make the project less complex and the source code easier to navigate.  I will explore this idea in the future.

Secondly, there is a problem with the core if it is not built with the "APP_OPTIM := debug" option specified in Application.mk.  However, adding this option to the entire project affects all the libraries, not just the core.  This results in the emulator running noticeably slower.  To work around this problem, I've separated the core from the main project, and set up the Android.mk files to link with it.  That way, the core project can have "APP_OPTIM := debug" specified, and the main project can have "APP_OPTIM := release".  At some point it would be useful to figure out why the core has this problem in the first place.  Fixing the problem so the core could be built along with the main project with "APP_OPTIM := release" would not only reduce the complexity of the project, but it would most likely speed up the emulator as well.


To build from source (follow steps in this order):

1) Run ndk-build in the core-debug folder
2) Run ndk-build in the main folder
3) (Optional) Run ndk-build in the xperia-play folder
4) Run "ant debug" (or "ant release") in the main (or xperia-play) folder, to generate the .apk file(s)

