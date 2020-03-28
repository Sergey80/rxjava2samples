package rxjava2.samples.ff;


import com.google.common.collect.Lists;
import io.reactivex.Single;
import io.reactivex.internal.operators.single.SingleError;
import rxjava2.samples.ff.infrastructure.Client;
import rxjava2.samples.ff.infrastructure.rx.RxUtils;
import rxjava2.samples.ff.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Given:
 *  calling Client.callClient(title, args[]) by chunk of of ars - by 2 for example
 *  at some moment callClient() fails (with argument 5 in our case), as result all chain of calls fails.
 *  This sample demoes it.
 *  The intention to know what chunk got failed and collect it into Result
 */
public class RxTest1 {

    public static void main(String[] args) {

        RxUtils.rxPluginSetup();    // this helps use to pass proper exception to doOnError below:

        final Result result = get(List.of(1,2,3,5,5,6)).doOnError(ex -> { // uses RxUtils.rxPluginSetup() got get proper exception (and reduce a stack trace for uncaught exception)
            System.out.println("-->" + ex);
        }).blockingGet();

        System.out.println(result);

    }

    // application
    public static Single<Result> get(final List<Integer> ids) {

        final Result result = new Result();

        final List<Single<String>> scheduledCalls = new ArrayList<>();

        final List<List<Integer>> idsChunks = Lists.partition(ids, 2);

        int title = 0;
        for (final List<Integer> chunk : idsChunks) {
            final Single<String> resultSingle = Client.callClient(Integer.toString(title+1), chunk);
            scheduledCalls.add(resultSingle);
        }

        return SingleError.merge(scheduledCalls).map(x -> {
            result.success.add(x);
            return x;
        }).lastElement().toSingle().flatMap(done -> {
            return Single.just(result);
        });
    }
}
