package com.example.parshad.ui.fragments

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parshad.adapters.ProblemAdapter
import com.example.parshad.databinding.FragmentAccomplishedBinding
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel
import com.example.parshad.util.Status

class AccomplishedFragment : BaseFragment() {

    private lateinit var _binding: FragmentAccomplishedBinding
    private val binding get() = _binding
    private lateinit var viewModel: MainViewModel
    private lateinit var problemAdapter: ProblemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccomplishedBinding.inflate(inflater, container, false)
        viewModel = (activity as MainActivity).mainViewModel
        setUpRecyclerView()
        viewModel.loadAccomplishments()
        setUpObserver()
        setListeners()
        return _binding.root
    }

    private fun setUpObserver() {
        viewModel.accomplished.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                when (response.status) {
                    Status.SUCCESS -> {
                        isLoading(false)
                        response.data?.let {
                            problemAdapter.differ.submitList(it.toList())
                        }
                    }
                    Status.ERROR -> {
                        isLoading(false)
                        showToast(response.toString())
                    }
                    Status.LOADING -> {
                        isLoading(true)
                    }
                }
            }
        })
    }

    private fun isLoading(value:Boolean){
        if(value)
        {
            binding.apply {
                loadingPb.visibility=View.VISIBLE
                rvProblems.visibility=View.GONE
            }
        }
        else
        {
            binding.apply {
                loadingPb.visibility=View.GONE
                rvProblems.visibility=View.VISIBLE
            }
        }
    }

    private fun setUpRecyclerView() {
        problemAdapter = ProblemAdapter()
        binding.rvProblems.apply {
            adapter = problemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setListeners() {
        problemAdapter.setOnFileClickListener { problems ->
            problems.attachment?.let { it1 ->
                problems.attachmentName?.let { it2 ->
                    onFileClicked(it1, it2)
                }
            }
        }
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
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
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${fileName}.pdf")

        val downloadManager = activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }


}