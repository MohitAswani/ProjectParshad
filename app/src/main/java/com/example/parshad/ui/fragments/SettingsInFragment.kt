package com.example.parshad.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import com.example.parshad.R
import com.example.parshad.data.entities.User
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.databinding.FragmentSettingsBinding
import com.example.parshad.ui.AuthActivity
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel
import com.example.parshad.util.Constants
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SettingsInFragment : BaseFragment() {

    private lateinit var _binding: FragmentSettingsBinding
    private val binding get() = _binding
    private var encodedImage: String? = null
    private var gender = ""
    private var phoneNumber = ""
    private var role = ""
    private lateinit var authDatabase: AuthDatabase
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        authDatabase = (activity as MainActivity).authDatabase
        viewModel = (activity as MainActivity).mainViewModel
        setUserDetails()
        setListeners()
        return _binding.root
    }

    private fun setUserDetails() {
        binding.apply {
            viewModel.userName.observe(viewLifecycleOwner) {
                editFullName.setText(it)
            }

            viewModel.userImage.observe(viewLifecycleOwner) {
                if (it != null && it.isNotBlank() && it.isNotEmpty()) {
                    userImage.setImageBitmap(getUserImage(it))
                    encodedImage=it
                }
            }

            viewModel.userWard.observe(viewLifecycleOwner) {
                editAddress.setText(it)
            }

            viewModel.userAadhar.observe(viewLifecycleOwner) {
                editAadharNumber.setText(it)
            }

            viewModel.userGender.observe(viewLifecycleOwner) {
                this@SettingsInFragment.gender=it
                afterSetGender()
            }

            viewModel.userPhone.observe(viewLifecycleOwner) {
                this@SettingsInFragment.phoneNumber=it
            }

            viewModel.userRole.observe(viewLifecycleOwner) {
                this@SettingsInFragment.role=it
            }
        }

    }

    private fun getUserImage(encodedImage: String): Bitmap {

        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    }

    private fun highlightEmptyFields(): Boolean {
        binding.apply {
            if (encodedImage.isNullOrEmpty()) {
                addImageText.setTextColor(Color.RED)
            }
            if (editFullName.text.isNullOrEmpty()) {
                fullName.setTextColor(Color.RED)
                return false
            }

            if (editAddress.text.isNullOrEmpty()) {
                address.setTextColor(Color.RED)
                return false
            }

            if (editAadharNumber.text.isNullOrEmpty()) {
                editAadharNumber.setTextColor(Color.RED)
                return false
            }

            if (this@SettingsInFragment.gender.isEmpty()) {
                gender.setTextColor(Color.RED)
                return false
            }

            return true
        }
    }

    private fun setListeners() {

        binding.addImageText.visibility = View.GONE
        binding.addUserImg.visibility = View.GONE

        binding.signUpButton.setOnClickListener {
            if(highlightEmptyFields())
            {
                binding.apply {
                    val user = User(
                        name = editFullName.text.toString(),
                        image = encodedImage ?: "",
                        phoneNumber =this@SettingsInFragment.phoneNumber,
                        gender = this@SettingsInFragment.gender,
                        currentAddress = editAddress.text.toString(),
                        aadhar = editAadharNumber.text.toString(),
                        userRole = this@SettingsInFragment.role
                    )
                    signUp(user)
                }
            }
        }
        binding.imageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
        binding.maleCard.setOnClickListener {
            gender = Constants.USER_GENDER_MALE
            Log.d("SignIn", gender)
            afterSetGender()
        }
        binding.femaleCard.setOnClickListener {
            Log.d("SignIn", gender)
            gender = Constants.USER_GENDER_FEMALE
            afterSetGender()
        }
        binding.otherCard.setOnClickListener {
            Log.d("SignIn", gender)
            gender = Constants.USER_GENDER_OTHER
            afterSetGender()
        }
    }

    private fun afterSetGender() {
        when (gender) {
            Constants.USER_GENDER_MALE -> {
                binding.apply {
                    maleButton.setTextColor(resources.getColor(R.color.themeColor))
                    femaleButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    otherButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    maleCard.strokeColor = resources.getColor(R.color.themeColor)
                    femaleCard.strokeColor = resources.getColor(R.color.fontColorLight)
                    otherCard.strokeColor = resources.getColor(R.color.fontColorLight)
                }
            }
            Constants.USER_GENDER_FEMALE -> {
                binding.apply {
                    maleButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    femaleButton.setTextColor(resources.getColor(R.color.themeColor))
                    otherButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    maleCard.strokeColor = resources.getColor(R.color.fontColorLight)
                    femaleCard.strokeColor = resources.getColor(R.color.themeColor)
                    otherCard.strokeColor = resources.getColor(R.color.fontColorLight)
                }
            }
            Constants.USER_GENDER_OTHER -> {
                binding.apply {
                    maleButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    femaleButton.setTextColor(resources.getColor(R.color.themeColor))
                    otherButton.setTextColor(resources.getColor(R.color.fontColorLight))
                    maleCard.strokeColor = resources.getColor(R.color.fontColorLight)
                    femaleCard.strokeColor = resources.getColor(R.color.fontColorLight)
                    otherCard.strokeColor = resources.getColor(R.color.themeColor)
                }
            }
            else -> {}
        }
    }

    private fun signUp(user: User) {
        (activity as MainActivity).mainViewModel.saveUserData(user)
//        viewModel.changeSettings()
        requireActivity().onBackPressed()
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val pWidth = 150;
        val pHeight = bitmap.height * pWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri: Uri = result.data?.data!!
                try {
                    val inputStream = activity?.contentResolver?.openInputStream(imageUri)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.userImage.setImageBitmap(bitmap)
                    binding.addImageText.visibility = View.GONE
                    binding.addUserImg.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun loading(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                binding.signUpPb.visibility = View.VISIBLE
                binding.signUpButton.visibility = View.GONE
            }
            else -> {
                binding.signUpPb.visibility = View.GONE
                binding.signUpButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onStop() {
        super.onStop()

    }

}