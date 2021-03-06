# howalarming-android

Companion application for [HowAlarming](https://github.com/jethrocarr/howalarming)
which offers a native Android client which can recieve push messages from a
house alarm via the HowAlarming platform.

This is an open source application which you'll need to build yourself, you
won't find it on the app store. See the instructions below.

![Current Version Screenshot](https://raw.githubusercontent.com/jethrocarr/howalarming-android/master/docs/Screenshot_2016-01-11-22-32-15.png)


# Functionality

Currently quite basic. I'd like to extend on this in future and of course, pull
requests are always very welcome. Especially around the UI side of things.

Current functionality includes:

1. Push notification of alarm arm/disarms, faults and of course alarm events.

2. UI showing recent events and status (colour coded).

3. Vibration & Sound when something bad happens.


# Installation

Firstly make sure you have a functional [HowAlarming](https://github.com/jethrocarr/howalarming)
environment up and running. Recommend testing with the email alerter to ensure
the basics are working first.

Then you need to build this application. To do so:

1. Check out this source code repository.

2. Create a new project in Google Apps and enable Google Compute Messaging in it at the [Google Developer Console](https://developers.google.com/mobile/add?platform=android&cntapi=gcm&cnturl=https:%2F%2Fdevelopers.google.com%2Fcloud-messaging%2Fandroid%2Fclient&cntlbl=Continue%20Adding%20GCM%20Support&%3Fconfigured%3Dtrue)

3. Copy the provided `google-services.json` file to the app/ directory.

4. Open this project with [Android Studio](http://developer.android.com/sdk/index.html)

5. Connect your phone and use the `Build -> Run` menu option to install the app
   to your device.

6. After the first launch of the app, copy the `GCM registration token` which is
   logged out in the logcat window in Android Studio. This is needed since the
   app currently doesn't support calling back to the HowAlarming server with it's
   registration ID and the server needs to know what it is.

7. Add the token to your `config.yaml` file in HowAlarming and restart the
   daemon.

8. Do an arm & disarm of your alarm. It should generate a notification on your
   device. If this works, good job - celebrate your new
   internet-of-mobile-things alarm system.


# I'm lazy, can you send me an APK and/or put this on the app store?

I've intentionally not done this. Two main reasons - the first is that I don't
want to feel liable to people for guaranteeing their app/alarm works properly.

The second is that the only way to make a distributable version of this app
given that the user also controls the server, would be to setup an intermediary
service to authenticate/register users and pass messages to Google Cloud
Messaging which would instantly make people dependent on my systems. And then
I'm just another dodgy Internet-of-Things vendors with no SLA and no recourse if
I just turned it off one day.

Setting up GCM with Google and building the app is more work, but it guarantees
that the only third party between your HowAlarming server-side and your
HowAlarming client-side is yourself and Google's Android push services.


# Troubleshooting

The AndroidStudio is the easiest way of debugging problems with the application
including both "why won't it build?" and "why does it just crash when I run it?".


# About

Written by Jethro Carr. Possibly not my greatest Android app effort since it's
the first I've written for this platform. If it somehow doesn't crash and is
actually useful for you, please send me beer. :-)

Pull requests including docs, bug fixes, better build/setup instructions and of
course application UI & functionality improvements are all very welcome as well
instead (or in addition?) to beer.


# License

Unless otherwise stated, all source code is:

    Copyright (c) 2016 Jethro Carr

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in
    the Software without restriction, including without limitation the rights to
    use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
    of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
