package com.example.parshad.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parshad.MyApplication
import com.example.parshad.R
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.ui.AuthActivity
import com.example.parshad.ui.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {
    private lateinit var authDatabase: AuthDatabase
    private lateinit var application: MyApplication
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        authDatabase = (activity as AuthActivity).authDatabase
        application = (activity as AuthActivity).application as MyApplication
        updateUI()
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun updateUI() {
        lifecycleScope.launch {
            delay(1000L)
            (activity as AuthActivity).authViewModel.isSignedIn.observe(viewLifecycleOwner) {
                Log.d("Splash",it.toString())
                if (authDatabase.auth.currentUser != null && it == true) {
                    Intent(requireActivity(), MainActivity::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                } else {
                    val action = SplashFragmentDirections.actionSplashFragmentToPhoneLoginFragment()
                    findNavController().navigate(action)
                }
            }

        }
    }


}