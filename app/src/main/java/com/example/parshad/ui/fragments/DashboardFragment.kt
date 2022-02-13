package com.example.parshad.ui.fragments

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.*
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.parshad.R
import com.example.parshad.data.entities.Problems
import com.example.parshad.databinding.FragmentDashboardBinding
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel
import com.example.parshad.util.Constants
import com.example.parshad.util.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DashboardFragment : BaseFragment() {

    private lateinit var _binding: FragmentDashboardBinding
    private val binding get() = _binding
    private lateinit var viewModel: MainViewModel
    private var rep: Long = 0L

    override fun onPause() {
        super.onPause()
        rep=0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setClickListeners()
        viewModel = (activity as MainActivity).mainViewModel
        viewModel.loadProblems()
        setUserName()
        setObservers()
        return _binding.root
    }

    var job: Job?=null

    private fun setObservers() {
        viewModel.problems.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                when (response.status) {
                    Status.SUCCESS -> {
                        response.data?.let {
                            Log.d("Dashboard", it.toString())
                            if (it.size != 0) {
                                binding.recentProblemCard.visibility = View.VISIBLE
                                binding.recentProblemTxt.visibility = View.VISIBLE
                                setRecentProblem(it[0])
                                rep=0
                                job?.cancel()
                            }
                            else {
                                binding.recentProblemCard.visibility = View.GONE
                                binding.recentProblemTxt.visibility = View.GONE
                                Log.d("Dashboard", rep.toString())
                                if(rep<10L) {
                                    rep=rep.plus(1L)
                                    job = lifecycleScope.launch {
                                        delay(2000L)
                                        viewModel.loadProblems()

                                    }
                                }
                                else{
                                    job?.cancel()
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        showToast(response.toString())
                    }
                    Status.LOADING -> {
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.problemImgBtn.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToProblemsFragment()
            findNavController().navigate(action)
        }
        binding.suggestionImgBtn.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToSuggestionFragment()
            findNavController().navigate(action)
        }
        binding.accomplishedImgBtn.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToAccomplishedFragment()
            findNavController().navigate(action)
        }
    }



    private fun setUserName() {
        viewModel.getNameFromDataStore()
        viewModel.userName.observe(viewLifecycleOwner, Observer {
            binding.userName.text = it
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.nav_menu, menu)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.supportActionBar!!.show()
        (activity as MainActivity?)!!.supportActionBar!!.setTitle("")
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity?)!!.supportActionBar!!.hide()
    }

    private fun setRecentProblem(problem: Problems) {
        problem.posterImage.let {
            if (it.isNotEmpty()) {
                Glide.with(binding.root)
                    .load(getUserImage(problem.posterImage))
                    .fallback(R.drawable.example_photo)
                    .into(binding.userProfile)
            } else {
                Glide.with(binding.root)
                    .load(R.drawable.example_photo)
                    .into(binding.userProfile)
            }
        }

        binding.postUserName.text = problem.posterName
        binding.postUserAddress.text = problem.posterWard
        binding.tvDesc.text = problem.description

        when (problem.attachmentType) {
            "image" -> {
                binding.userPost.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(problem.attachment)
                    .into(binding.userPost)
            }
            Constants.KEY_VIDEO_TYPE -> {
//                    binding.userPostVideo.visibility=View.VISIBLE
//                    binding.userPostVideo.setVideoURI(problem.attachment?.toUri())
            }
            Constants.KEY_PDF_TYPE -> {
                binding.uploadedFile.visibility = View.VISIBLE
                binding.uploadedFileText.text = "Attached file"
                binding.uploadedFile.setOnClickListener {
                    problem.attachment?.let { it1 ->
                        problem.attachmentName?.let { it2 ->
                            onFileClicked(it1, it2)
                        }
                    }
                }
            }
            else -> {
            }
        }

    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun onFileClicked(file: String, fileName: String) {
        Log.d("ChatActivity", "file being download ${file}")
        val request = DownloadManager.Request(Uri.parse(file))
        val title = URLUtil.guessFileName(file, null, null)
        request.setTitle(title)
        request.setDescription("Downloading")
        val cookie = CookieManager.getInstance().getCookie(file)
        request.addRequestHeader("cookie", cookie)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${fileName}.pdf"
        )

        val downloadManager =
            activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

}