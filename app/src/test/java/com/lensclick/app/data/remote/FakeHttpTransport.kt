package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.HttpTransport

class FakeHttpTransport(private val response: HttpResponse) : HttpTransport {
    var lastRequest: HttpRequest? = null

    override suspend fun execute(request: HttpRequest): HttpResponse {
        lastRequest = request
        return response
    }
}