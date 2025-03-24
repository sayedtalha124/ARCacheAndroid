package app.androidarcache.arCore

import android.Manifest.permission.CAMERA
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.createBitmap
import app.androidarcache.R
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingFailureReason.BAD_STATE
import com.google.ar.core.TrackingFailureReason.CAMERA_UNAVAILABLE
import com.google.ar.core.TrackingFailureReason.EXCESSIVE_MOTION
import com.google.ar.core.TrackingFailureReason.INSUFFICIENT_FEATURES
import com.google.ar.core.TrackingFailureReason.INSUFFICIENT_LIGHT
import com.google.ar.core.TrackingFailureReason.NONE
import com.google.ar.core.TrackingState
import com.google.ar.core.TrackingState.PAUSED
import com.google.ar.core.TrackingState.STOPPED
import com.google.ar.core.TrackingState.TRACKING
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import kotlin.math.sqrt


class ARCore(
    private var arSceneView: ArSceneView,
    private var ctx: Context,
    private var onARChangeListener: OnARChangeListener,
    private var nodeView: View,
    private var fileName: String
) {
    var nodes: Nodes? = null
    private fun defaultPose(ar: ArSceneView): Pose {
        var distanceFromCamera = 2f
        // Get screen position of the view
        val viewLocation = IntArray(2)
        nodeView.getLocationOnScreen(viewLocation)
// Calculate coordinates above the view
        var centerX = viewLocation[0] + nodeView.width / 2f
        var centerY = viewLocation[1] - 110f // Move 50 pixels above the view

        // Fallback: Place model at a fixed distance in front of the camera
        val ray = ar.scene.camera.screenPointToRay(centerX, centerY)
        val point = ray.getPoint(distanceFromCamera)

        return Pose.makeTranslation(point.x, point.y, point.z)
    }


    fun placeModelAutomatically() {
        if (isToCloseToWall) return
        val anchor =
            arSceneView.session?.createAnchor(defaultPose(arSceneView)) // Create anchor manually
        anchor?.let {
            if (nodes == null) {
                nodes = Nodes(coordinator, ctx, onARChangeListener, arSceneView, fileName)
            }
            nodes?.attach(anchor)
        }

    }


    fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(ctx, CAMERA) == PERMISSION_GRANTED


    private fun onArTap(motionEvent: MotionEvent) {


    }


    private fun onArUpdate() {
        val frame = arSceneView.arFrame
        val camera = frame?.camera
        val state = camera?.trackingState
        val reason = camera?.trackingFailureReason

        onArUpdateStatusText(state, reason)

    }

    private fun onArUpdateStatusText(state: TrackingState?, reason: TrackingFailureReason?) {
        when (state) {
            TRACKING -> R.string.tracking_success
            PAUSED -> when (reason) {
                NONE -> R.string.tracking_failure_none
                BAD_STATE -> R.string.tracking_failure_bad_state
                INSUFFICIENT_LIGHT -> R.string.tracking_failure_insufficient_light
                EXCESSIVE_MOTION -> R.string.tracking_failure_excessive_motion
                INSUFFICIENT_FEATURES -> R.string.tracking_failure_insufficient_features
                CAMERA_UNAVAILABLE -> R.string.tracking_failure_camera_unavailable
                null -> 0
            }

            STOPPED -> R.string.tracking_stopped
            null -> 0
        }.let {
            trackingStatus = ctx.getString(it)
            if (state == TRACKING || reason == NONE || reason == null) trackingStatus = ""
            showLogMessage()
        }

    }

    private fun showLogMessage() {
        var msg = "$trackingStatus \n$distanceError"
        if (trackingStatus.isEmpty() && distanceError.isEmpty()) {
            msg = ""
        } else if (trackingStatus.isEmpty() && distanceError.isNotEmpty()) {
            msg = distanceError
        } else if (distanceError.isEmpty() && trackingStatus.isNotEmpty()) {
            msg = trackingStatus
        }
        /*var msg = "Status:$trackingStatus Surface:$surface"
        if (trackingStatus.isEmpty() && surface.isEmpty()) {
            msg = ""
        } else if (trackingStatus.isEmpty() && surface.isNotEmpty()) {
            msg = "Surface:$surface"
        } else if (surface.isEmpty() && trackingStatus.isNotEmpty()) {
            msg = "Status:$trackingStatus"
        }*/
        onARChangeListener.onError(msg)


    }

    private var trackingStatus = ""
    private var distanceError = ""

    private val coordinator by lazy {
        Coordinator(
            ctx,
            ::onArTap,
            ::onNodeSelected,
            ::onNodeFocused
        )
    }

    private fun onNodeSelected(old: Nodes? = coordinator.selectedNode, new: Nodes?) {
        old?.onNodeUpdate = null
        new?.onNodeUpdate = ::onNodeUpdate
    }

    private fun onNodeFocused(node: Nodes?) {

    }

    private fun onNodeUpdate(node: Nodes) {
        when {
            node != coordinator.selectedNode || node != coordinator.focusedNode -> Unit
            else -> {
                //   showLog("distance ${arSceneView.arFrame?.camera.formatDistance(ctx, node)}")
                //  showLog("isTransforming ${!node.isTransforming}")
                /* positionValue.text = node.worldPosition.format(this@SceneActivity)
                 rotationValue.text = node.worldRotation.format(this@SceneActivity)
                 scaleValue.text = node.worldScale.format(this@SceneActivity)*/
            }
        }
    }

    fun exit() {
        if (coordinator.selectedNode != null) {
            coordinator.selectNode(null)
        }
    }

    private fun shareImage(imageUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(intent, "Share AR Window"))
    }

    private fun Context.saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "AR_Screenshot_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/AR_Screenshots"
            )
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                shareImage(uri)
                Toast.makeText(this, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } ?: Log.e("Screenshot", "Failed to save image")
    }

    fun takeScreenshot() {
        val view = arSceneView

        // Ensure the AR scene is ready
        if (view.width == 0 || view.height == 0) {
            Log.e("Screenshot", "SceneView not ready")
            return
        }

        val bitmap = createBitmap(view.width, view.height)
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        PixelCopy.request(view, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                ctx.saveBitmapToGallery(bitmap) // Save the screenshot
            } else {
                Log.e("Screenshot", "Failed to capture screenshot")
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun checkUserDistance(frame: Frame) {
        val cameraPose = frame.camera.pose
        val centerX = arSceneView.width / 2f
        val centerY = arSceneView.height / 2f

        val hitResult = frame.hitTest(centerX, centerY).firstOrNull { hit ->
            val trackable = hit.trackable
            trackable is Plane || trackable is DepthPoint

        }

        if (hitResult != null) {
            val distance = calculateDistance(cameraPose, hitResult.hitPose)
            if (distance < .7f) {
                isToCloseToWall = true
                distanceError = "Stay 3 meter away from the wall."
            } else {
                isToCloseToWall = false
                distanceError = ""
            }
            showLogMessage()

        } else {
            isToCloseToWall = false

        }
    }

    private fun calculateDistance(pose1: Pose, pose2: Pose): Float {
        val dx = pose1.tx() - pose2.tx()
        val dy = pose1.ty() - pose2.ty()
        val dz = pose1.tz() - pose2.tz()
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    fun rotateOnY() {
        coordinator.focusedNode.let {
            // Rotate the model on Y-axis when tapped
            val currentRotation = it?.localRotation
            val newRotation = Quaternion.axisAngle(Vector3.up(), 30f) // Rotate by 30 degrees
            it?.localRotation = Quaternion.multiply(currentRotation, newRotation)
        }
        /*
                val node=arSceneView.scene.children.firstOrNull()
                node?.apply {
                    val currentRotation: Quaternion = localRotation
                    val rotationIncrement = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 45f)
                    localRotation = Quaternion.multiply(currentRotation, rotationIncrement)
                }*/
    }


    private var isToCloseToWall = true

    private var updateListener: Scene.OnUpdateListener = Scene.OnUpdateListener {

        onArUpdate()
        val frame = arSceneView.arFrame
        // showLog("OnUpdateListener ${frame?.camera?.trackingState}")
        if (frame?.camera?.trackingState == TRACKING) {

            //placeModelAutomatically()
            checkUserDistance(frame)

        }
    }

    init {
        with(arSceneView) {

            nodes = Nodes(coordinator, ctx, onARChangeListener, arSceneView, fileName)

            scene.addOnUpdateListener(updateListener)

            scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
                coordinator.onTouch(hitTestResult, motionEvent)

            }
            planeRenderer.apply {
                isVisible = true
                isEnabled = true

            }

        }

    }

    companion object {


        const val REQUEST_CAMERA_PERMISSION = 1
    }


}