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
import com.example.parshad.databinding.ItemSuggestionBinding
import com.example.parshad.util.Constants
import com.example.parshad.data.entities.Suggestion

class SuggestionAdapter: RecyclerView.Adapter<SuggestionAdapter.ProblemViewHolder>() {

    companion object {
        const val TAG = "ProblemAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewCategory: Int): ProblemViewHolder {
        val itemSuggestionBinding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProblemViewHolder(itemSuggestionBinding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.setIssues(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ProblemViewHolder(private val binding: ItemSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setIssues(suggestion: Suggestion) {
            Glide.with(binding.root)
                .load(getUserImage(suggestion.posterImage))
                .into(binding.userProfile)

            binding.postUserName.text=suggestion.posterName
            binding.postUserAddress.text=suggestion.posterWard
            binding.tvDesc.text=suggestion.description

            when(suggestion.attachmentType){
                "image"->{
                    binding.userPost.visibility= View.VISIBLE
                    Glide.with(binding.root)
                        .load(suggestion.attachment)
                        .into(binding.userPost)
                }
                Constants.KEY_VIDEO_TYPE->{
//                    binding.userPostVideo.visibility=View.VISIBLE
//                    binding.userPostVideo.setVideoURI(suggestion.attachment?.toUri())
                }
                Constants.KEY_PDF_TYPE->{
                    binding.uploadedFile.visibility=View.VISIBLE
                    binding.uploadedFileText.text="Attached file"
                    binding.uploadedFile.setOnClickListener{
                        onFileClickListener?.let {it(suggestion)}
                    }
                }
                else ->{
                }
            }

        }
        private fun getUserImage(encodedImage: String): Bitmap {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    private var onFileClickListener: ((Suggestion) -> Unit)? = null

    fun setOnFileClickListener(listener: (Suggestion) -> Unit) {
        onFileClickListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<Suggestion>() {
        override fun areItemsTheSame(
            oldItem: Suggestion,
            newItem: Suggestion
        ): Boolean {
            return oldItem.description == newItem.description
        }

        override fun areContentsTheSame(
            oldItem: Suggestion,
            newItem: Suggestion
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}