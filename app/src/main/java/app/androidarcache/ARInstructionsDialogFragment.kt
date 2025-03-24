package app.androidarcache

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import app.androidarcache.databinding.DialogArInstructionsBinding


class ARInstructionsDialogFragment : DialogFragment() {
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val percent = 90 / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    private var _binding: DialogArInstructionsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        _binding = DialogArInstructionsBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {

        binding.apply {


            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }

    companion object {
        fun open(
            fragmentManager: FragmentManager,
        ) {
            try {
                val bottomSheet = ARInstructionsDialogFragment()
                bottomSheet.apply {
                    arguments = Bundle().apply {

                    }
                    show(fragmentManager, "open")
                }
            } catch (_: Exception) {
            }
        }


    }

}