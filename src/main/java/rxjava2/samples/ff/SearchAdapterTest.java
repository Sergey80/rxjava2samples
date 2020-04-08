package rxjava2.samples.ff;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// The idea is. There is a main Search Service that performs search.
// And there is SearchEnhancer that enhance what Search Service result.

// Here everything is simplified but the idea of using SearchEnhancer is to search data from another source (not where Search Service searches),
// convert data back to Search Service response format as if SearchEnhancer did all by itself.
// SearchEnhancer has to be run for every chunk of Search Service response. Non blocking.

// Use PublishSubject as a flag to make sure all PublishSubject's work was submitted and nothing is missed.

class SearchEnhancer {

    public Single<String> enhance(final String result) {

        return Single.fromCallable( () -> {
            Thread.sleep(1000);
            return "enhanced result for " + result;
        }).subscribeOn(Schedulers.io());

    }
}

class SearchService {

    public Single<String> search(final String param) {

        return Single.fromCallable( () -> {
            Thread.sleep(100);
            return "search result for for " + param;
        }).subscribeOn(Schedulers.io());

    }
}


public class SearchAdapterTest {

    public static void main(String[] args) throws InterruptedException {

        final List<SearchEnhancer> searchEnhancerList = List.of(
                new SearchEnhancer()
        );

        Single<String> result = search(searchEnhancerList);

        String r = result.blockingGet();

        System.out.println("Final result:" + r);

        Thread.sleep(10000);
    }

    public static Single<String> search( final List<SearchEnhancer> searchEnhancerList) {


        SearchService searchService = new SearchService();

        final List<Observable<String>> searchResultsObs1 = new ArrayList<>();
        final List<Observable<String>> searchResultsObs2 = new ArrayList<>();

        final PublishSubject<Boolean> flag = PublishSubject.create();

        final AtomicInteger submittedEnhancedSearchesCountFlag = new AtomicInteger();
        final AtomicInteger expectedEnhancedSearchesCount = new AtomicInteger(3);

        for (int i = 0; i < 3; i++) { // amount of call to basic search

            final Observable<String> searchResult = searchService.search("" + i).toObservable();

            if(false) { // if precedent base search was not successful
                expectedEnhancedSearchesCount.decrementAndGet();
            }

            // enh
            searchResult.subscribe(result -> {

                searchEnhancerList.forEach(searchEnhancer -> {
                    final Observable<String> result2 = searchEnhancer.enhance(result).toObservable();
                    searchResultsObs2.add(result2);

                    if( submittedEnhancedSearchesCountFlag.incrementAndGet() == expectedEnhancedSearchesCount.get()) {
                        flag.onNext(true);
                        flag.onComplete();
                    }
                });

                System.out.println(result);
            });

            searchResultsObs1.add(searchResult);

        }

        final Single<String> resultSingle1 = transform(searchResultsObs1, "base search");

        final Observable transformedObservable2 = flag.flatMap((Boolean done) -> { // wait fo the flag to be true/done
            final Single<String> resultSingle2 = transform(searchResultsObs2 , "enhanced search");
            return resultSingle2.toObservable();
        });



        final Single transformedSingle1 = resultSingle1.map(result -> {
            return result;
        });



         return Single.zip(transformedSingle1, Single.fromObservable(transformedObservable2), (transformedResult1, transformedResult2) -> {
             return transformedResult1 + "," + transformedResult2;
         });

    }

    public static Single<String> transform(final List<Observable<String>> observableList, String title) {

        System.out.println("transforming " + title);

          final StringBuilder apolloAdaptedSearchResponseTop = new StringBuilder();

          final Single<String> apolloAdaptedSearchResponseSingle =
          Observable.mergeDelayError(observableList).map( responseAdapted -> {

              apolloAdaptedSearchResponseTop.append(responseAdapted + ",");

              return apolloAdaptedSearchResponseTop.toString();

          }).lastElement().toSingle().flatMap(apolloAdaptedSearchResponse -> {
              // if we did not have any errors
              final String result = "[" + apolloAdaptedSearchResponse.toString() + "]";
              return Single.just(result);
          }).onErrorReturn(ex -> {
              // if we had at least one error (will be called only once)
              return null;  // TODO: add error result anyways
          });

          return apolloAdaptedSearchResponseSingle;
      }

}
