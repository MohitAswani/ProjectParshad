package com.example.parshad.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parshad.R
import com.example.parshad.databinding.ItemProblemBinding
import com.example.parshad.util.Constants
import com.example.parshad.data.entities.Problems

class ProblemAdapter : RecyclerView.Adapter<ProblemAdapter.ProblemViewHolder>() {

    companion object {
        const val TAG = "ProblemAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewCategory: Int): ProblemViewHolder {
        val itemIssuesBinding = ItemProblemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProblemViewHolder(itemIssuesBinding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.setIssues(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ProblemViewHolder(private val binding: ItemProblemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setIssues(problem: Problems) {
            problem.posterImage.let {
                if (it.isNotEmpty()) {
                    Glide.with(binding.root)
                        .load(getUserImage(problem.posterImage))
                        .fallback(R.drawable.example_photo)
                        .into(binding.userProfile)
                }
                else
                {
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
                        onFileClickListener?.let { it(problem) }
                    }
                }
                else -> {
                }
            }

            binding.root.setOnLongClickListener {
                onProblemLongPressListener?.let { it(problem) } ?: false
            }

        }

        private fun getUserImage(encodedImage: String): Bitmap {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    private var onFileClickListener: ((Problems) -> Unit)? = null

    fun setOnFileClickListener(listener: (Problems) -> Unit) {
        onFileClickListener = listener
    }

    private var onProblemLongPressListener: ((Problems) -> Boolean)? = null

    fun setOnProblemLongPressListener(listener: (Problems) -> Boolean) {
        onProblemLongPressListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<Problems>() {
        override fun areItemsTheSame(
            oldItem: Problems,
            newItem: Problems
        ): Boolean {
            return oldItem.description == newItem.description
        }

        override fun areContentsTheSame(
            oldItem: Problems,
            newItem: Problems
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}