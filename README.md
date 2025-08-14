# AcciDetect
Accident Detection System


The Android accident detection app consists of three modules – User, Ambulance, and Hospital Authority – enhanced with TensorFlow Lite (TFLite) AI/ML for real-time accident detection. The AI model runs on-device, analyzing accelerometer and gyroscope sensor data to detect accident patterns with high accuracy, even offline. Once an accident is detected, the system automatically triggers alerts and coordinates emergency response via Firebase.

User Module:
The primary module where users can register, log in, add emergency contacts, and start/stop AI-powered accident detection. The TFLite model continuously monitors motion sensor data in the background to identify potential accidents. Upon detection, the module sends real-time alerts with location details to emergency contacts, ambulance services, and hospital authorities via Firebase Cloud Messaging (FCM).

Ambulance Module:
Receives AI-triggered notifications from the User module containing accident location and confidence score. The ambulance team can be dispatched immediately, and their movements are tracked in real-time. The module updates hospital authorities on the accident status and estimated arrival time using Firebase Realtime Database (RTDB).

Hospital Authority Module:
Receives instant notifications from the Ambulance module about accident details, including severity predictions from the AI model. This module coordinates with medical teams to prepare for incoming patients, ensuring timely medical assistance.

By combining AI-powered accident detection with real-time communication between the three modules, the system ensures faster response times, reduces accident severity, and improves patient outcomes.

Tools and Technology:-
- Android Studio
- Kotlin / Java
- Firebase
- Firebase Cloud Messaging (FCM)
- Google Maps API
- Sqlite Database
- Tflite Model (AI/ ML)

![image](https://github.com/rushikeshty/AcciDetect/assets/117820507/c94e418d-0b72-429f-8909-2b4694c53a44)

![image](https://github.com/rushikeshty/AcciDetect/assets/117820507/4b932d42-342c-4871-b9af-38dd0c510408)

![image](https://github.com/rushikeshty/AcciDetect/assets/117820507/053b08c6-4326-4406-9bdb-d9bb2f2414b3)

![image](https://github.com/rushikeshty/AcciDetect/assets/117820507/53729ffa-03ba-41b5-b995-074e6759211b)

![image](https://github.com/rushikeshty/AcciDetect/assets/117820507/c4a0d8af-a057-438e-a705-0b0e14ff88b4)
