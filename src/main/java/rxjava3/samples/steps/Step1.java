package rxjava3.samples.steps;

import io.reactivex.rxjava3.core.Observable;

public class Step1 {

    public static void main(String[] args) throws InterruptedException {

        var o = Observable.just(1);

        o.doOnComplete( () -> {
            System.out.println("o completed");
        });

        var oo = o.flatMap(x -> {
            Thread.sleep(100);
            return Observable.just(1 + 1);
        });




        oo.doOnComplete(() -> {
            System.out.println("completed");
            System.exit(0);
        }).subscribe(x -> {
            System.out.println("x:" + x);
        });

        Integer a = oo.blockingFirst();

        System.out.println(a);

        Thread.sleep(1000000);

    }

}
