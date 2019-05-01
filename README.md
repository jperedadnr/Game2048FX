2048FX
======

This is another version of the game 2048, this time built using JavaFX, JavaFXPorts mobile [plugin](https://bitbucket.org/javafxports/javafxmobile-plugin), [Gluon Mobile](https://gluonhq.com/products/mobile/) and [Charm Down](https://bitbucket.org/gluon-oss/charm-down) to run on Desktop, Android and iOS platforms with the very same codebase.

The game is based on a fork of the fully Java 8 [version](https://github.com/brunoborges/fx2048)
by Bruno <a href="mailto:bruno.borges@oracle.com">Borges</a> & José <a href="mailto:jperedadnr@gmail.com">Pereda</a>, which was also based on a 
fork based on the original Javascript [version](https://github.com/gabrielecirulli/2048) by Gabriele Cirulli.

 - Android and Desktop: José Pereda
 - iOS version: Jens <a href="mailto:mail@jensd.de">Deters</a>

<img src="https://github.com/jperedadnr/Game2048FX/blob/master/screenshot.png" width="306">

### Leaderboard

To enable the leaderboard feature you need a valid subscription 
to Gluon [CloudLink](https://gluonhq.com/products/cloudlink/). 
You can get it here, and there is a 30-day free trial. 
Sign up and get a valid account on Gluon CloudLink.

#### Desktop

Just run it from your IDE or from command line: `./gradlew run`

#### Android

Connect your Android device and run `./gradlew androidInstall`

#### iOS
 
Connect your iOS device and run `./gradlew launchIOSDevice`


Google Play (Android)
=====================

<a href="https://play.google.com/store/apps/details?id=org.jpereda.game2048"><img src="https://github.com/jperedadnr/Game2048FX/blob/master/src/android/res/mipmap-xhdpi/ic_launcher.png" width="75">App for Android</a>

Blog [post](http://jperedadnr.blogspot.com/2015/03/javafx-on-mobile-dream-come-true.html)

License
===================

The project is licensed under GPL 3. See [LICENSE](https://github.com/jperedadnr/Game2048FX/blob/master/LICENSE)
file for the full license.