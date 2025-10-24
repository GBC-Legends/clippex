package com.example.clippex.core.links

object LinkProcessorFactory {
    private val processors = listOf(
        YouTubeLinkProcessor(),
        InstagramLinkProcessor(),
        TikTokLinkProcessor(),
        XLinkProcessor()
    )

    private val genericProcessor = GenericFileProcessor()

    fun getProcessor(url: String): LinkProcessor {
        return processors.firstOrNull { it.canProcess(url) } ?: genericProcessor
    }
}