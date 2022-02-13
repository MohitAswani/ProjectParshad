package com.example.parshad.ui.fragments

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parshad.adapters.ProblemAdapter
import com.example.parshad.databinding.FragmentProblemBinding
import com.example.parshad.util.Status
import com.example.parshad.ui.MainActivity
import com.example.parshad.ui.viewModels.MainViewModel

class ProblemsFragment : BaseFragment() {

    private lateinit var _binding: FragmentProblemBinding
    private val binding get() = _binding
    private lateinit var viewModel: MainViewModel
    private lateinit var problemAdapter: ProblemAdapter
    private var userRole: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProblemBinding.inflate(inflater, container, false)
        viewModel = (activity as MainActivity).mainViewModel
        setUserDetails()
        setUpRecyclerView()
        viewModel.loadProblems()
        setUpObserver()
        return _binding.root
    }

    private fun setUpObserver() {
        viewModel.problems.observe(viewLifecycleOwner, Observer { response ->
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

    private fun setUserDetails() {
        viewModel.userRole.observe(viewLifecycleOwner) {
            userRole = it
            Log.d("ProbFrag",it)
            setListeners()
        }

    }

    private fun isLoading(value: Boolean) {
        if (value) {
            binding.apply {
                loadingPb.visibility = View.VISIBLE
                rvProblems.visibility = View.GONE
            }
        } else {
            binding.apply {
                loadingPb.visibility = View.GONE
                rvProblems.visibility = View.VISIBLE
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
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.addProblemBtn.setOnClickListener {
            val action = ProblemsFragmentDirections.actionProblemsFragmentToAddProblemFragment()
            findNavController().navigate(action)
        }

        problemAdapter.setOnFileClickListener { problems ->
            problems.attachment?.let { it1 ->
                problems.attachmentName?.let { it2 ->
                    onFileClicked(it1, it2)
                }
            }
        }

        if (userRole == "authority") {
            problemAdapter.setOnProblemLongPressListener { problems ->
                Log.d("ProblemFrag", "LongPressed")
                val builder = AlertDialog.Builder(requireContext())
                builder.apply {
                    setMessage("Has this problem been solved ?")
                    setTitle("Problem solved")
                    setCancelable(false)
                    setPositiveButton(
                        "Yes"
                    ) { p0, p1 ->
                        (activity as MainActivity).mainViewModel.deleteProblems(problems)
                    }
                    setNegativeButton(
                        "No"
                    ) { p0, p1 ->
                        showToast("Lol!")
                    }
                }
                val alertBox = builder.create()
                alertBox.show()
                true
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
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${fileName}.pdf"
        )

        val downloadManager = activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }


}