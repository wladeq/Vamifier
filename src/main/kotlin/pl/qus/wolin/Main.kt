package pl.qus.wolin


import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


object Main {

    @JvmStatic
    fun main(argv: Array<String>) {
        val name = "DiscoveryFiltersModule"

        val pack = ""

        //val currentFile = event.getData(DataKeys.VIRTUAL_FILE) // skorzystać z tego!

        val rootDir = File("F:\\Android\\Android_projects\\wsFPF\\app\\src\\main")

        run(name, pack, rootDir)

        //coroutinePlayground()

        //startScanning()
    }

}

//fun startScanning() {
//    val api = prepareApi()
//
//    scanDir(File("Z:\\obrazy"), api)
//}
//
//fun prepareApi() : ConvertImageApi {
//    val defaultClient: ApiClient = Configuration.getDefaultApiClient()
//    val apikey = defaultClient.getAuthentication("Apikey") as ApiKeyAuth;
//    apikey.apiKey = "1e740922-3de5-4e45-be5d-9acb1cda9a62";
//    return ConvertImageApi()
//}
//
//fun scanDir(dir: File, api: ConvertImageApi) {
//    println("Scanning dir: $dir")
//    dir.listFiles()?.forEach {
//        if(it.isDirectory)
//            scanDir(it, api)
//        else if(it.name.startsWith("RDT_")){
//            try {
//                val istream = it.inputStream()
//                val signature = ByteArray(4)
//                val read = istream.read(signature, 0, 4)
//                if (signature[0] == 'R'.code.toByte() && signature[1] == 'I'.code.toByte() && signature[2] == 'F'.code.toByte() && signature[3] == 'F'.code.toByte()) {
//                    //converter(it, api)
//                    renamer(it)
//                }
//            } catch (ex: Exception) {
//                println("Error converting $it: ${ex.message}")
//            }
//        }
//    }
//}
//
//fun converter(inputFile: File, apiInstance: ConvertImageApi) {
//    try {
//        val result = apiInstance.convertImageImageFormatConvert("HEIC", "JPG", inputFile);
//        val outputFile = File(inputFile.path.dropLast(inputFile.extension.length)+"jpg")
//        outputFile.writeBytes(result)
//        print("Converted $inputFile. ")
//        Thread.sleep(1100);
//        println("Saved.")
//    } catch (e: ApiException) {
//        println("Error converting $inputFile: ${e.message}")
//    }
//}
//
//
//fun renamer(inputFile: File) {
//    try {
////        val result = apiInstance.convertImageImageFormatConvert("HEIC", "JPG", inputFile);
//        val outputFile = File(inputFile.path.dropLast(inputFile.extension.length)+"jpg")
//        println("Renamed $inputFile")
//        inputFile.renameTo(outputFile)
//    } catch (e: ApiException) {
//        println("Error converting $inputFile: ${e.message}")
//    }
//}

fun coroutinePlayground() {
    runBlocking {
//        coroutineScope {
//            otherJob(this)
//        }

        println("Start")

        otherJobBetter()

//        timeout()
//        val t = measureTimeMillis {
//            dwaNaRaz()
//        }
//        println("Wykonalo sie w $t")

        println("I koniec")
    }
}

suspend fun otherJob(scope: CoroutineScope) {
    // wykonują się współbieżnie
    scope.launch {
        delay(1100L)
        println("Bede drugi!")
    }
    scope.launch {
        delay(1000L)
        println("Jestem pierwszy!")
    }
    println("Odpalam ich!")
}

suspend fun otherJobBetter() = coroutineScope {
    // wykonują się współbieżnie
    launch {
        delay(1100L)
        println("Bede czwarty!")
    }
    launch {
        delay(1000L)
        println("Jestem trzeci!")
    }
    println("Odpalam ich!")
}

suspend fun waiter() = coroutineScope {
    // wykonują się współbieżnie
    val job = launch {
        delay(1100L)
        println("Bede czwarty!")
    }
    launch {
        delay(1000L)
        println("Jestem trzeci!")
    }
    println("Odpalam ich!")
    job.join()
    println("I się skończyło")
}

suspend fun cancellation() = coroutineScope {
    val job = launch(Dispatchers.Default) {
        try {
            while (true /*isActive*/) {
                ensureActive()
                println("czekam...")
                delay(1000L)
            }
        }
        finally {
            println("skanselowany lub zakonczony")
            // tu nie można odpalac kondu suspend, chyba że w withContext(NonCancellable) {...}
        }
    }

    println("Poczekam 5 sekund i skanceluje")
    delay(5000L)
    job.cancelAndJoin()
    println("Skanselowana")
}

suspend fun timeout() = coroutineScope {
    val timeout = 1000L

    val result = withTimeoutOrNull(timeout) {
        try {
            delay(5000L)
            "dupa"
        }
        finally {
            if(isActive)
                println("Obliczenie zakonczone")
            else
                println("Ubili mnie!")
        }
    }

    // wykona się po $timeout
    println("Result: $result")
}

suspend fun dwaNaRaz() = coroutineScope {
    // mogą być lazy - startują na żądanie
    val faceci = async(Dispatchers.IO) {
        println("czekam w ${Thread.currentThread().name}")
        delay(1000)
        15
    }

    val babki = async(Dispatchers.IO) {
        println("czekam w ${Thread.currentThread().name}")
        delay(500)
        50
    }

    println("Razem:${faceci.await()+babki.await()}, watek:${Thread.currentThread().name}");
}


val job = SupervisorJob()
val ScopeIO = CoroutineScope(job + Dispatchers.IO)

// === niezalecane w kotlinie =======================================
fun longJobAsync() = ScopeIO.async {
    delay(5000L)
}
fun nonSuspendingFunctionTest() {
    runBlocking {
        val result = longJobAsync().await()
        println("Wynik:$result")
    }
}
// zamiast powyższego używać buildera coroutineScope

// sposób google na komunikację main/io
suspend fun t() = withContext(Dispatchers.Main) {
    launch {
        val result = networkCall()
    }
}
fun cośtam(uiscope: CoroutineScope) {
    uiscope.launch {
        val result = networkCall()

        val innyResult = withContext(Dispatchers.IO) {
            true
        }

        if(result) {
            println("udało się!")
        }
    }
}

suspend fun networkCall(): Boolean = withContext(Dispatchers.IO) {
    // operacja na sieci
    true
}

fun testScope() {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->  })
}

// wszystkie coroutiny async wystartowane wewnątrz funkcji suspend muszą się zakończyć zanim funkcja wróci
// więc należy na nich przed końcem wywołać await

// Scope:
// skołp pozwala zarzadzać cyklem życia, zawieta domyślny dispatcher, ale możemy go zmienić
// skołp można utworzyć
// - z ręki:              val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->  }), CoroutineScope(job + Executors.newFixedThreadPool(4).asCoroutineDispatcher())
// - poprzez builder:     coroutineScope {}, supervisorScope {} (wyjątki z niego nie propagują w górę), withContext {} (zwraca wartość!)
// - są zrobione dla nas: vievModelScope, lifecycleScope
// w obrębie scope możemy zrobić
// - launch(nowy dispatcher lub parent job)
// - async(nowy dispatcher lub parent job)
// supervisorScope { this  == CoroutineScope(SupervisorJob())
//
// coroutineScope {} ≡ withContext(this.coroutineContext) {}
// Since switching contexts is just one of several features of withContext, this is a legitimate use case.
// withContext waits for all the coroutines you start within the block to complete. If any of them fail,
// it will automatically cancel all the other coroutines and the whole block will throw an exception,
// but won't automatically cancel the coroutine you're calling it from.
// https://stackoverflow.com/questions/56858624/kotlin-coroutines-what-is-the-difference-between-coroutinescope-and-withcontext
//
// Dispatcher:
// Default - CPU intensive, pula wątków
// IO - dla długich zapytań, pula wątków
//
// Funckcje suspend mają być main-safe, czyli jeżeli robią coś długiego i blokującego, to powinny używać withContext
// aby przejść do innego wątku
//
// SupervisorJob - dzieci mogą niezależnie failować


// Set the menu item name.
// Set the menu item name, description and icon.
// super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));

fun run(name : String, pack : String, rootDir : File) {
    val popPack = if(pack=="")
        "pl.qus.ukbank.mvi.${name.toLowerCase()}"
    else
        "pl.qus.ukbank.mvi.${pack.toLowerCase()}"

    tworzacz(popPack,name,rootDir)

}

fun tworzacz(pack : String, name : String, rootDir : File) {
    val newDir = File(rootDir.path+"\\java\\"+pack.replace(".","\\"))

    if(!newDir.exists())
        newDir.mkdirs()

    oktalizer(File(rootDir,"\\java\\${pack.replace(".","\\")}\\${name}.kt"), createFragment(pack,name))
    oktalizer(File(rootDir,"\\java\\${pack.replace(".","\\")}\\${name}Interactor.kt"), createInteractor(pack,name))
    oktalizer(File(rootDir,"\\java\\${pack.replace(".", "\\")}\\${name}ViewModel.kt"), createViewModel(pack,name))
    oktalizer(File(rootDir,"\\java\\${pack.replace(".", "\\")}\\${name}UIInterface.kt"), createUIInterface(pack,name))
    oktalizer(File(rootDir,"\\res\\layout\\${name.toLowerCase()}.xml"), createLayout(pack,name))
}

fun oktalizer(fileName : File, contents : String) {

    val writer = BufferedWriter(FileWriter(fileName))

    writer.write(contents)

    writer.close()
}

fun createFragment(PACKAGE_NAME : String, NAME : String) : String {

    val layoutName = NAME.toLowerCase()
    val bindingName = layoutName.substring(0,1).toUpperCase() + layoutName.substring(1)

    return """package $PACKAGE_NAME

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.qus.mvi.MVIFragment
import pl.qus.livecycle.R
import pl.qus.livecycle.databinding.${bindingName}Binding
import pl.qus.ukbank.data.TraceableRealm
import pl.qus.ukbank.flow.DTOViewModel

class ${NAME} :
        MVIFragment<${NAME}Interactor, ${NAME}UIInterface>(),
        ${NAME}UIInterface {

    override val moduleDTO: ${NAME}ViewModel by DTOViewModel()
    override val interactorClass = ${NAME}Interactor::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<${bindingName}Binding>(
                inflater, R.layout.$layoutName, container, false)
        binding.data = moduleDTO

        return binding.root
    }

	override fun onResume() {
        super.onResume()

        //(activity as MainActivity).statusToLeadColor()
		//interactor.bindDataViews
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

		// setup views here
    }

    override fun setupMode() {
        when (moduleDTO.mode) {
        }
    }
}"""
}

fun createInteractor(PACKAGE_NAME : String, NAME : String)  : String{
    return """package $PACKAGE_NAME

import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import pl.qus.mvi.MVIInteractor
import pl.qus.ukbank.api.ApiInterface
import pl.qus.ukbank.data.DataManager

class ${NAME}Interactor(kodein : Kodein) : MVIInteractor<${NAME}UIInterface>(kodein) {
    override var ui: ${NAME}UIInterface? = null
}"""
}

fun createViewModel(PACKAGE_NAME : String, NAME : String) : String {
    return """package $PACKAGE_NAME

import pl.qus.ukbank.flow.DTO

class ${NAME}ViewModel : DTO() {

     override fun clear() {

    }
 
}"""
}

fun createUIInterface(PACKAGE_NAME : String, NAME : String) : String {
    return """package $PACKAGE_NAME

import pl.qus.mvi.MVIUIInterface

interface ${NAME}UIInterface : MVIUIInterface {

}"""
}

fun createLayout(PACKAGE_NAME : String, NAME : String) : String {
    return """<?xml version="1.0" encoding="utf-8"?>
<!-- Nazwa pliku: $NAME.xml -->
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>

        <variable
            name="data"
            type="$PACKAGE_NAME.${NAME}ViewModel" />
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.google.android.material.appbar.AppBarLayout
            android:id="@+id/appbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="?leadColor"
            android:theme="@style/AppTheme.AppBarOverlay"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent">

            <androidx.appcompat.widget.Toolbar
                android:id="@+id/paymentToolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:elevation="2dp"
                app:layout_scrollFlags="scroll|enterAlways"
                app:navigationIcon="@drawable/abc_ic_ab_back_material"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
                app:title="Payment" />

        </com.google.android.material.appbar.AppBarLayout>


        <androidx.constraintlayout.widget.Guideline
            android:id="@+id/guideline"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:layout_constraintGuide_percent="@dimen/left_guideline" />

        <androidx.constraintlayout.widget.Guideline
            android:id="@+id/guideline2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:layout_constraintGuide_percent="@dimen/right_guideline" />

        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            app:layout_constraintBottom_toTopOf="@+id/bottom_navigation1"
            app:layout_constraintTop_toBottomOf="@+id/appbar">

        </ScrollView>

        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottom_navigation1"
            android:layout_width="match_parent"
            android:layout_height="@dimen/bottom_nav_bar_height"
            android:layout_alignParentBottom="true"
            app:itemBackground="@android:color/white"
            app:itemTextColor="?colorPrimary"
            app:layout_constraintBottom_toBottomOf="parent"
            app:menu="@menu/bottom_nav" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</layout>"""
}