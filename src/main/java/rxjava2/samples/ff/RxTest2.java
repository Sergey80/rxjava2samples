package rxjava2.samples.ff;


import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import rxjava2.samples.ff.infrastructure.Client;
import rxjava2.samples.ff.infrastructure.ClientCommunicationException;
import rxjava2.samples.ff.infrastructure.rx.RxUtils;
import rxjava2.samples.ff.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Given:
 *  calling Client.callClient(title, args[]) by chunk of of ars - by 2 for example
 *  at some moment callClient() fails (with argument 5 in our case), as result all chain of calls fails.
 *
 *  The intention to know what chunk got failed and collect it into Result
 *
 *  Solution:
 *   - convert and collect result from Client.callClient() from Single to Observable
 *   - use Observable.mergeDelayError to let the calls go over all successful outcomes and collect Error to CompositeException and call onError in the the end, once
 *   - handling CompositeException in onErrorReturn fills the Result with what were failing
 *   - the result was printer without interuptings
 */
public class RxTest2 {

    public static void main(String[] args) {

        RxUtils.rxPluginSetup();

        final Result result = get(List.of(1,2,3,5,6,1,2,5))
                                        . blockingGet();

        System.out.println(result);

    }

    // application
    public static Single<Result> get(final List<Integer> ids) {

        final Result result = new Result();

        final List<Observable<String>> scheduledCalls = new ArrayList<>();

        final List<List<Integer>> idsChunks = Lists.partition(ids, 2);

        int title = 0;
        for (final List<Integer> chunk : idsChunks) {
            final Single<String> resultSingle = Client.callClient(Integer.toString(title+1), chunk);
            scheduledCalls.add(resultSingle.toObservable());
        }

        return Observable.mergeDelayError(scheduledCalls).map(x -> {
            result.success.add(x);
            return x;
        }).toList().flatMap(done -> {
            return Single.just(result);
        }).onErrorReturn(ex -> {
            if(ex instanceof io.reactivex.exceptions.CompositeException) {
                io.reactivex.exceptions.CompositeException compositeException = (CompositeException) ex;
                final List<Throwable> throwableList = compositeException.getExceptions();
                throwableList.forEach(er -> {
                    if(er instanceof ClientCommunicationException) {
                        final ClientCommunicationException cce = (ClientCommunicationException) er;
                        cce.getFailedIds().forEach(id -> {
                            result.failures.add(id);
                        });

                    }
                });
            }
            return result;
        });
    }
}
