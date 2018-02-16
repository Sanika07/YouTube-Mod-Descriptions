# YouTube-Mod-Descriptions
Extension to the YouTube Data API. 

Allows a youtube creator to update all of their video descriptions based on a search key. 
This is useful for updating links, social media handles, or any repetitive text that needs to be applied to multiple videos.

Currently, input is hard coded so do not use this on your own videos unless you know what you are doing. I made a quick
fix to update my video descriptions (line 159 src/main/java/Quickstart.java). Soon I will make a nice interface without hard coded variables.

If you want to implement this, you need an API key. https://developers.google.com/youtube/v3/getting-started
Download the client_secret.json and place it in YouTube-Mod-Descriptions/src/main/resources/client_secret.json

I have plans to convert this to a web app which makes it easy for people to use.
