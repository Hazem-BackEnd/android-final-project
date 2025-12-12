package com.chat.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chat.app.data.repository.ChatRepository
import kotlin.jvm.java

class HomeViewModelFactory (
    private val repository: ChatRepository,
    private val currentUserId: String = "current_user" // 🔥 NEW: Pass current user ID
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(HomeScreenViewModel::class.java)){
            return HomeScreenViewModel(repository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}

/*
https://developer.android.com/training/dependency-injection/manual
https://developer.android.com/training/dependency-injection


بص يعم ركز معايا , دلوقتي احنا عندنا مشكلة
اي هي؟
 ان فيه كلاسات كتير معتمدة علي بعض
HomeScreen -> HomeScreenViewModel -> ChatRepository -> ChatDao and ChatEntity and....
تقولي اتفرج علي الجمال عندنا الدنيا متنظمة ازاي
هقولك تخيل لو حصل تعديل هتعمل اي؟ هيبقي جنان طبعاً
طيب والعمل
قالك حاجه اسمها manual dependency injection
انا كحازم كنت اسمع عنها اكيد ولكن مطبقتهاش
وعشان مخدتش سوفتويير ف مش متخيلها
ولكن عرفتها انهرده يوم 12 شهر 12 2025
واحنا هنعمل حوار الفاكتوري
احنا هدفنا ميكونش فيه كده
    userRepository is not private; it'll be exposed
    val userRepository = UserRepository(localDataSource, remoteDataSource)

        Gets LoginViewModelFactory from the application instance of AppContainer
        to create a new LoginViewModel instance
        val appContainer = (application as MyApplication).appContainer
        loginViewModel = appContainer.loginViewModelFactory.create()

السيناريو:

اليوزر دخل ChatDetailsScreen
وبعدين رجع HomeScreen
→ لازم تبني HomeScreenViewModel تاني
→ بس الـ Repository موجود بالفعل ومش عايز تبنيه كل مرة.

هنا ييجي دور الـ DI

لما اليوزر يدخل ChatDetails
وبعدين يرجع HomeScreen
انت محتاج ViewModel جديد
بس مش محتاج تعمل Repository جديد
لأن Repository مربوط بالـ Database
ومفروض يفضل موجود طول عمر التطبيق
وإلا الأبلكيشن هيبوظ في الأداء.

فبنقول:

Factory يبني ViewModels

AppContainer يبني Repositories

Application يدي AppContainer لأي Screen

 */