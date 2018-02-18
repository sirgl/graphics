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