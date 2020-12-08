package rxjava3.samples.steps.step3;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxjava3.samples.steps.LogUtil;

import java.util.ArrayList;
import java.util.List;

/*
  + Service2 renamed to EnhancerService
  + there are many EnhancerServices that meant to Enhance the Service 1 response
  + Service1 to return Single (not Observable)
  + move main code to getData() function
*/

class Response {

    public Integer value;
    public String warning;
    public Response(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Response{" +
                "value=" + value +
                ", warning='" + warning + '\'' +

                '}';
    }
}

class Service1 {

    public Single<Response> call(final int arg) {

        LogUtil.logWithCurrentTime("Calling service 1 ...");

        return Single.fromCallable(() -> {
            return new Response(arg);
        }).doOnSuccess(response -> {
            LogUtil.logWithCurrentTime("Service 1 has returned with the Original Response " + response);
        }).subscribeOn(Schedulers.io());
    }

}

class EnhancerService {

    String name;
    int factor = 100;
    long delayMilliseconds = 1000;

    public EnhancerService(final String name, int factor, long delayMilliseconds) {
        this.name = name;
        this.factor = factor;
        this.delayMilliseconds = delayMilliseconds;
    }

    public Observable<Response> call(int arg) {

        LogUtil.logWithCurrentTime("Calling " + name + " service with " + arg);

        if ( arg % 2 == 0) {                                                      // make it fail for not ODD values

            return Observable.create( (ObservableEmitter<Response> emitter) -> {

                Thread.sleep(delayMilliseconds);
                emitter.onNext(new Response(factor * arg));

                Thread.sleep(delayMilliseconds);
                emitter.onNext(new Response(factor * arg + 1));

                emitter.onComplete();

            }).doOnNext(response -> {

                LogUtil.logWithCurrentTime("Service" + name + " has returned with the response " + response);
            });

        } else {

            return Observable.error(new RuntimeException("service 2 error"));
        }
    }
}

public class Step3 {

    static Service1 service1 = new Service1();

    static List<EnhancerService> enhancerServices = List.of(
                                                    new EnhancerService("enhancer-1", 100, 1000),
                                                    new EnhancerService("enhancer-2", 200, 1000)
                                                    );

    public static void main(String[] args) throws InterruptedException {


        getData(1)              // try with 1(fail), 3(fail), 4 !
                .doOnNext(next -> {
                    LogUtil.logWithCurrentTime("next: " + next);
                })
                .doOnComplete(() -> {
                   System.exit(0);
               }).subscribe();

        Thread.sleep(100000);

    }

    public static Observable<Response> getData(int input) {

        var responsesObs1 = service1.call(input).toObservable();

        final Observable<Response> combinedObs = responsesObs1
                .flatMap(response1 -> {

                    final List<Observable<Response>> responseList = new ArrayList<>();

                    for (final EnhancerService enhancerService : enhancerServices) {
                        responseList.add( enhancerService.call(response1.value));
                    }

                    final Observable<Response> combinedResults = Observable.mergeDelayError(responseList);  // will not it fail if any of enhancerService fails, to collect as much data as possible

                    return combinedResults
                            .onErrorReturn((ex) -> {

                                LogUtil.logWithCurrentTime("Error handling for " + response1);

                                response1.warning = ex.getMessage();
                                return response1; // returns at least service1 result
                            });
                }).subscribeOn(Schedulers.newThread());

        return combinedObs;
    }

}
