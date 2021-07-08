package com.kieronquinn.app.darq.ui.screens.settings.faq

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentSettingsFaqBinding
import com.kieronquinn.app.darq.ui.base.AutoExpandOnRotate
import com.kieronquinn.app.darq.ui.base.BackAvailable
import com.kieronquinn.app.darq.ui.base.BoundFragment
import com.kieronquinn.app.darq.ui.utils.TransitionUtils
import com.kieronquinn.app.darq.utils.extensions.getColorResCompat
import com.kieronquinn.monetcompat.extensions.views.enableStretchOverscroll
import org.commonmark.node.Heading
import ru.noties.markwon.AbstractMarkwonPlugin
import ru.noties.markwon.Markwon
import ru.noties.markwon.MarkwonSpansFactory
import ru.noties.markwon.core.MarkwonTheme

class SettingsFaqFragment: BoundFragment<FragmentSettingsFaqBinding>(FragmentSettingsFaqBinding::inflate), BackAvailable, AutoExpandOnRotate {

    override fun onCreate(savedInstanceState: Bundle?) {
        exitTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
        enterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
        returnTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
        reenterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium)
        val markwon = Markwon.builder(requireContext()).usePlugin(object: AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                typeface?.let {
                    builder.headingTypeface(it)
                    builder.headingBreakHeight(0)
                }
            }

            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                val origin = builder.requireFactory(Heading::class.java)
                builder.setFactory(Heading::class.java) { configuration, props ->
                    arrayOf(origin.getSpans(configuration, props), ForegroundColorSpan(requireContext().getColorResCompat(android.R.attr.textColorPrimary)))
                }
            }
        }).build()
        val markdown = requireContext().assets.open("faq.md").bufferedReader().use { it.readText() }
        binding.markdown.text = markwon.toMarkdown(markdown)
        ViewCompat.setOnApplyWindowInsetsListener(binding.markdown){ view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottomInset)
            insets
        }
        binding.root.enableStretchOverscroll()
    }

}