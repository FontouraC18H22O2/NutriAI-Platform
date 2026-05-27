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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
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
    // Injetamos a nossa ViewModel diretamente no ecrã usando a biblioteca padrão do Compose
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val imageCapture = remember { ImageCapture.Builder().build() }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    // Escutamos o estado da rede vindo da ViewModel
    val networkState = cameraViewModel.uiState

    if (!cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "O NutriAI necessita de acesso à câmara.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) { Text("Dar Permissão") }
            }
        }
    } else if (networkState is CameraUiState.Success) {
        // [MODO RESULTADO DA IA] Mostra o que o servidor devolveu
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Análise Concluída com Sucesso! 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                val mealData = networkState.data
                Text("Refeição Detetada: ${mealData.mealType}", fontWeight = FontWeight.Bold)
                Text("Calorias Totais: ${mealData.totalCalories} kcal")
                Text("Proteínas: ${mealData.protein}g | Carbohidratos: ${mealData.carbs}g | Gorduras: ${mealData.fats}g")
                Text("Alimentos: ${mealData.detectedFoods.joinToString(", ")}")

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    capturedPhotoPath = null
                    cameraViewModel.resetToIdle()
                }) {
                    Text("Tirar Nova Foto")
                }
            }
        }
    } else {
        // [MODO CAPTURA / CONFIRMAÇÃO NORMAL]
        Box(modifier = Modifier.fillMaxSize()) {
            if (capturedPhotoPath != null) {
                // Preview da foto tirada
                Image(
                    painter = rememberAsyncImagePainter(File(capturedPhotoPath!!)),
                    contentDescription = "Foto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Se estiver a carregar o upload, bloqueia o ecrã com um Spinner de progresso
                if (networkState is CameraUiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    // Painel com os botões normais de decisão
                    Column(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.7f)).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Analisar esta refeição?", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

                        // Mostra o erro caso a chamada de rede falhe
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
                                onClick = {
                                    // Dispara o upload real do ficheiro para a API!
                                    cameraViewModel.uploadMealPhoto(capturedPhotoPath!!)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Analisar Prato") }
                        }
                    }
                }
            } else {
                // Visor ativo da câmara nativa
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