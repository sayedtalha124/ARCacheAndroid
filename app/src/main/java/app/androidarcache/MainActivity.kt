package app.androidarcache

import android.Manifest.permission.CAMERA
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import app.androidarcache.ProgressDialog.showLoadingDialog
import app.androidarcache.Utils.Companion.showLog
import app.androidarcache.arCore.ARCore
import app.androidarcache.arCore.OnARChangeListener
import app.androidarcache.databinding.ActivityMainBinding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Config.AugmentedFaceMode
import com.google.ar.core.Config.CloudAnchorMode
import com.google.ar.core.Config.DepthMode
import com.google.ar.core.Config.FocusMode
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Config.PlaneFindingMode.VERTICAL
import com.google.ar.core.Config.UpdateMode
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView
import kotlin.toString

class MainActivity : AppCompatActivity(), OnARChangeListener {


    private fun init() {

        ARInstructionsDialogFragment.open(fragmentManager = supportFragmentManager)
        arSceneView = binding.arSceneView

        binding.apply {
            tvPlaceWindow.setOnClickListener {
                arCore?.placeModelAutomatically()
            }
            buttonRotate.setOnClickListener {
                arCore?.rotateOnY()

            }
            tvTakeScreenshot.setOnClickListener {
                arCore?.takeScreenshot()
            }

            ivBack.setOnClickListener {
                arCore?.exit()
                arSceneView?.destroy()
                finish()

            }
        }


    }

    private var arSceneView: ArSceneView? = null
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ARCore.REQUEST_CAMERA_PERMISSION && arCore?.hasCameraPermission() == false) {
            redirectToApplicationSettings()
        }
    }
    fun AppCompatActivity.redirectToApplicationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    private var arCore: ARCore? = null
    override fun onPause() {
        super.onPause()
        arSceneView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView?.destroy()
    }

    private fun requestCameraPermission() {
        if (arCore?.hasCameraPermission() == false) {
            requestPermissions(arrayOf(CAMERA), ARCore.REQUEST_CAMERA_PERMISSION)
        }
    }

    /* private fun startTracking() {
         arCore?.createNodeAndAddToScene(anchor = {
             arSceneView?.session!!.createAnchor(
             Nodes.defaultPose(
                 arSceneView!!
             )
         )
         }, focus = false)
     }*/

    private var sessionInitializationFailed: Boolean = false
    private val features = emptySet<Session.Feature>()
    private fun initArSession() {
        if (arSceneView?.session != null) {
            return
        }

        if (sessionInitializationFailed) {
            return
        }
        val sessionException: UnavailableException?


        try {
            val requestInstall = ArCoreApk.getInstance().requestInstall(this, !installRequested)
            if (requestInstall == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                installRequested = true
                return
            }
            installRequested = false
            val session = Session(this, features)
            val config = Config(session).apply {

                lightEstimationMode = LightEstimationMode.AMBIENT_INTENSITY
                planeFindingMode = /*HORIZONTAL_AND_VERTICAL*/VERTICAL
                updateMode = UpdateMode.LATEST_CAMERA_IMAGE
                cloudAnchorMode = CloudAnchorMode.DISABLED
                augmentedFaceMode = AugmentedFaceMode.DISABLED
                focusMode = FocusMode.AUTO
                if (session.isDepthModeSupported(DepthMode.AUTOMATIC)) {
                    depthMode = DepthMode.AUTOMATIC
                }


            }
            session.configure(config)
            arSceneView?.setupSession(session)

            return
        } catch (e: UnavailableException) {
            sessionException = e
        } catch (e: Exception) {
            sessionException = UnavailableException().apply { initCause(e) }
        }
        sessionInitializationFailed = true
        if (sessionException != null) {
            showLog("initArSession " + sessionException.message.toString())
        }
        onError(sessionException?.message.toString())
    }

    private var installRequested: Boolean = false
    override fun onResume() {
        super.onResume()
        arCoreSetup()
    }

    private fun arCoreSetup() {
        if (arCore != null) {
            if (arCore?.hasCameraPermission() == false) {
                requestCameraPermission()
                return
            }
            initArSession()
            try {
                arSceneView?.resume()
            } catch (ex: CameraNotAvailableException) {
                sessionInitializationFailed = true
            }

        } else {
            if (arSceneView != null) {
                arCore = ARCore(arSceneView!!,this, this, binding.anchorPlace,
                    intent.getStringExtra("fileName").toString()
                )
            }
            arCoreSetup()
        }
    }


    private var msg = ""
    override fun onError(var1: String) {
        if (var1 == msg) return
        msg = var1
        runOnUiThread {
            binding.let {
                binding.tvArError.text = msg
            }

        }

    }

    override fun onProgressBar(showLoading: Boolean) {
        if (showLoading) {
            showLoading()

        } else {
            hideLoading()
        }
    }

    private var mProgressDialog: Dialog? = null

    fun showLoading() {
        hideLoading()
        mProgressDialog = showLoadingDialog(this)
        mProgressDialog?.show()

    }

    fun hideLoading() {
        runOnUiThread {
            if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                mProgressDialog!!.cancel()
                mProgressDialog!!.dismiss()
            }
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
    }
}