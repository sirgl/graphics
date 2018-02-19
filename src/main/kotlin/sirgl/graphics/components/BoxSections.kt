package sirgl.graphics.components

import java.awt.GridBagConstraints


// Only vertical
class BoxSections {
    private val sections = mutableListOf<BoxSection>()

    fun addSection(builder: BoxSection.() -> Unit) {
        val section = BoxSection()
        builder(section)

    }
}

class BoxSection {
    val constraint = GridBagConstraints()
    init {
    }
    
    fun setPosition() {
        
    }
}

fun boxSections() {

}