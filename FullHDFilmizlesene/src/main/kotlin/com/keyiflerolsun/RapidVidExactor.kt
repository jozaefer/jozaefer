// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class RapidVid : ExtractorApi() {
    override val name            = "RapidVid"
    override val mainUrl         = "https://rapidvid.net"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref   = referer ?: ""
        val video_req = app.get(url, referer=ext_ref).text

        Regex("""captions\",\"file\":\"([^\"]+)\",\"label\":\"([^\"]+)\"""").findAll(video_req).forEach {
            val (sub_url, sub_lang) = it.destructured

            subtitleCallback.invoke(
                SubtitleFile(
                    lang = sub_lang,
                    url  = fixUrl(sub_url.replace("\\", ""))
                )
            )
        }

        val extracted_value = Regex("""file": "(.*)",""").find(video_req)?.groupValues?.get(1) ?: throw Error("File not found")

        val bytes   = extracted_value.split("\\x").filter { it.isNotEmpty() }.map { it.toInt(16).toByte() }.toByteArray()
        val decoded = String(bytes, Charsets.UTF_8)
        Log.d("Kekik_${this.name}", "decoded » ${decoded}")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = decoded,
                referer = ext_ref,
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )
    }
}