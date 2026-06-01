package org.awi.fitness.repository

import com.prof18.rssparser.RssParser
import org.awi.fitness.model.Article

class ArticleRepository {
    private val rssParser = RssParser()

    private fun extractImageFromHtml(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val regex = """<img[^>]+src=["']([^"']+)["']""".toRegex()
        return regex.find(html)?.groupValues?.getOrNull(1)
    }

    suspend fun fetchArticles(language: String = "en"): Result<List<Article>> {
        return try {
            val articles = mutableListOf<Article>()
            val seenLinks = mutableSetOf<String>()

            val healthUrl = if (language == "nl") {
                "https://news.google.com/rss/headlines/section/topic/HEALTH?hl=nl&gl=NL&ceid=NL:nl"
            } else {
                "https://news.google.com/rss/headlines/section/topic/HEALTH?hl=en-US&gl=US&ceid=US:en"
            }

            val fitnessUrl = if (language == "nl") {
                "https://news.google.com/rss/search?q=fitness+training+voeding&hl=nl&gl=NL&ceid=NL:nl"
            } else {
                "https://news.google.com/rss/search?q=fitness+workout+exercise+nutrition&hl=en-US&gl=US&ceid=US:en"
            }

            val feeds = listOf(
                healthUrl to "Google Health",
                fitnessUrl to "Google Fitness"
            )

            for ((url, sourceName) in feeds) {
                try {
                    val channel = rssParser.getRssChannel(url)
                    channel.items.forEach { item ->
                        val link = item.link ?: return@forEach
                        if (seenLinks.contains(link)) return@forEach
                        seenLinks.add(link)

                        articles.add(
                            Article(
                                title = item.title ?: "",
                                description = item.description
                                    ?.replace(Regex("<[^>]*>"), "")
                                    ?.trim()
                                    ?.take(200)
                                    ?: "",
                                link = link,
                                imageUrl = item.image ?: extractImageFromHtml(item.description),
                                source = item.sourceName ?: sourceName,
                                pubDate = item.pubDate ?: ""
                            )
                        )
                    }
                } catch (_: Exception) { }
            }

            Result.success(articles.take(30))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
