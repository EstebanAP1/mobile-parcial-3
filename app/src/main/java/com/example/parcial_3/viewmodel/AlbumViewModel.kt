package com.example.parcial_3.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial_3.model.Album
import com.example.parcial_3.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AlbumViewModel: ViewModel() {
    private val _uiState = mutableStateOf<UiState>(UiState.Loading)
    val uiState: State<UiState> get() = _uiState

    init {
        fetchAlbums()
    }

    private fun fetchAlbums() {
        viewModelScope.launch {
            _uiState.value = try {
                val response = RetrofitInstance.api.getAlbums()
                if (response.isSuccessful) {
                    UiState.Success(response.body() ?: emptyList())
                } else {
                    UiState.Error("Error ${response.code()}")
                }
            } catch (e: IOException) {
                UiState.Error("Network Error")
            } catch (e: HttpException) {
                UiState.Error("HTTP Error")
            }
        }
    }
}

sealed class UiState {
    data object Loading : UiState()
    data class Success(val albums: List<Album>) : UiState()
    data class Error(val message: String) : UiState()
}