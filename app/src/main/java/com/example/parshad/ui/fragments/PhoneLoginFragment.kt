package com.example.parshad.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.databinding.FragmentLoginPhoneBinding
import com.example.parshad.ui.AuthActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneLoginFragment : BaseFragment() {

    private lateinit var _binding: FragmentLoginPhoneBinding
    private val binding get() = _binding
    private lateinit var authDatabase: AuthDatabase
    private lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginPhoneBinding.inflate(inflater, container, false)

        authDatabase = (activity as AuthActivity).authDatabase

        sendButtonState()

        binding.apply {
            getOtpButton.setOnClickListener {
                if (editNumber.text.toString().trim().length==10) {
                    phoneNumber="+91"+editNumber.text.toString().trim()
                    sendOTP()
                } else {
                    Toast.makeText(requireContext(), "Enter a 10 digit mobile Number", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return _binding.root
    }

    private fun sendButtonState() {
        binding.editNumber.addTextChangedListener {
            binding.getOtpButton.isEnabled = it.toString().isNotEmpty()
        }
    }

    private fun sendOTP() {
        Log.d("Login",phoneNumber)
        val options = PhoneAuthOptions.newBuilder(authDatabase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            binding.apply {
                sendOtpProgressBar.visibility = View.INVISIBLE
                getOtpButton.visibility = View.VISIBLE
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            binding.apply {
                sendOtpProgressBar.visibility = View.INVISIBLE
                getOtpButton.visibility = View.VISIBLE
            }
            Toast.makeText(requireContext(), p0.toString(), Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            binding.apply {
                sendOtpProgressBar.visibility = View.INVISIBLE
                getOtpButton.visibility = View.VISIBLE
            }
            val action = PhoneLoginFragmentDirections.actionPhoneLoginFragmentToEnterOTPFragment(
                verificationId,
                token,
                phoneNumber
            )
            findNavController().navigate(action)
            super.onCodeSent(verificationId, token)
        }

    }
}