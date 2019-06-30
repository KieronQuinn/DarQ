package com.kieronquinn.app.darq.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.utils.getColorResCompat
import kotlinx.android.synthetic.main.fragment_faq.view.*
import org.commonmark.node.Heading
import ru.noties.markwon.AbstractMarkwonPlugin
import ru.noties.markwon.Markwon
import ru.noties.markwon.MarkwonSpansFactory
import ru.noties.markwon.core.CoreProps
import ru.noties.markwon.core.MarkwonTheme


class FaqFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { activity ->
            val typeface = ResourcesCompat.getFont(activity, R.font.hkgrotesk)
            val markwon = Markwon.builder(activity).usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    typeface?.let {
                        builder.headingTypeface(it)
                        builder.headingBreakHeight(0)
                    }
                }

                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    val origin = builder.requireFactory(Heading::class.java)
                    builder.setFactory(Heading::class.java) { configuration, props ->
                        arrayOf(origin.getSpans(configuration, props), ForegroundColorSpan(activity.getColorResCompat(android.R.attr.textColorPrimary)))
                    }
                }
            }).build()
            val markdown = activity.assets.open("faq.md").bufferedReader().use { it.readText() }
            markwon.setMarkdown(view.markdown, markdown)
        }

    }

}