# mpesasummary

## Instructions for cloud upload
* Go to the firebase console and create a project
* Disable analytics for the project
* On the project screen click add firebase to your app and select the Android icon
* For the Android package name use com.example.android.mpesasummary
* Download the provided google-services.json file and add to the app directory of the application
* Uncomment the following lines in MainActivity.java:
  * //FirebaseFirestore db = FirebaseFirestore.getInstance();
  * //upload(db,currentMonth,"currentMonth");
* Run the application while connected to your device
