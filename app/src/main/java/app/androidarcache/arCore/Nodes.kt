package app.androidarcache.arCore

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import androidx.core.net.toUri
import app.androidarcache.Utils.Companion.showLog

import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File


class Nodes(
    coordinator: Coordinator, var context: Context,
    private var onARChangeListener: OnARChangeListener,
    private var arSceneView: ArSceneView,
    var  fileName: String,
) : TransformableNode(coordinator) {


    private fun createModel() {
        onARChangeListener.onProgressBar(showLoading = true)
        val cacheDir = File(context.cacheDir, "models")
        val modelFile = File(cacheDir, fileName)
        ModelRenderable.builder()
            .setSource(
                context,
                RenderableSource.builder()
                    .setSource(context, modelFile.toUri(), RenderableSource.SourceType.GLB)
                    .build()
            )
            .build()
            .thenAccept {
                renderable = it
                onARChangeListener.onProgressBar(showLoading = false)

            }.exceptionally {
                showLog("exceptionally ${it.localizedMessage}")
                onARChangeListener.onProgressBar(showLoading = false)
                null
            }
    }

    init {

        createModel()


    }

    var onNodeUpdate: ((Nodes) -> Any)? = null


    override fun getTransformationSystem(): Coordinator =
        super.getTransformationSystem() as Coordinator

    override fun setRenderable(renderable: Renderable?) {
        super.setRenderable(
            renderable?.apply {

                isShadowCaster = false
                isShadowReceiver = false
            },
        )
    }

    override fun onUpdate(frameTime: FrameTime) {
        onNodeUpdate?.invoke(this)
    }


    private fun removePreviousAnchors() {
        val nodeList = arSceneView.scene.children
        for (childNode in nodeList) {
            if (childNode is AnchorNode) {
                if (childNode.anchor != null) {
                    childNode.anchor!!.detach()
                    childNode.setParent(null)
                }
            }
        }
    }


    fun attach(anchor: Anchor) {
        removePreviousAnchors()


        val a = AnchorNode()
        a.localRotation = Quaternion.eulerAngles(Vector3(90f, 90f, 0f))
        // a.localPosition = Vector3(0f, 0f, -0.1f) // Moves it forward slightly
        a.anchor = anchor

        setParent(
            a.apply {
                localScale = Vector3(0.5f, 0.5f, 0.5f) // 50% of original size
                // localScale = Vector3(1.0f, 1.0f, 1.0f)
                /* localRotation = Quaternion.eulerAngles(Vector3(0f, 90f, 0f))*/
                rotationController.apply {
                    isEnabled = true

                }
                translationController.isEnabled = true



                setParent(arSceneView.scene)
                select()
            }
        )
        val cameraPosition = arSceneView.scene.camera.worldPosition
        val modelPosition = worldPosition
        // Calculate direction to camera
        val direction = Vector3.subtract(cameraPosition, modelPosition)
        direction.y = 0f // Ignore Y-axis to prevent tilting
        // Rotate model to always face the camera
        worldRotation = Quaternion.lookRotation(direction, Vector3.up())

        transformationSystem.focusNode(this)

    }


    override fun onTap(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        super.onTap(hitTestResult, motionEvent)

        if (isTransforming) return // ignored when dragging over a small distance
        transformationSystem.focusNode(this)

    }
}




