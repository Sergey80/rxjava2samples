package rxjava2.samples.ff;


import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.Single;
import rxjava2.samples.ff.infrastructure.Client;
import rxjava2.samples.ff.infrastructure.rx.RxUtils;
import rxjava2.samples.ff.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified version of RxTest2
 * It maybe "too much" dealing with onErrorReturn after all
 *    the event then matching the input arguments and building up the results out of CompositeException.
 *
 *   It would be way simpler to handle error just after each Client.callClient() and build the result right away.
 *
 *   The only one thing is:
 *    - It is a bit less "functional style": we going to deal with common mutable state - the result, thus
 *    - the result.failures - should be synchronized collection. Otherwise your result will be messed up
 *       (in random fashion, sometimes giving you what you expect sometimes now. Could lead to very nasty bug)
 */
public class RxTest3 {

    public static void main(String[] args) {

        RxUtils.rxPluginSetup();

        final Result result = getResult(List.of(1,2,3,5,6,1,2,5))         // 5 will cause an exception
                                                            . blockingGet();

        System.out.println(result);

    }

    // application
    public static Single<Result> getResult(final List<Integer> ids) {

        final Result result = new Result();

        final List<Observable<String>> scheduledCalls = new ArrayList<>();

        final List<List<Integer>> idsChunks = Lists.partition(ids, 2);

        int title = 0;
        for (final List<Integer> chunk : idsChunks) {

            final Single<String> resultSingle = Client
                                                        .callClient(Integer.toString(title+1), chunk)

                                                        .doOnError(er -> {
                                                            System.out.println("1"); // will be invoked twice (sine we have two "5" in the input list)
                                                            result.failures.addAll(chunk);
                                                        });

            scheduledCalls.add(resultSingle.toObservable());
        }

        return Observable.mergeDelayError(scheduledCalls).map(x -> {
            result.success.add(x);
            return x;
        }).toList().flatMap(done -> {
            // if we had any error at all
            System.out.println("no errors:" + Thread.currentThread().getName());
            return Single.just(result);
        }).onErrorReturn(ex -> {
            // if we had at least one error
            System.out.println("some errors:" + Thread.currentThread().getName());
            System.out.println("2"); // yes this will be invoked once
            return result;
        });
    }
}
