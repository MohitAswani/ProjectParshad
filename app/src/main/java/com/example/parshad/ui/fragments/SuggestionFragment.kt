package com.example.parshad.ui.fragments

import android.app.DownloadManager
import android.content.Context
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parshad.adapters.SuggestionAdapter
import com.example.parshad.databinding.FragmentSuggestionBinding
import com.example.parshad.util.Status
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel

class SuggestionFragment : BaseFragment() {

    private lateinit var _binding: FragmentSuggestionBinding
    private val binding get()=_binding
    private lateinit var viewModel: MainViewModel
    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSuggestionBinding.inflate(inflater,container,false)
        viewModel = (activity as MainActivity).mainViewModel
        setUpRecyclerView()
        viewModel.loadSuggestions()
        setUpObserver()
        setListeners()
        return _binding.root
    }

    private fun setUpObserver() {
        viewModel.suggestions.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                when (response.status) {
                    Status.SUCCESS -> {
                        response.data?.let {
                            Log.d("SuggestionFragment", it.toString())
                            suggestionAdapter.differ.submitList(it.toList())
                        }
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }

    private fun setUpRecyclerView() {
        suggestionAdapter = SuggestionAdapter()
        binding.rvSuggestions.apply {
            adapter = suggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setListeners() {
        binding.addSuggestionBtn.setOnClickListener {
            val action = SuggestionFragmentDirections.actionSuggestionFragmentToAddSuggestionFragment()
            findNavController().navigate(action)
        }

        suggestionAdapter.setOnFileClickListener { problems ->
            problems.attachment?.let { it1 ->
                problems.attachmentName?.let { it2 ->
                    onFileClicked(it1, it2)
                }
            }
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

        val downloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

}