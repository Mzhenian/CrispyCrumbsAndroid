
 # Crispy Crumbs - Android App

 Welcome to the Crispy Crumbs app, a dedicated video sharing platform showcasing only the finest Crispy Crumbs content. This app is built with Android Studio to ensure a smooth, interactive, and tasty user experience.

### Crispy Crumbs - founders
Ofek Avan Danan 211824727
Zohar Mzhen 314621806
Dolev Menajem 207272220

## Running the Crispy Crumbs App
The app is built for android versions 10 up to 14, and is designed to run on android devices as well as on emulators.
Getting the Crispy Crumbs app to work on your device is simple and straightforward:

1. ### 'Personal Android Device' :
   There are two crispy-golden ways to run the app on a compatible android device.
   First: Install the APK from  the [releases section of the GITHUB page](https://github.com/Mzhenian/CrispyCrumbsAndroid/releases/tag/v1) and run the app. 
   Second: Download the entire project and open it in android studio - add your personal (physical) android device to the studio devices by either connecting it with a usb to your machine, or through wi-fi. (Note that this method require enabling ADB debugging through the developer options in the device.) - insure you are on the "app" run/debug configuration and press the run button to lunch the home page of CrispyCrumbs.
2. ### 'Emulator' :
   Download the [entire project](https://github.com/Mzhenian/CrispyCrumbsAndroid.git) and open project in Android studio - add a virtual device emulator to your liking through the device manager on the right tool bar of android studio, and run the app on the emulator.

   At any stage you can travel through the app using the navigation side menu, which adapt to the user's login status and shows only the relevant options.


## App Pages
0. ### Navigation Menu

   The app features an omni-present, all powerful navigation menu you can open with a click of the three-lines button at the corner of the top bar.
   The navigation bar will show different option whether a user is logged in or not, specifically, if there are no logged users it will show the option to log in and sign up, and if a user is logged
   it will show the option to logout or go to the video upload page, video edit page, or your uploaded videos list page.
    
    ![NavBar](./demonstration/NavBar.jpg)
    ![Guest Bar](./demonstration/Guest_Bar.jpg)

1. ### Homepage
  
    The homepage of the Crispy Crumbs app features a search bar at the top, and a list of available videos, each presented with his title, thumbnail, uploader, views count and upload date.

   ![HomePage](./demonstration/HomePage.jpg)

   Additionally we implemented a personal video list, from which a user can edit his videos to his liking. 
   ![my videos](./demonstration/my_videos.jpg)

2. ### Sign Up
  
   It includes the crucial fields of email address, username, password and displayed name (to mask the username) with the optional birthday and phone number and the option to upload a profile picture. 
   The form includes "Sign up" (which validate the entered data before creating a user node from them) and "Log In" button, in case you already have an account.

   ![SignUp](./demonstration/SignUp.jpg)

3. ### Login

   It includes fields for entering the username and password. For the demo you can log in with Username: Tuna , Password: password6 or Username: ArnoldSchwarzenegger , Password: password8.

   ![Login](./demonstration/Login.jpg)

5. ### Video Upload

   Includes fields for uploading a video file with complementary date - the video title, description and thumbnail. when they are valid you can press the "Upload" button, or press the "Cancel" button at any time.

   ![upload video](./demonstration/upload_video.jpg)

6. ### Video Edit

   Allows the user to edit the video's title, description and thumbnail or delete the video completely.

   ![edit video](./demonstration/edit_video.jpg)

7. ### Video Player

   This page shows a video player at the top. Below it, there are details about the video, including the uploader's name, upload date and views. Logged in users can interact with the video with the like/unlike buttons and the comments section below which allows them to delete and edit their own comments.
    There also a , and a share button to open a "stupid" share menu.

   ![VideoPlayer](./demonstration/VideoPlayer.jpg)

7. ### Dark Theme

   The app features a dark mode that can be toggled on and off from the navigation menu, independently from the system dark theme. The dark theme changes the color scheme of the app to a darker, more eye-friendly palette.

   ![dark_theme1](./demonstration/dark_theme1.png) 
   ![dark_theme2](./demonstration/dark_theme2.png) 
   ![dark_theme3](./demonstration/dark_theme3.png) 
   ![dark_theme4](./demonstration/dark_theme4.png) 

## Work process -  
Our project was built collaboratively as a team. 
Initially, we met to plan the work together using Jira to divided the tasks among us and keep track of the progress. 

We created mockups for the website and application pages using Figma, and then we started working on the code for both the application and the website. The process was educational, and we thoroughly enjoyed the journey.
the Jira page -
![jira.png](./demonstration/jira.png)

the figma document-
![figma](./demonstration/figma.png)
![jira.png](./demonstration/jira.png)