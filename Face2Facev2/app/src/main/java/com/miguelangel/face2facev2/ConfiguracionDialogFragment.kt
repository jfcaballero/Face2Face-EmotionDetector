package com.miguelangel.face2facev2

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ConfiguracionDialogFragment(private var preferences: SharedPreferences) : DialogFragment() {
    private var useCameraButtom = preferences.getBoolean("useCameraButton", true)

    private var temp = preferences.getInt("timer", 5)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.configuration_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton = dialog?.findViewById<ImageButton>(R.id.close)
        closeButton?.setOnClickListener {
            dialog?.dismiss()
        }

        val checkCamara = dialog?.findViewById<ImageView>(R.id.check_camara)
        val checkTemp = dialog?.findViewById<ImageView>(R.id.check_tempo)

        val plus = dialog?.findViewById<ImageButton>(R.id.plus)
        val minus = dialog?.findViewById<ImageButton>(R.id.minus)
        val textoTemp = dialog?.findViewById<TextView>(R.id.temporizador)

        if (useCameraButtom) {
            checkTemp?.visibility = View.GONE
            plus?.isClickable = false
            minus?.isClickable = false
        }
        else {
            checkCamara?.visibility = View.GONE
            plus?.visibility = View.VISIBLE
            plus?.isClickable = true
            minus?.visibility = View.VISIBLE
            minus?.isClickable = true
            textoTemp?.text = getString(R.string.segundos, temp)
        }

        val botonCamara = dialog?.findViewById<ImageButton>(R.id.boton_camara)
        botonCamara?.setOnClickListener {
            if (checkCamara?.visibility == View.GONE) {
                checkTemp?.visibility = View.GONE
                checkCamara.visibility = View.VISIBLE
                plus?.visibility = View.GONE
                plus?.isClickable = false
                minus?.visibility = View.GONE
                minus?.isClickable = false
                textoTemp?.text = ""
                useCameraButtom = true
            }
        }

        val botonTemp = dialog?.findViewById<ImageButton>(R.id.boton_tempo)
        botonTemp?.setOnClickListener {
            if (checkTemp?.visibility == View.GONE) {
                checkTemp.visibility = View.VISIBLE
                checkCamara?.visibility = View.GONE
                plus?.visibility = View.VISIBLE
                plus?.isClickable = true
                minus?.visibility = View.VISIBLE
                minus?.isClickable = true
                textoTemp?.text = getString(R.string.segundos, temp)
                useCameraButtom = false
            }
        }

        plus?.setOnClickListener {
            if (temp < 30) {
                temp++
                textoTemp?.text = getString(R.string.segundos, temp)
            }
        }

        minus?.setOnClickListener {
            if (temp > 5) {
                temp--
                textoTemp?.text = getString(R.string.segundos, temp)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        with (preferences.edit()) {
            putBoolean("useCameraButton", useCameraButtom)
            putInt("timer", temp)
            apply()
        }
    }
}