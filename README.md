# MAKE SURE YOU READ THIS 

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
    
    
   # Note: 
   When I pressed the button, it gave me: 
   *XXXX Illinois Ave, St.Charles, IL 60174, USA, St. Charles*
   
   (I put XXXX for privacy matter)
   
   So here it repeated the *St. Charles* twice.
   To remove that, go to line *250*: 
   
   ```result = address.getAddressLine(0) + ", " + address.locality```
   
   Remove the ``` + ", " + address.locality ```
   So you get ```result = address.getAddressLine(0)```
    which will remove the excessive *St. Charles*.
    
   But keep in mind that since it's the locality, it helps with the accuracy of the information. 
