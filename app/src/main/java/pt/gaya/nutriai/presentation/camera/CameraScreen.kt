package pt.gaya.nutriai.presentation.camera

import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val imageCapture = remember { ImageCapture.Builder().build() }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    val networkState = cameraViewModel.uiState

    if (!cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "O NutriAI necessita de acesso à câmara.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFECEFF1), modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) { Text("Dar Permissão") }
            }
        }
    } else if (networkState is CameraUiState.Success) {
        // [MODO RESULTADO DA IA] - DESIGN PREMIUM REMODELADO
        val mealData = networkState.data

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)) // Fundo Preto Total Premium
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Análise Concluída com Sucesso! 🎉",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFECEFF1),
                modifier = Modifier.padding(top = 16.dp)
            )

            // CARD PRINCIPAL: Tipo de Refeição e Calorias
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mealData.mealType,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFB0BEC5),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${mealData.totalCalories}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF64B5F6) // Azul Néon focado no valor energético
                    )
                    Text(
                        text = "Calorias Totais (kcal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78909C)
                    )
                }
            }

            // LINHA DE MACRONUTRIENTES: 3 Blocos Simétricos lado a lado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroNutrientBadge(label = "Proteínas", value = "${mealData.protein}g", color = Color(0xFF81C784), modifier = Modifier.weight(1f))
                MacroNutrientBadge(label = "Carbos", value = "${mealData.carbs}g", color = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
                MacroNutrientBadge(label = "Gorduras", value = "${mealData.fats}g", color = Color(0xFFE57373), modifier = Modifier.weight(1f))
            }

            // ALIMENTOS DETETADOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Alimentos Detetados pela IA:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFECEFF1)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    mealData.detectedFoods.forEach { food ->
                        Text(
                            text = "• $food",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCFD8DC),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTÃO RECOMEÇAR
            Button(
                onClick = {
                    capturedPhotoPath = null
                    cameraViewModel.resetToIdle()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Tirar Nova Foto", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // [MODO CAPTURA / CONFIRMAÇÃO NORMAL]
        Box(modifier = Modifier.fillMaxSize()) {
            if (capturedPhotoPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(File(capturedPhotoPath!!)),
                    contentDescription = "Foto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (networkState is CameraUiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF64B5F6))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.75f)).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = "Analisar esta refeição?", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

                        if (networkState is CameraUiState.Error) {
                            Text(text = networkState.message, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { capturedPhotoPath = null; cameraViewModel.resetToIdle() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) { Text("Repetir") }

                            Button(
                                onClick = { cameraViewModel.uploadMealPhoto(capturedPhotoPath!!) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) { Text("Analisar Prato") }
                        }
                    }
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                Box(modifier = Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
                    Button(
                        onClick = { takePhoto(context = context, imageCapture = imageCapture) { path -> capturedPhotoPath = path } },
                        shape = CircleShape, modifier = Modifier.size(80.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {}
                }
            }
        }
    }
}

// COMPONENTE AUXILIAR DOS MACRONUTRIENTES
@Composable
fun RowScope.MacroNutrientBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF78909C))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, onPhotoSaved: (String) -> Unit) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val photoFile = File(context.cacheDir, "$name.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Erro: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhotoSaved(photoFile.absolutePath)
            }
        }
    )
}