package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.ui.GitaViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: GitaViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // CameraX configurations
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Preloaded spiritual contemplate simulation templates
    val templates = listOf(
        TemplateItem(
            name = "Burnout Schedule",
            prompt = "An overloaded hourly study/work planner with zero rest.",
            icon = Icons.Filled.Schedule,
            color = Color(0xFFEF4444)
        ),
        TemplateItem(
            name = "Failed Result Sheet",
            prompt = "An academic report with low scores causing guilt and anxiety.",
            icon = Icons.Filled.Feed,
            color = Color(0xFFF59E0B)
        ),
        TemplateItem(
            name = "Road Crossroads",
            prompt = "A literal crossroad on a trail, representing indecisiveness.",
            icon = Icons.Filled.AltRoute,
            color = Color(0xFF3B82F6)
        ),
        TemplateItem(
            name = "Sprout in Dust",
            prompt = "A small plant growing in the desert, representing resilience.",
            icon = Icons.Filled.NaturePeople,
            color = Color(0xFF10B981)
        ),
        TemplateItem(
            name = "Stretched String",
            prompt = "A guitar string being stretched overly tight, representing stress.",
            icon = Icons.Filled.TensionLimit, // Fallback built-in icon
            color = Color(0xFF8B5CF6)
        )
    )

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Smart Gitā Vision",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Analyze live situation & seek divine wisdom",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // View Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.capturedBitmap != null) {
                    // Show captured / selected picture
                    Image(
                        bitmap = viewModel.capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Picture",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Reset button
                    IconButton(
                        onClick = { viewModel.resetCamera() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Discard Image",
                            tint = Color.White
                        )
                    }

                } else if (cameraPermissionState.status.isGranted) {
                    // Start CameraX preview
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder().build()
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                ProcessCameraProvider.getInstance(ctx).get().apply {
                                    unbindAll()
                                    bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Camera binding failed", e)
                            }
                            previewView
                        }
                    )

                    // simulated shutter overlay button in list overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(72.dp)
                                .clickable {
                                    takePhoto(
                                        context = context,
                                        imageCapture = imageCapture,
                                        executor = cameraExecutor
                                    ) { bitmap ->
                                        viewModel.capturedBitmap = bitmap
                                    }
                                }
                                .border(4.dp, Color.White, CircleShape)
                                .shadow(4.dp, CircleShape)
                        ) {}
                    }

                } else {
                    // Permission request UI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Permission Needed",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera access is recommended for Smart Vision",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Controls & Outputs
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (viewModel.capturedBitmap == null) {
                    // Show simulation template scanner
                    Text(
                        text = "Or Contemplate Pre-loaded Spiritual Material Scenarios",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(templates) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = item.color.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, item.color.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable {
                                        // Simulate photo capture by converting a customized vector canvas image to Bitmap immediately!
                                        val mockBitmap = generateVisualSample(context, item.name, item.color)
                                        viewModel.capturedBitmap = mockBitmap
                                        viewModel.cameraPrompt = "I am contemplating this: ${item.prompt}."
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = item.color
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Running camera analysis
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = viewModel.cameraPrompt,
                                onValueChange = { viewModel.cameraPrompt = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("camera_custom_prompt_input"),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                placeholder = { Text("What is your specific question about this?") }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = { viewModel.runCameraAnalysis() },
                                modifier = Modifier.testTag("analyze_camera_button"),
                                enabled = !viewModel.isCameraLoading
                            ) {
                                if (viewModel.isCameraLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Filled.Send, contentDescription = "Scan")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Results Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                if (viewModel.isCameraLoading) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Gītā AI is studying the object & consulting verses...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                                        )
                                    }
                                } else if (viewModel.cameraResultText.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Bhagavad Gita Wisdom Reflection:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = viewModel.cameraResultText,
                                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                                            )
                                        }

                                        IconButton(onClick = { viewModel.speakText(viewModel.cameraResultText) }) {
                                            Icon(
                                                imageVector = Icons.Filled.VolumeUp,
                                                contentDescription = "Play voice audio",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Please enter your query and tap the Send button to let Gita AI analyze this spiritual material.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers for Template List
data class TemplateItem(
    val name: String,
    val prompt: String,
    val icon: ImageVector,
    val color: Color
)

// Helper to capture physical photo
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    onPhotoTaken: (Bitmap) -> Unit
) {
    val imageCapture = imageCapture ?: return
    val photoFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    onPhotoTaken(bitmap)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

// Generates a mock solid colored image with description text for direct offline analysis simulations
private fun generateVisualSample(context: Context, name: String, color: Color): Bitmap {
    val size = 512
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()

    // Draw background
    paint.color = color.hashCode()
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    // Draw overlay circle
    paint.color = Color.White.hashCode()
    paint.alpha = 40
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    return bitmap
}

// Custom Fallback Icon if not in core icons
val Icons.Filled.TensionLimit: ImageVector
    get() = Icons.Filled.Tune
