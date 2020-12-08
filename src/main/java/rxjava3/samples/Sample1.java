package rxjava3.samples;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Sample1 {

    static final List<Integer> threadIds = Collections.synchronizedList(new ArrayList<Integer>());

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final long startTime = System.currentTimeMillis();

        System.out.println("Main thread: " + Thread.currentThread().getName());

        final int amountOfRequests = 1000;

        final Single<String> single = singleIsHere(amountOfRequests);

        final CompletionStage futureHasCome = futureIsHere(single);

        String result = (String) futureHasCome.toCompletableFuture().get();

        System.out.println("Result:" + result);

        final long endTime = System.currentTimeMillis();

        System.out.println( "Execution time: " + (endTime - startTime) / 1000 + "sec.");

        System.out.println("The amount of thread is " + Collections.max(threadIds) + " which were involved to handle the " + amountOfRequests + " amount of requests");

    }

    // In case if you need to convert it to CompletionStage. Say Rest Easy JAX-RS Resource uses the CompletionStage as return types
    public static CompletionStage<String> futureIsHere(Single<String> single) {

        final CompletableFuture<String> future = new CompletableFuture<>();
        single
                //.subscribeOn(Schedulers.io())
                .subscribe(future::complete);

        return future;

    }

    // Represent some Service call that uses RX Java 2
    public static Single<String> singleIsHere(int amountOfRequests) {

        final List<Integer> requestList = IntStream.rangeClosed(0, amountOfRequests).boxed().collect(Collectors.toList());

        final Flowable<String> relatedMaps = Flowable.range(0, requestList.size())
                .concatMapEager(index ->
                                fetchByHttp(requestList.get(index))
                                        .subscribeOn(Schedulers.io())
                                        .toFlowable(),
                        /* max parallel executions: */ requestList.size(),
                        /* items expected per source: */ 1
                );

        return relatedMaps.lastElement().toSingle();
    }

    public static Single<String> fetchByHttp(final int request)  {

        return Single.fromCallable( () -> {

                System.out.println("Fetch by Http Thread name: " + Thread.currentThread().getName()); // RxCachedThreadScheduler-N

                final Integer id = Integer.parseInt(Thread.currentThread().getName().split("-")[1]);

                 threadIds.add(id);

                try {
                    Thread.sleep(1000); // say it take 1 second to do the HTTP call
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return request + "";
        });//.subscribeOn(Schedulers.newThread());

    }

}
