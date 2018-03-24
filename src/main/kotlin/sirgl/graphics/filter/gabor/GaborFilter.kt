package sirgl.graphics.filter.gabor

import sirgl.graphics.filter.KernelInfo
import sirgl.graphics.filter.PerChanelMatrixFilter
import sirgl.graphics.observable.Observable

class GaborFilter(kernelObservable: Observable<KernelInfo>) : PerChanelMatrixFilter(kernelObservable) {

}