package rxjava3.samples.steps.step2;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

/*

  concatMap
    concatMap does not subscribe to the next observable until the previous completes, the value from the source delayed by NNNms will be emitted first.

 switchMap
     The main difference between switchMap and other flattening operators is the cancelling effect.
     On each emission the previous inner observable (the result of the function you supplied) is cancelled and the new observable is subscribed.
     You can remember this by the phrase switch to a new observable.
     This works perfectly for scenarios like typeaheads where you are no longer concerned with the response of the previous request when a new input arrives.

  flatMap vs concatMap

  FlatMap takes emissions from source observable,
        then create new observable and _merge_ it to original chain,
        while concatMap               _concat_ it to original chain.

  Main difference is that concatMap() will merge each mapped Observable _sequentially_ and fire it one at a time.
  It will only move to the next Observable when the current one calls onComplete().
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

    public Observable<Response> call(int arg) {

        System.out.println("Calling service 1 ...");

        return Observable.create( (ObservableEmitter<Response> emitter) -> {

            // if these items were to be emitted a the same time (commented sleep(1000) ) (and we use switchMap (see below)

//            Thread.sleep(1000);
            emitter.onNext(new Response(1));

//            Thread.sleep(1000);
            emitter.onNext(new Response(2));

//            Thread.sleep(1000);
            emitter.onNext(new Response(3));

//            Thread.sleep(1000);
            emitter.onNext(new Response(4));

            emitter.onComplete();

        }).doOnNext(response -> {
            System.out.println("Service 1 has returned with the response " + response);
        });
    }

}

class Service2 {

    public Observable<Response> call(int arg) {

        System.out.println("Calling service 2 with " + arg);

        if ( arg % 2 == 0) {

//            System.out.println("service 2: " + arg);

            return Observable.create( (ObservableEmitter<Response> emitter) -> {

                Thread.sleep(1000);
                emitter.onNext(new Response(100 * arg));

                Thread.sleep(1000);
                emitter.onNext(new Response(200 * arg));

                emitter.onComplete();

            }).doOnNext(response -> {
                System.out.println("Service 2 has returned with the response " + response);
            });

        } else {

//            System.out.println("service 2: " + arg);

            return Observable.error(new RuntimeException("service 2 error"));
        }
    }
}

public class Step2 {

    static Service1 service1 = new Service1();
    static Service2 service2 = new Service2();

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        var responsesObs1 = service1.call(1);



        final Observable<Response> combinedObs = responsesObs1  // NOTE: not of these "maps" do any parallel call
                .flatMap(response1 -> {
//                .concatMap(response1 -> {   // concatMap does not subscribe to the next observable until the previous completes, the value from the source delayed by NNNms will be emitted first.
//                .switchMap(response1 -> {     // difference between switchMap and other flattening operators is the cancelling effect.

            final Observable<Response> oo2 = service2.call(response1.value);

            return oo2
                    .onErrorReturn((ex) -> {
                        //System.out.println("Error handling..." + ex.getMessage() + " " + response);
                        response1.warning = ex.getMessage();
                        return response1; // returns at least service1 result
                    });
        }).subscribeOn(Schedulers.newThread());

        combinedObs.doOnComplete(() -> {
            long end = System.currentTimeMillis();

            System.out.println("Took time:" + (end - start));

            System.exit(0);
        });

        combinedObs
        .doOnComplete(() -> {

            long end = System.currentTimeMillis();

            System.out.println("Took time:" + (end - start));

            System.exit(0);
        })
        .subscribe(x -> {
//            System.out.println("\nFinal response: " + x + "\n");
        });


        Thread.sleep(100000);

    }

}
