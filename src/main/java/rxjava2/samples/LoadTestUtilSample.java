package rxjava2.samples;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import rxjava2.samples.model.LoadTestMetricsData;
import rxjava2.samples.model.Response;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LoadTestUtilSample {

    public static void main(String[] args) throws InterruptedException {

       final LoadTestMetricsData loadTestMetricsData = callPeriodically(LoadTestUtilSample::httpCallFunction, 1000, 10);

        System.out.println("Seconds: " + loadTestMetricsData.duration.getSeconds());

        System.out.println("Size:" + loadTestMetricsData.responseList.size());

    }

    public static LoadTestMetricsData callPeriodically(final Supplier<Single<Response>> restCallFunction, long callnTimes, long callEveryMilisseconds) throws InterruptedException {

        final long start = System.currentTimeMillis();

        final List<Single<Response>> listOfSingleResponses = Collections.synchronizedList(new ArrayList<>());

        // final Observable<Long> timeSpace = Observable.interval(callEveryMilisseconds, TimeUnit.MILLISECONDS).take(callnTimes);

        for(int i = 0; i < callnTimes; i++) {

            Thread.sleep(callEveryMilisseconds);

            Single<Response> responseSingle = restCallFunction.get();
            listOfSingleResponses.add(responseSingle);
        }

        return Single.zip(listOfSingleResponses, (Object[] objects) -> {

            final LoadTestMetricsData loadTestMetricsData = new LoadTestMetricsData();

            final Response[] arrayOfResponses = Arrays.copyOf(objects, objects.length, Response[].class);

            loadTestMetricsData.responseList = Arrays.asList(arrayOfResponses);

            loadTestMetricsData.duration = Duration.ofMillis(System.currentTimeMillis() - start);

            return loadTestMetricsData;

        }).blockingGet();

    }

    public static Single<Response> httpCallFunction() {

        System.out.println("Calling http method...");

       return Single.fromCallable(() -> {
           Thread.sleep(1000);          // takes one second to get a response from the some remote service
            return new Response();
        }).subscribeOn(Schedulers.io());
    }

}
