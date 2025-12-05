# Android Final Project
### GUY SENSI HAS OPENED THE SEVENTH GATEE!!
Description:
This project is a simplified clone of WhatsApp, developed for an Android development course. It's built using Kotlin and Jetpack Compose, showcasing modern Android development practices. The app aims to provide core real-time messaging functionalities in a clean and intuitive user interface.

Folder Structure:
```
com.app.whatever/
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── UserDao.kt
│   │   │   ├── ChatDao.kt
│   │   │   └── MessageDao.kt
│   │   └── entities/
│   │       ├── UserEntity.kt
│   │       ├── ChatEntity.kt
│   │       └── MessageEntity.kt
│   │
│   ├── remote/
│   │   ├── firebase/
│   │   │   ├── FirebaseAuthManager.kt
│   │   │   ├── FirebaseChatService.kt
│   │   │   └── FirebaseUserService.kt
│   │   ├── api/
│   │   │   ├── ApiService.kt
│   │   │   └── ApiModels.kt
│   │
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── UserRepository.kt
│   │   ├── ChatRepository.kt
│   │   ├── MessageRepository.kt
│   │   ├── ContactsRepository.kt
│   │   └── ApiRepository.kt
│
├── domain/
│   ├── models/
│   ├── mappers/
│   └── usecases/        // optional
│
├── ui/
│   ├── login/
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   ├── register/
│   ├── home/
│   ├── contacts/
│   ├── chatdetails/
│   ├── settings/
│   └── components/
│
├── utils/
│   ├── validators/
│   └── extensions/
│
└── App.kt
```