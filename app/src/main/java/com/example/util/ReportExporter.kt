package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun shareSummary(context: Context, title: String, content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "AgriCalc Field Report: $title")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export AgriCalc Field Report")
        context.startActivity(shareIntent)
    }

    /**
     * Prints or exports an HTML formatted AgriCalc field summary using Android PrintManager.
     */
    fun printReport(
        context: Context,
        reportTitle: String,
        category: String,
        primaryMetric: String,
        formula: String,
        details: List<Pair<String, String>>
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val html = buildString {
            append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
            append("<title>$reportTitle</title>")
            append("<style>")
            append("body { font-family: sans-serif; margin: 24px; color: #121E17; }")
            append("h1 { color: #1B4332; margin-bottom: 4px; }")
            append(".tag { display: inline-block; background: #52B788; color: #1B4332; font-weight: bold; padding: 4px 8px; border-radius: 4px; font-size: 12px; margin-bottom: 12px; }")
            append(".hero { background: #E9EFEA; border: 2px solid #52B788; padding: 16px; border-radius: 8px; margin: 16px 0; }")
            append(".hero-val { font-size: 32px; font-weight: bold; color: #1B4332; }")
            append(".formula { font-family: monospace; background: #0F1E16; color: #74C69D; padding: 8px 12px; border-radius: 6px; }")
            append("table { width: 100%; border-collapse: collapse; margin-top: 16px; }")
            append("th, td { text-align: left; padding: 8px; border-bottom: 1px solid #B7CEBF; }")
            append("th { background-color: #F4F7F4; }")
            append(".footer { margin-top: 32px; font-size: 11px; color: #666; border-top: 1px solid #ccc; padding-top: 8px; }")
            append("</style></head><body>")
            append("<h1>AgriCalc Field Record</h1>")
            append("<div class='tag'>$category • METRIC SI • OFFLINE VERIFIED</div>")
            append("<p><strong>Generated:</strong> $dateStr</p>")
            append("<div class='hero'>")
            append("<div><strong>Calculated Output:</strong></div>")
            append("<div class='hero-val'>$primaryMetric</div>")
            append("</div>")
            append("<p><strong>Applied Mathematical Formula:</strong></p>")
            append("<div class='formula'>$formula</div>")
            if (details.isNotEmpty()) {
                append("<h3>Input Parameters & Sub-Metrics</h3>")
                append("<table><tr><th>Parameter</th><th>Value</th></tr>")
                for ((k, v) in details) {
                    append("<tr><td>$k</td><td><strong>$v</strong></td></tr>")
                }
                append("</table>")
            }
            append("<div class='footer'>AgriCalc Offline-First Agricultural Platform. Precision Farming Guardrails.</div>")
            append("</body></html>")
        }

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(reportTitle)
                val jobName = "AgriCalc_${reportTitle.replace(' ', '_')}"
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}
