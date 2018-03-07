package sirgl.graphics.core

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import javax.swing.JPanel

object FilterConfigurationChanged


interface AppFilters : ImageChangeListener {
    val filterPipeline: FilterPipeline
    val filterConfigurationObservable: Observable<FilterConfigurationChanged>
//    fun addFilter(filterModel: FilterModel)
//    fun clearFilters()
}

interface FFilters {
    fun filterConfigurationChanged()
}

class Filters : AppFilters, FFilters {
    override val filterConfigurationObservable: Observable<FilterConfigurationChanged> = SimpleObservable(FilterConfigurationChanged)

    val filtersObservable: Observable<MutableList<FilterModel>> = SimpleObservable(mutableListOf())
    override val filterPipeline = FilterPipeline(filtersObservable)
    val selectedFilterPanel: Observable<JPanel> = SimpleObservable<JPanel>(null)

    override fun notifyOriginalImageChanged() {
        filtersObservable.value?.forEach {
            it.dropBuffers()
        }
    }

    fun addFilter(filterModel: FilterModel) {
        val filters = filtersObservable.value ?: mutableListOf()
        filters.add(filterModel)
        filtersObservable.value = filters
    }

    fun clearFilters() {
        filtersObservable.value = mutableListOf()
    }

    override fun filterConfigurationChanged() {
        filterConfigurationObservable.value = FilterConfigurationChanged
    }
}