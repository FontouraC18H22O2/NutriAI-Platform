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
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val imageCapture = remember { ImageCapture.Builder().build() }

    // ESTADO MVI LOCAL: Guarda o caminho da foto se ela já tiver sido tirada
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    if (!cameraPermissionState.status.isGranted) {
        // Ecrã de pedido de permissão (Mantém-se igual)
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "O NutriAI necessita de acesso à câmara.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) { Text("Dar Permissão") }
            }
        }
    } else if (capturedPhotoPath != null) {
        // [MODO CONFIRMAÇÃO] Se já temos uma foto, mostra a preview estática e botões de ação
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // Usamos a biblioteca Coil (que já está no Compose) para ler o ficheiro físico .jpg
            Image(
                painter = rememberAsyncImagePainter(File(capturedPhotoPath!!)),
                contentDescription = "Foto capturada do prato",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Painel Inferior de Decisão
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.7f)).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Analisar esta refeição?", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Botão de Cancelar / Repetir Foto
                    OutlinedButton(
                        onClick = { capturedPhotoPath = null }, // Limpa o estado e volta para a câmara
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Repetir")
                    }

                    // Botão de Confirmar e Enviar para a IA
                    Button(
                        onClick = {
                            Toast.makeText(context, "A enviar para o motor IA do NutriAI...", Toast.LENGTH_LONG).show()
                            // Aqui faremos a ligação ao Retrofit / Ktor para disparar o upload do MultipartBody
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Analisar Prato")
                    }
                }
            }
        }
    } else {
        // [MODO CAPTURA] O visor da câmara normal ativo
        Box(modifier = Modifier.fillMaxSize()) {
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
                    onClick = {
                        // Passamos uma callback expression (caminho ->) para capturar o resultado aqui no ecrã
                        takePhoto(context = context, imageCapture = imageCapture) { path ->
                            capturedPhotoPath = path
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {}
            }
        }
    }
}

// Função de captura atualizada para retornar o caminho do ficheiro guardado
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
                println("LOG_NUTRIAI: Foto guardada em ${photoFile.absolutePath}")
                onPhotoSaved(photoFile.absolutePath) // Dispara o gatilho para atualizar a UI do Compose
            }
        }
    )
}