package com.newsapp.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getWebViewUrl(): String? = suspendCancellableCoroutine { cont ->
        firestore.collection("config").document("app").get()
            .addOnSuccessListener { document ->
                val url = document?.getString("url")
                if (cont.isActive) cont.resume(url)
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resume(null)
            }
    }
}
