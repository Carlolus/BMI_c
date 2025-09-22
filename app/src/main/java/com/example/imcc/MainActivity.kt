package com.example.imcc

import UserPreferences
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.imcc.data.AppDatabase
import com.example.imcc.data.BmiDao
import com.example.imcc.data.BmiHistory
import com.example.imcc.data.User
import com.example.imcc.ui.history.HistoryScreen
import com.example.imcc.ui.history.HistoryViewModel
import com.example.imcc.ui.history.ViewModelFactory
import com.example.imcc.ui.theme.IMCcTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow


enum class Screen {
    Splash,
    NameInput,
    Calculator,
    History
}

class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var appDatabase: AppDatabase
    private lateinit var googleAuthClient: GoogleAuthClient
    private val currentUserFlow = MutableStateFlow<com.example.imcc.data.User?>(null)
    private val historyViewModel: HistoryViewModel by viewModels {
        ViewModelFactory(AppDatabase.getDatabase(applicationContext).bmiDao(), "")
    }

    // Flow para notificar el nombre del usuario y el cambio de pantalla
    private val userNameFlow = MutableStateFlow<String?>(null)

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        lifecycleScope.launch {
            val success = googleAuthClient.handleSignInResult(result.data)
            if (success) {
                println("Sign-in successful")
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                firebaseUser?.let { user ->
                    val uid = user.uid
                    val name = user.displayName ?: "Usuario"
                    val email = user.email
                    
                    val userEntity = com.example.imcc.data.User(
                        uid = uid,
                        name = name,
                        email = email
                    )
                    
                    appDatabase.bmiDao().saveUser(userEntity)
                    currentUserFlow.value = userEntity
                    userNameFlow.value = name // Notifica el nombre para cambiar la pantalla
                }
            } else {
                println("Sign-in failed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(applicationContext)
        appDatabase = AppDatabase.getDatabase(applicationContext)
        googleAuthClient = GoogleAuthClient(this)

        setContent {
            IMCcTheme {
                BMIApp(
                    userPreferences = userPreferences,
                    bmiDao = appDatabase.bmiDao(),
                    historyViewModel = historyViewModel,
                    googleAuthClient = googleAuthClient,
                    onSignIn = { intent ->
                        signInLauncher.launch(intent)
                    },
                    userNameFlowFromActivity = userNameFlow, // Pasa el flow
                    currentUserFlow = currentUserFlow
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMIApp(
    userPreferences: UserPreferences,
    bmiDao: BmiDao,
    historyViewModel: HistoryViewModel,
    googleAuthClient: GoogleAuthClient,
    onSignIn: (Intent) -> Unit,
    userNameFlowFromActivity: MutableStateFlow<String?>, // Nuevo parámetro
    currentUserFlow: MutableStateFlow<com.example.imcc.data.User?>
) {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var userName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(2000)
        // Verificar si hay un usuario autenticado en Firebase
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            // Si hay usuario en Firebase, verificar si está en la base de datos local
            val existingUser = bmiDao.getUser(firebaseUser.uid).first()
            if (existingUser != null) {
                currentUserFlow.value = existingUser
                userName = existingUser.name
                currentScreen = Screen.Calculator
            } else {
                // Si no está en la BD local, crearlo
                val userEntity = com.example.imcc.data.User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Usuario",
                    email = firebaseUser.email
                )
                bmiDao.saveUser(userEntity)
                currentUserFlow.value = userEntity
                userName = userEntity.name
                currentScreen = Screen.Calculator
            }
        } else {
            currentScreen = Screen.NameInput
        }
    }

    LaunchedEffect(userPreferences.userNameFlow) {
        userPreferences.userNameFlow.collect { name ->
            if (userName != name) {
                userName = name
                if (name != null && currentScreen == Screen.NameInput) {
                    currentScreen = Screen.Calculator
                }
            }
        }
    }

    // Escucha el nombre del usuario desde el inicio de sesión con Google
    LaunchedEffect(userNameFlowFromActivity) {
        userNameFlowFromActivity.collect { name ->
            if (name != null && currentScreen == Screen.NameInput) {
                userName = name
                currentScreen = Screen.Calculator
            }
        }
    }

    // Escucha cambios en el usuario actual
    LaunchedEffect(currentUserFlow) {
        currentUserFlow.collect { user ->
            if (user == null && currentScreen != Screen.NameInput) {
                currentScreen = Screen.NameInput
                userName = null
            }
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.Splash -> SplashScreenContent()
                Screen.NameInput -> NameScreen(
                    onNameSaved = { name ->
                        scope.launch {
                            userPreferences.saveUserName(name)
                            // El usuario se guarda automáticamente en el signInLauncher
                        }
                    },
                    googleAuthClient = googleAuthClient,
                    onSignIn = onSignIn
                )
                Screen.Calculator -> {
                    if (userName == null) {
                        currentScreen = Screen.NameInput
                    } else {
                        IMCCalculator(
                            name = userName!!,
                            bmiDao = bmiDao,
                            onNavigateToHistory = { currentScreen = Screen.History },
                            googleAuthClient = googleAuthClient,
                            onSignIn = onSignIn,
                            currentUserFlow = currentUserFlow
                        )
                    }
                }
                Screen.History -> {
                    val currentUser by currentUserFlow.collectAsState()
                    currentUser?.let { user ->
                        val historyViewModelForUser = HistoryViewModel(
                            bmiDao = bmiDao,
                            userId = user.uid
                        )
                        HistoryScreen(
                            viewModel = historyViewModelForUser,
                            onNavigateBack = { currentScreen = Screen.Calculator }
                        )
                    } ?: run {
                        // Si no hay usuario, volver a la pantalla principal
                        currentScreen = Screen.Calculator
                    }
                }
            }
        }
    }
}


@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E21)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.splash_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}


@Composable
fun IMCCalculator(
    name: String,
    bmiDao: BmiDao,
    onNavigateToHistory: () -> Unit,
    googleAuthClient: GoogleAuthClient,
    onSignIn: (Intent) -> Unit, // Callback para lanzar el Intent
    currentUserFlow: MutableStateFlow<com.example.imcc.data.User?>
) {
    val context = LocalContext.current
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSignedIn by rememberSaveable {
        mutableStateOf(googleAuthClient.isSignedIn())
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de Sign Out
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            googleAuthClient.signOut()
                            currentUserFlow.value = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.button_sign_out), fontSize = 12.sp)
                }
                
                // Botón de Historial
                IconButton(onClick = onNavigateToHistory) {
                    Icon(Icons.Filled.List, contentDescription = stringResource(R.string.history_title))
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = stringResource(R.string.greeting, name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.enter_height_weight),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.bmi_is), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = result,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = status, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text(stringResource(R.string.label_height)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(stringResource(R.string.label_weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val h = height.toDoubleOrNull()
                    val w = weight.toDoubleOrNull()
                    if (h != null && w != null && h > 0) {
                        val imc = w / ((h / 100) * (h / 100))
                        result = "%.2f".format(imc)
                        status = when {
                            imc < 18.5 -> context.getString(R.string.underweight)
                            imc < 24.9 -> context.getString(R.string.normal_weight)
                            imc < 29.9 -> context.getString(R.string.overweight)
                            else -> context.getString(R.string.obesity)
                        }
                        scope.launch {
                            val currentUser = currentUserFlow.value
                            if (currentUser != null) {
                                val bmiHistoryEntry = BmiHistory(
                                    userId = currentUser.uid,
                                    result = imc,
                                    weight = w,
                                    height = h / 100
                                )
                                bmiDao.insertHistory(bmiHistoryEntry)
                            }
                        }
                    } else {
                        result = ""
                        status = context.getString(R.string.invalid_input)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(context.getString(R.string.button_calculate), style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

