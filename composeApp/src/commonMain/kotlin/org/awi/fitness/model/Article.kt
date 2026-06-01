package org.awi.fitness.model

data class Article(
    val title: String,
    val description: String,
    val link: String,
    val imageUrl: String?,
    val source: String,
    val pubDate: String
)
