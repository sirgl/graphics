package sirgl.graphics.observable

interface Observable<T> {
    fun subscribe(listener: Listener<T>)
    var value: T?
}

typealias Listener<T> = (T?) -> Unit

class SimpleObservable<T>(v: T?) : Observable<T> {
    constructor(another: Observable<T>, mapOp: ((T?) -> T?)? = { it }) : this(another.value) {
        mapOp ?: return
        another.subscribe {
            value = mapOp(it)
        }
    }

    private val listeners = mutableListOf<Listener<T>>()

    override fun subscribe(listener: Listener<T>) {
        listeners.add(listener)
    }

    override var value: T? = v
        set(value) {
            field = value
            notifyListeners(value)
        }

    private fun notifyListeners(next: T?) {
        for (listener in listeners) {
            listener(next)
        }
    }
}

fun <T> Observable<T>.transmitTo(another: Observable<T>) {
    this.subscribe {
        another.value = it
    }
}

fun <T1, T2> Observable<T1>.map(transform: (T1?) -> T2?): Observable<T2> {
    val observable = SimpleObservable(transform(value))
    subscribe {
        observable.value = transform(it)
    }
    return observable
}

fun <T> Observable<T>.filter(filterOp: (T?) -> Boolean): Observable<T> {
    val observable = SimpleObservable(value)
    subscribe {
        if (filterOp(it)) {
            observable.value = it
        }
    }
    return observable
}

fun <T1> Observable<T1>.printValue() = map { println(it);it }

class NamedObservable<T>(observable: Observable<T>, val name: String) : Observable<T> by observable

fun <T> Observable<T>.named(name: String) = NamedObservable(this, name)

fun <T> Observable<T>.refresh() {
    value = value
}