package gbc.legends.clippex.core.links

object LinkProcessorFactory {

    fun getProcessor(url: String): LinkProcessor {
        val processors = listOf(
            YouTubeLinkProcessor(url = url),
            InstagramLinkProcessor(url=url),
            TikTokLinkProcessor(url=url),
            XLinkProcessor(url=url)
        )

        val genericProcessor = GenericLinkProcessor(url=url)
        return processors.firstOrNull { it.canProcess(url) } ?: genericProcessor
    }
}