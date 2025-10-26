package com.example.clippex.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clippex.R
import com.example.clippex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btnDownload = binding.btnDownload

        btnDownload.setOnClickListener {
            val inputLink = binding.linkInput.text.toString().trim()

            // if there's a link, go to the download page
            if (inputLink.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("fileUrl", inputLink)
                }
                findNavController().navigate(R.id.navigation_download, bundle)
            } else { // if empty
                Toast.makeText(requireContext(), "Please enter a valid link", Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}