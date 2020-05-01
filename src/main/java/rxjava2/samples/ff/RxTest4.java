package rxjava2.samples.ff;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class RxTest4 {

    public static Single<Integer> get(Integer request) {
        return Single.fromCallable( () -> {
            Thread.sleep(1000);
            return request;
        }).subscribeOn(Schedulers.io());
    }

    public static void main(String[] args) throws InterruptedException {


        final List<Observable<Integer>> responseList = new ArrayList<>();

        for(int i=0; i< 2; i++) {
            responseList.add(get(i).toObservable());
        }

        final List<Integer> rr = new ArrayList<>();

        final Single<List<Integer>> ff = Observable.mergeDelayError(responseList).map(x -> {

            System.out.println("adding " + x);

            return rr.add(x);

        }).lastElement().toSingle().flatMap(xx -> {
            System.out.println("result:");
            return Single.just(rr);
        });

        List<Integer> rrr = ff.blockingGet();

        System.out.println(rrr);

    }
}
