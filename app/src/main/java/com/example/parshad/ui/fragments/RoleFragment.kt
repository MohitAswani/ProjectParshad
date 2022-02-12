package com.example.parshad.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

class RoleFragment : Fragment() {

    private lateinit var binding: FragmentRoleBinding
    private var role = ""
    private val args: RoleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoleBinding.inflate(inflater, container, false)
        setUpListeners()
        return binding.root
    }

    private fun setUpListeners() {
        binding.authorityCard.setOnClickListener{
            role=Constants.KEY_AUTHORITY_ROLE
            afterClick()
        }
        binding.wardMemberCard.setOnClickListener {
            role=Constants.KEY_WARD_MEMBER_ROLE
            afterClick()
        }
        binding.nextFab.setOnClickListener {
            val action=RoleFragmentDirections.actionRoleFragmentToSignInFragment(args.phoneNumber,role)
            findNavController().navigate(action)
        }
    }

    private fun afterClick() {
        binding.nextFab.isEnabled=true
        if (role == Constants.KEY_WARD_MEMBER_ROLE) {
            binding.wardMemberTxt.setTextColor(resources.getColor(R.color.themeColor))
            binding.wardMemberDesc.setTextColor(resources.getColor(R.color.themeColor))
            binding.authorityTxt.setTextColor(resources.getColor(R.color.fontColorLight))
            binding.authorityDesc.setTextColor(resources.getColor(R.color.fontColorLight))
        } else {
            binding.wardMemberTxt.setTextColor(resources.getColor(R.color.fontColorLight))
            binding.wardMemberDesc.setTextColor(resources.getColor(R.color.fontColorLight))
            binding.authorityTxt.setTextColor(resources.getColor(R.color.themeColor))
            binding.authorityDesc.setTextColor(resources.getColor(R.color.themeColor))
        }
    }

}