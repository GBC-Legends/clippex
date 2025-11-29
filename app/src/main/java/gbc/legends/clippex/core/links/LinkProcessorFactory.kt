package gbc.legends.clippex.core.links

object LinkProcessorFactory {

    fun getProcessor(url: String): LinkProcessor {
        val processors = listOf(
            YouTubeLinkProcessor(),
            InstagramLinkProcessor(),
            TikTokLinkProcessor(),
            XLinkProcessor()
        )

        val genericProcessor = GenericLinkProcessor()
        return processors.firstOrNull { it.canProcess(url) } ?: genericProcessor
    }
}