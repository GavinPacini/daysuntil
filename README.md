Days Until
===================
A simple Android app demonstrating the use of Kotlin, RxJava, Realm and more...

<p align="center">
    <img src="images/device.png" alt="Web Launcher"/>
</p>

What do I mean by "more"? Here's exactly what I've made extensive use of:

 - [Kotlin](https://kotlinlang.org/)
 - [RxJava](https://github.com/ReactiveX/RxJava) and [RxAndroid](https://github.com/ReactiveX/RxAndroid)
 - [Realm](https://realm.io/) with help from [Realm with RxJava](https://github.com/kboyarshinov/realm-rxjava-example)
 - [EasyAdapter](https://github.com/ribot/easy-adapter)
 - [Kotter Knife](https://github.com/JakeWharton/kotterknife)
 - [Material DateTime Picker](https://github.com/wdullaer/MaterialDateTimePicker)
 - [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)
 - [Android-Crop](https://github.com/jdamcd/android-crop)
 
P.S: They are all amazing.

I used this great project, [Android Boilerplate](https://github.com/hitherejoe/Android-Boilerplate), as a reference.

I had been looking for something like this myself and couldn't find one. So, if you were like me, I hope you learn something from this!

Requirements
------------
 - [Android SDK](http://developer.android.com/sdk/index.html)
 - Android SDK Build tools 23.0.2
 - Android Support Repository and Library
 - Gradle Plugin 1.5.0
 
Building
--------
The easiest way to setup this project is to simply open it with Android Studio, like you would any other project.
 
However, if you don't want to import it, you can run this from the root of the project to build, install and run:

    ./gradlew installRunDebug

Possible Upcoming Features
--------
 - Unit Tests
 - Automated Tests
 - Rx-ify more of the app
 - Re-crop image without reselecting one in Event Activity

*I need to write some tests*

Minor Bugs
--------
 - After undoing the deletion of an event, the entire view refreshes. Quite annoying.
 
Notes
--------
Due to the way Realm works, it's impossible to iterate over each individual result **as they are returned.** Realm returns a `RealmResult` object which contains the results of your query. Therefore, when using RxJava, it is not possible to call `onNext` for each result, you must call it only at the end after you have your `RealmResult` object.


Pull Requests
--------
If you see something you think could be better, please don't hesitate to submit a pull request! 

License
--------------

    Copyright 2015 Gavin Pacini

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.