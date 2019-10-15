# LocationExample

  Thanks to these guys:

 1. Longitude + Latitude:
   https://www.youtube.com/watch?v=wUkMG3JGA84
   https://github.com/kmvignesh/LocationExample

 > If you watch this ^, he does 2 location: Gps and Network, then
 > pick the one that is more precise.
 > Here I just use the gps one

 2. Reverse Geocoder attempt:
   https://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation


  ** Put the following into Manifest:

     <!--Allow Internet-->
    <uses-permission android:name="android.permission.INTERNET"/>

    <!--Allow location-->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
