package rxjava3.samples.steps;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

// Showing the Switching Map (`problem`)

public class Step1_SwitchMapProblem {

    public static void main(String[] args) throws InterruptedException {

        Observable<Integer> outerObs = Observable.just(1,2);

        outerObs.doOnComplete( () -> {
            System.out.println("o completed");
        });

        // On each upstream emit switchMap subscribes to given inner-stream.
        // When a new value is emitted from upstream,
        // the old inner-stream gets unsubscribed and the lambda of switchMap is called again in order to subscribe to the new inner-stream.

        Observable<Integer> resultObs =
                outerObs
//                .flatMap(x -> {
                .switchMap(x -> {                                // try to un-comment the switchMap -

            var innerObs = Observable.just(x + 1)
                    .delay(100, TimeUnit.MILLISECONDS)  // delay() matters - this delay makes this observable be subscribed for new values almost at exact same time, like 1607462278332, 1607462278334 milliseconds
                    ;

            innerObs.subscribe(xx -> {
                System.out.println("received value at " + Schedulers.io().now(TimeUnit.MILLISECONDS));

            });
            return innerObs;
        });


        // upstream:        1,               2

        // inner-stream:     \_ 1 + 1 = 2      \_ 2 + 1 = 3
        //                      -------10 ms -----> |

        //                     1 -> (1 + 1) inner stream got unsubscribed, because there is upper stream with new value 2 get started before (1+1) is finished




        resultObs.doOnComplete(() -> {
            System.out.println("completed");
            System.exit(0);
        }).subscribe(x -> {
            System.out.println("x:" + x);
        });

        Thread.sleep(1000000);

    }

}

/*
Outputs:

/*
 * flatMap:
 *
 * x:2
 * x:3
 * completed

 */


/* switchMap:
*
* x:3
* completed

// x2: is missed! (depends on delay(), see above ). delay x2 is missed because it was canceled (because switchMap indeed has switched to another value)
*/
