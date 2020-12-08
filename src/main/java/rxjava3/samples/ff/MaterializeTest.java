package rxjava3.samples.ff;


import io.reactivex.rxjava3.core.Observable;

import java.util.List;

public class MaterializeTest {

    public static void main(String[] args) {

        final Observable<String> stringObservable = Observable.fromArray("1", "2", "3")
                .flatMap(x -> {
                    if (x.equals("2")) {
                        return Observable.<String>error(new NullPointerException())
                                .materialize();
                    }

                    return Observable.just(x)
                            .materialize();
                })
                .filter(n -> n.isOnNext())
                .map(n -> n.getValue());

        List<String> a = stringObservable.toList().doOnError(x -> {
            System.out.println(x);
        }).blockingGet();

        System.out.println(a);

    }

}
