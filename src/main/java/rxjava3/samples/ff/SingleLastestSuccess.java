package rxjava3.samples.ff;


//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.core.Single;
//
//import java.util.NoSuchElementException;
//import java.util.concurrent.atomic.AtomicReference;
//
//public class SingleLastestSuccess {
//
//    public static void main(String[] args) {
//
//    }
//
//    public static <T> Single<T> latestSuccess(final Single<T>... sources) {
//
//        return Single.defer(() -> {
//            AtomicReference<T> last = new AtomicReference<T>();
//            return Observable.fromArray(sources)
//                    .concatMap(source ->
//                            source.doOnSuccess(last::lazySet)
//                                    .toObservable()
//                                    .onErrorResumeNext(Observable.empty())
//                    )
//                    .ignoreElements()
//                    .andThen(Single.fromCallable(() -> {
//                        if (last.get() == null) {
//                            throw new NoSuchElementException();
//                        }
//                        return last.get();
//                    }));
//        });
//    }
//}
