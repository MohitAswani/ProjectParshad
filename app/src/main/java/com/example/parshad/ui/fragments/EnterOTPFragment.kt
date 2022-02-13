package com.example.parshad.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.parshad.data.entities.User
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.databinding.FragmentOtpBinding
import com.example.parshad.ui.AuthActivity
import com.example.parshad.ui.MainActivity
import com.example.parshad.util.Constants
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class EnterOTPFragment : BaseFragment() {

    private lateinit var _binding: FragmentOtpBinding
    private val binding get() = _binding
    private val args: EnterOTPFragmentArgs by navArgs()
    private lateinit var authDatabase: AuthDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        authDatabase = (activity as AuthActivity).authDatabase
        binding.otpPhoneNumber.text = args.phoneNumber
        otpAutoMove()
        loginClickListener()
        resendClickListener()
        verifyButtonStates()
        changeClickListener()
        return _binding.root
    }

    private fun changeClickListener() {
        binding.otpChangePhoneNumber.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun resendClickListener() {
        binding.otpResend.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(authDatabase.auth)
                .setPhoneNumber(args.phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(args.resendToken)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            isLoading(false)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            isLoading(false)
            Toast.makeText(requireContext(), p0.toString(), Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            isLoading(false)
            Toast.makeText(requireContext(), "OTP sent again", Toast.LENGTH_LONG).show()
            super.onCodeSent(verificationId, token)
        }

    }

    private fun verifyButtonStates() {
        binding.apply {
            inputotp6.addTextChangedListener {
                verifyButton.isEnabled =
                    (inputotp1.text.toString().trim().isNotEmpty() && inputotp2.text.toString()
                        .trim().isNotEmpty()
                            && inputotp3.text.toString().trim()
                        .isNotEmpty() && inputotp4.text.toString()
                        .trim().isNotEmpty()
                            && inputotp5.text.toString().trim()
                        .isNotEmpty() && inputotp6.text.toString()
                        .trim().isNotEmpty())
            }
        }
    }

    private fun loginClickListener() {
        binding.apply {
            verifyButton.setOnClickListener {
                if (inputotp1.text.toString().trim().isNotEmpty() && inputotp2.text.toString()
                        .trim().isNotEmpty()
                    && inputotp3.text.toString().trim().isNotEmpty() && inputotp4.text.toString()
                        .trim().isNotEmpty()
                    && inputotp5.text.toString().trim().isNotEmpty() && inputotp6.text.toString()
                        .trim().isNotEmpty()
                ) {
                    val sb = StringBuilder()
                    sb.append(inputotp1.text.toString())
                    sb.append(inputotp2.text.toString())
                    sb.append(inputotp3.text.toString())
                    sb.append(inputotp4.text.toString())
                    sb.append(inputotp5.text.toString())
                    sb.append(inputotp6.text.toString())
                    val otp: String = sb.toString()

                    isLoading(true)
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        args.verificationToken, otp
                    )
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Please enter all 6 digit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        authDatabase.auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Welcome", Toast.LENGTH_SHORT).show()
                    authSuccessful()
                } else {
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        isLoading(false)
                        Toast.makeText(requireContext(), "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun authSuccessful() {
        isLoading(true)
        authDatabase.firestore.collection(Constants.KEY_COLLECTIONS_USER)
            .whereEqualTo(Constants.KEY_PHONE_NUMBER, args.phoneNumber)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result?.documents?.size!! > 0) {
                    val documentSnapshot = it.result?.documents?.get(0)
                    (activity as AuthActivity).authViewModel.saveUserData(
                        User(
                        name = documentSnapshot?.getString(Constants.USER_NAME)?:"Error 404",
                        aadhar = documentSnapshot?.getString(Constants.USER_AADHAR)?:"Error 404",
                        phoneNumber = documentSnapshot?.getString(Constants.USER_PHONE_NUMBER)?:"Error 404",
                        currentAddress = documentSnapshot?.getString(Constants.USER_ADDRESS)?:"Error 404",
                        image = documentSnapshot?.getString(Constants.USER_IMAGE)?:"Error 404",
                        gender = documentSnapshot?.getString(Constants.USER_GENDER)?:"Error 404",
                        userRole = documentSnapshot?.getString(Constants.USER_ROLE)?:"Error 404"
                    )
                    )
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.apply {
                        (activity as AuthActivity).authViewModel.setIsSignedInFromDataStore(true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    val action =
                        EnterOTPFragmentDirections.actionEnterOTPFragmentToRoleFragment(args.phoneNumber)
                    findNavController().navigate(action)
                }
            }
    }

    private fun isLoading(value: Boolean) {
        if (value) {
            binding.apply {
                verifyButton.visibility = View.INVISIBLE
                verifyOtpProgressBar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                verifyButton.visibility = View.VISIBLE
                verifyOtpProgressBar.visibility = View.INVISIBLE
            }
        }

    }

    private fun otpAutoMove() {
        binding.apply {
            inputotp1.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()) //size as per your requirement
                    {
                        inputotp2.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
            inputotp2.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()) //size as per your requirement
                    {
                        inputotp3.requestFocus()
                    } else {
                        inputotp1.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {

                }
            })
            inputotp3.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    if (s.toString().trim().isNotEmpty()) //size as per your requirement
                    {
                        inputotp4.requestFocus()
                    } else {
                        inputotp2.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {

                }

                override fun afterTextChanged(s: Editable) {

                }
            })
            inputotp4.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()) //size as per your requirement
                    {
                        inputotp5.requestFocus()
                    } else {
                        inputotp3.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {

                }

                override fun afterTextChanged(s: Editable) {

                }
            })
            inputotp5.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()) //size as per your requirement
                    {
                        inputotp6.requestFocus()
                    } else {
                        inputotp4.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
            inputotp6.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isEmpty()) //size as per your requirement
                    {
                        inputotp5.requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
        }
    }
}