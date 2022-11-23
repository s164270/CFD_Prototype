@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cfd_prototype

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cfd_prototype.ui.theme.CFD_PrototypeTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {

    val inventoryViewModel = InventoryViewModel()

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    lateinit var appDb: AppDatabase


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            Log.i("TEST", "GRANTED")
        } else {
            Log.i("TEST", "DENIED")
        }
    }

    private fun requestCameraPermission() {

        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("TEST", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> Log.i("TEST", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDb = AppDatabase.getDatabase(this)
        inventoryViewModel.appDb = appDb

        setContent {
            CFD_PrototypeTheme {
                NavTest(inventoryViewModel = inventoryViewModel, appDb)
            }
        }
        Log.d("TEST", "TESTHER")
        requestCameraPermission()
    }




    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
fun NavTest(inventoryViewModel: InventoryViewModel, appDatabase: AppDatabase) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destination.Home.route) {
        composable(Destination.Home.route) {
            HomeScreen (
                navToInventory = { navController.navigate(Destination.Inventory.route)},
                navToAddItem = { navController.navigate(Destination.AddItem.route)},
                navToCamera = { navController.navigate(Destination.TakePicture.route)},
                appDatabase)
        }
        composable(Destination.Inventory.route) {
            InventoryScreen(groceries = inventoryViewModel.groceries){ x: Grocery ->
                navController.navigate(Destination.Item.routeToItem(x.id))
            }
        }
        composable(
            Destination.Item.route,
            arguments = listOf(
                navArgument("id"){
                    type = NavType.IntType
                    this.nullable = false
            })
            ) {
            ItemDetailsScreen(inventoryViewModel, it.arguments?.getInt("id") ?: 0)
        }
        composable(Destination.AddItem.route) {
            AddItemScreen() {inventoryViewModel.addItem()}
        }
        composable(Destination.TakePicture.route) {
            //BarcodeTestScreen()
            CameraTestScreen(inventoryViewModel)
        }
    }
}


@Composable
fun HomeScreenItem(painterResourceId: Int, itemText: String, onClick: () -> Unit) {
    Surface(modifier = Modifier
        .padding(20.dp)
        .clickable { onClick() }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(modifier = Modifier.size(100.dp), painter = painterResource(id = painterResourceId), contentDescription = itemText)
            Text(text = itemText)
        }
    }
}

@Composable
fun HomeScreen(navToInventory: () -> Unit,
               navToAddItem: () -> Unit,
               navToCamera: () -> Unit,
               appDatabase: AppDatabase) {

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HomeScreenItem(painterResourceId = R.drawable.ic_inventory, itemText = "View inventory") { navToInventory() }
            HomeScreenItem(painterResourceId = R.drawable.ic_add, itemText = "Add items") { navToAddItem() }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            HomeScreenItem(painterResourceId = R.drawable.ic_camera, itemText = "Barcode test") { navToCamera() }
            HomeScreenItem(painterResourceId = R.drawable.ic_inventory, itemText = "Nothing yet 2") { }
        }

        Button(onClick = {
            val temp1 = BarcodeInfo(null, 5053990139545, "Pringles", GroceryCategory.NoCategory.toString())
            val temp2 = BarcodeInfo(null, 5053990139545, "Pringles", GroceryCategory.NoCategory.toString())

            GlobalScope.launch(Dispatchers.IO) {
                appDatabase.barcodeInfoDao().insert(temp1)
            }
        }) { Text(text = "TEST INSERT") }

        Button(onClick = {
            val testcode = 5700426101216//7
            GlobalScope.launch(Dispatchers.IO) {
                val codeInfo = appDatabase.barcodeInfoDao().findByBarcode(testcode)
                if (codeInfo != null) {
                    Log.d("BARCODETEST1", "Name: " + codeInfo.name)
                } else {
                    Log.d("BARCODETEST1", "Name: " + "NULL")
                }

            }
        }) { Text(text = "TEST READ") }

    }
}

@Composable
fun AddItemScreen(addItem: () -> Unit) {
    Column {
        var id by remember {mutableStateOf("")}
        Text(text = "Add items manually")
        TextField(value = id, onValueChange = {id = it})

        Button(onClick = { addItem() }) {
            Text(text = "Test button")
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraTestScreen(inventoryViewModel: InventoryViewModel) {
    //val shouldShowCamera by remember {mutableStateOf(false)}
    CameraView(inventoryViewModel)
}


@Composable
fun BarcodeTestScreen() {

    val imageURI = Uri.parse("android.resource://com.example.cfd_prototype/" + R.drawable.coke.toString())
    val image = InputImage.fromFilePath(LocalContext.current, imageURI)

    var code by remember{ mutableStateOf("nothing")}
    Column {
        //Image(painter = , contentDescription = )
        //Image(painter = , contentDescription = )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageURI)
                .placeholder(R.drawable.egg3)
                .build(), contentDescription = "", modifier = Modifier.size(350.dp))
        
        Button(onClick = {

            val scanner = BarcodeScanning.getClient()
            val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for(barcode in barcodes) {
                        Log.d("BARCODETEST", "SUCCESS I GUESS")
                        Log.d("BARCODETEST", barcode.rawValue.toString())
                        code = barcode.rawValue.toString()
                    }
                }
                .addOnFailureListener {
                    Log.d("BARCODETEST", "FAILURE I GUESS")
                }
            //code = "asdf"
        }) {
            Text(text = code)
        }

    }

}

@Composable
fun InventoryScreen(groceries: List<Grocery> = List(10) { Grocery(id = it) },
                    viewItemDetails: (x: Grocery) -> Unit) {
    LazyColumn {
        items(items = groceries) {
            Surface(Modifier.clickable { viewItemDetails(it) }) {
                InventoryListItem(it)
            }

        }
    }
}

@Composable
fun InventoryListItem(grocery: Grocery) {
    Row() {
        Text(text = grocery.name, Modifier.weight(1f))
        Text(text = "ID: ${grocery.id}")
        Text(text = "Category: ${grocery.category.name}")
    }
}

@Composable
fun InventoryDetailItem(grocery: Grocery) {
    Column {
        Text(text = grocery.name)
        Text(text = "Category: ${grocery.category.name}")
        Text(text = "Barcode: ${grocery.barcode}")
        Text(text = "Bought: ${grocery.boughtDate}")
        Text(text = "Expiration: ${grocery.expirationDate}")
        //Text(text = "ID: ${grocery.id}")
        //Text(text = grocery.name, Modifier.weight(1f))
    }
}

@Composable
fun ItemDetailsScreen(inventoryViewModel: InventoryViewModel, groceryID: Int) {
    val grocery = inventoryViewModel.getItemWithId(groceryID)
    if (grocery != null) {
        InventoryDetailItem(grocery)
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryPreview() {
    InventoryScreen(){}
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CFD_PrototypeTheme {
        Greeting("Android")
    }
}
