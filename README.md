# EasyDoc

## Overview
EasyDoc is an appointment booking application designed to facilitate the scheduling and management of medical appointments. The app leverages Firebase for user authentication and to maintain a real-time database, ensuring that appointment data is synchronized across all users in real-time.

## Features
- **User Authentication**: Secure login and authentication using Firebase.
- **Real-Time Appointment Management**: Appointments are managed and updated in real-time using Firebase's real-time database.
- **Calendar Integration**: All appointments can be added directly to your personal calendar for easy tracking.
- **Recurring Appointments**: Ability to schedule recurring appointments, such as weekly sessions over a specified period.
- **Waitlist Queue**: When a day is fully booked, users have the option to join a waitlist queue.
- **Doctor Account**: Special account type for doctor, allowing the management of office details and appointment schedules.
- **Map Navigation**: Users can navigate to the clinic using google maps.
- **Phone Calling**: Direct calling feature enables users to call the doctor's office directly from the app interface.
- **Disable Fully Booked Days**: Days that are fully booked are automatically disabled, preventing further bookings and improving user experience.
- **Collision detection**: The app includes a time collision detection feature to prevent the scheduling of overlapping appointments, ensuring efficient time management.
## Tech Stack
- **Java language**
- **Firebase Authentication + UI**
- **RealTime Database from Firebase**
- **3rd party DatePicker and TimePicker dialogs from `wdullaer` :https://github.com/wdullaer/MaterialDateTimePicker**
- **Using ViewModel and LiveData management**
- **Google maps API**
- **Repository and Singletone design pattern**
- **Bottom Navigation View**
## Future improvements
- **Add notifications for adding or changing appointments**
- **Fixing issues from the 3rd party Date and Time pickers integrate better with my application**
- **Changing address for the medical center**
- **Add more doctors**

## Setup

## Setup
1. **Clone the repository**:
   ```bash
   git clone https://example.com/EasyDoc.git
   cd EasyDoc
   ```
2. **Install Dependencies**:   
    Ensure that Gradle is installed and your environment is set up for Android development. Run:
  ```bash
   ./gradlew build
  ```
3. **Configure Firebase**:
   - Set up a Firebase project and download the `google-services.json` file.
   - Place `google-services.json` into the `app/` directory.
   - Ensure that Firebase Authentication and Firebase Real-time Database services are enabled in your Firebase project settings.
4. **Run the Application**:
   Run the application on an Android device or emulator.

## Configuration
**Firebase Configuration**:
- `Secrets.properties`: Ensure this file contains your Firebase API keys and other secrets.
- `Local.defaults.properties`: Set up default configurations here.

  ## Application Flow
 ### 1. Login and Signup screen of EasyDoc
  <div class="row">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/24e3ffc5-6551-4f34-87a1-4bde0b5cd6c5" style="height:700px;" alt="Login screen of EasyDoc">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/b0b4fb37-52c2-45e7-8196-56b4209d7f58" style="height:700px;" alt="Signup screen of EasyDoc">
  </div>
  
  ### 2. Home, Dashboard, Appointments screens of EasyDoc
  <div class="row">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/31277a79-d671-44dd-818b-bc01ee7da5c5" style="height:500px;" alt="Home screen of EasyDoc">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/baa2f8fd-9205-4138-ae88-d022ceef0d22" style="height:500px;" alt="Dashboard screen of EasyDoc">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/dbb1f5ae-dcd8-4f64-8ead-a24b8c9b6f50" style="height:500px;" alt="Make Appointments screen of EasyDoc">
  </div>

  ### 3. Making an appointment
  **1. First select Date and time**
  <div class="row">
        <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/217d92da-4b6b-4c25-b6be-3a4208f4308d" style="height:500px;" alt="Select Date">
        <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/b9a7d703-fa88-4528-8abb-cf194e1676a2" style="height:500px;" alt="Select Time">
        <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/ec78f09f-f161-49f5-994d-e69f8839fc03" style="height:500px;" alt="Appointment screen of EasyDoc">
  </div>

   As you can see there are dates and times that are disabled. The app disable all weekends m fully booked days, and times there are allready has appointments.

   **2. add text**
   
   <div>
        <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/3b9b145c-de55-4e0e-8684-2f34ae869f8f" style="height:700px;" alt="Add text">
   </div>

  ### 4. Check dashboard
   
  <div>
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/baa2f8fd-9205-4138-ae88-d022ceef0d22" style="height:700px;" alt="Dashboard screen of EasyDoc">
   </div>
   
  ## Also if there is a date you want to make an appointment but its fully booked, you can sign in to the waiting list.

  <div>
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/484f624c-d35e-4269-a2ec-20bc6107edcc" style="height:700px;" alt="Add to waitList">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/262334a9-1cdd-46b5-aefd-e5cc8c2091b7" style="height:700px;" alt="Check waitList">

   </div>
   
 ## If the account is a doctor he can check all the appointments of all the users and also manage the office settings:
 

  <div>
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/392d4731-80ab-4abd-a8f7-8a41c4e927a1" style="height:500px;" alt="Office settings">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/ac1ce8c5-7679-4c69-b1b0-4dc6c98f5c96" style="height:500px;" alt="Dashboard">
    <img src="https://github.com/GalRadia/EasyDoc/assets/105793494/d63c5e8c-7d17-4fc8-b9f7-afd641f4f650" style="height:500px;" alt="Home">
   </div>


   
