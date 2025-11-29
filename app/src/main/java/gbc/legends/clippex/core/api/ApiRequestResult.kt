package gbc.legends.clippex.core.api


import gbc.legends.clippex.core.api.platforms.ApiResponse

sealed class ApiRequestResult

data class Success(val response: ApiResponse) : ApiRequestResult()

data class Failure(val errorMessage: String, val exception: Exception? = null): ApiRequestResult()