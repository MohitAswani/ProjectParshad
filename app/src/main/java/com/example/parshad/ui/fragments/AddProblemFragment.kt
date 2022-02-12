package com.example.parshadapp.ui.fragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.parshad.ui.fragments.BaseFragment
import com.example.parshadapp.R
import com.example.parshadapp.data.entities.Problems
import com.example.parshadapp.databinding.FragmentAddProblemBinding
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel
import com.example.parshadapp.util.Constants
import java.io.FileNotFoundException
import java.util.*

class AddProblemFragment : BaseFragment() {

    private lateinit var _binding: FragmentAddProblemBinding
    private val binding get() = _binding
    private lateinit var viewModel: MainViewModel
    private var userImage: String = ""
    private var userPhone: String = ""
    private var userWard: String = ""
    private var userName: String = ""
    private var attachedType="None"
    private var attachedFile:String=""
    private var attachedFileName:String=""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProblemBinding.inflate(inflater, container, false)

        viewModel = (activity as MainActivity).mainViewModel

        setUserDetails()

        setListeners()
        return _binding.root
    }

    private fun setListeners() {
        binding.submitBtn.setOnClickListener {
            viewModel.postProblems(
                Problems(
                    posterImage = userImage,
                    posterName = userName,
                    posterNumber = userPhone,
                    posterWard = userWard,
                    description = binding.editTxtDescribe.text.toString(),
                    attachmentType = attachedType,
                    postedDate = Date(),
                    postedTime = Date().time,
                    attachment = attachedFile,
                    attachmentName = attachedFileName,
                    severity = binding.severitySlider.value.toLong()
                )
            )
            Toast.makeText(requireContext(),"Problem added!",Toast.LENGTH_SHORT).show()
            val action=AddProblemFragmentDirections.actionAddProblemFragmentToProblemsFragment()
            findNavController().navigate(action)
        }

        binding.editTxtDescribe.addTextChangedListener (object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    binding.submitBtn.isEnabled=true
                }
                else if(attachedType=="None")
                {
                    binding.submitBtn.isEnabled=false
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

        binding.photoImg.setOnClickListener {
            pickImage()
        }

        binding.severitySlider.value

//        binding.videoImg.setOnClickListener {
//            pickVideo()
//        }

        binding.pdfImg.setOnClickListener {
            pickFile()
        }
    }

    private fun setUserDetails() {
        viewModel.userName.observe(viewLifecycleOwner) {
            userName = it
        }

        viewModel.userImage.observe(viewLifecycleOwner) {
            userImage = it?:""
        }

        viewModel.userWard.observe(viewLifecycleOwner) {
            userWard = it
        }

        viewModel.userPhone.observe(viewLifecycleOwner) {
            userPhone = it
        }

    }

    private fun pickImage(){
        val intent= Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForImageResult.launch(intent)
    }

    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri = result.data?.data!!
                try {
                    uploadingImages(imageUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun uploadingImages(uri:Uri)
    {
        val dialogBox= ProgressDialog(requireContext())
        dialogBox.setMessage("Uploading")

        dialogBox.show()

        attachedFileName=System.currentTimeMillis().toString()
        val ref = (activity as MainActivity).authDatabase.storage.reference.child("files/${attachedFileName}")
        val uploadTask = ref.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogBox.dismiss()
                showToast("Photo uploaded successfully")
                attachedFile=task.result.toString()
                attachedType=Constants.KEY_IMAGE_TYPE
                onUploaded()
            } else {
                showToast("Photo cannot be uploaded : ${task.result}")
            }
        }

    }

    private fun pickVideo(){
        val intent= Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startForVideoResult.launch(intent)
    }

    private val startForVideoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val videoUri: Uri = result.data?.data!!
                try {
                    uploadingVideos(videoUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun uploadingVideos(uri:Uri)
    {
        val dialogBox= ProgressDialog(requireContext())
        dialogBox.setMessage("Uploading")

        dialogBox.show()

        attachedFileName=System.currentTimeMillis().toString()
        val ref = (activity as MainActivity).authDatabase.storage.reference.child("files/${attachedFileName}")
        val uploadTask = ref.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogBox.dismiss()
                showToast("Video uploaded successfully")
                attachedFile=task.result.toString()
                attachedType=Constants.KEY_VIDEO_TYPE
                onUploaded()
            } else {
                showToast("Video cannot be uploaded : ${task.result}")
            }
        }

    }

    private fun pickFile(){
        val intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/*"
        startForFileResult.launch(intent)
    }

    private val startForFileResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val fileUri: Uri = result.data?.data!!
                try {
                    uploadingFiles(fileUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun uploadingFiles(uri:Uri)
    {
        val dialogBox= ProgressDialog(requireContext())
        dialogBox.setMessage("Uploading")

        dialogBox.show()

        attachedFileName=System.currentTimeMillis().toString()
        val ref = (activity as MainActivity).authDatabase.storage.reference.child("files/${attachedFileName}")
        val uploadTask = ref.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogBox.dismiss()
                showToast("File uploaded successfully")
                attachedFile=task.result.toString()
                attachedType=Constants.KEY_PDF_TYPE
                onUploaded()
            } else {
                showToast("File cannot be uploaded : ${task.result}")
            }
        }

    }

    private fun onUploaded(){
        binding.apply {
            attachmentTxt.text="Uploaded file"
            photoCard.visibility=View.INVISIBLE
            photoTxt.visibility=View.INVISIBLE
//            videoCard.visibility=View.GONE
//            videoTxt.visibility=View.GONE
            pdfCard.visibility=View.INVISIBLE
            pdfTxt.visibility=View.INVISIBLE
            submitBtn.isEnabled=true

            uploadedCard.visibility=View.VISIBLE
            uploadedText.text=attachedFileName
            when(attachedType){
                Constants.KEY_VIDEO_TYPE->uploadedImg.setImageResource(R.drawable.video)
                Constants.KEY_IMAGE_TYPE->uploadedImg.setImageResource(R.drawable.photo)
                Constants.KEY_PDF_TYPE->uploadedImg.setImageResource(R.drawable.pdf)
                else->uploadedImg.setImageResource(R.drawable.video)
            }
            cancelUpload.setOnClickListener {
                onCancelUpload()
            }
        }
    }

    private fun onCancelUpload(){
        binding.apply {
            attachmentTxt.text="Attachment"
            photoCard.visibility=View.VISIBLE
            photoTxt.visibility=View.VISIBLE
//            videoCard.visibility=View.VISIBLE
//            videoTxt.visibility=View.VISIBLE
            pdfCard.visibility=View.VISIBLE
            pdfTxt.visibility=View.VISIBLE

            uploadedCard.visibility=View.GONE

            attachedFile=""
            attachedFileName=""
            attachedType="None"

            if(editTxtDescribe.text.isNullOrEmpty())
            {
                submitBtn.isEnabled=false
            }
        }
    }


}