package single2flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

public class SingleToFlowable {

    public static void main(String[] args) {

        var list = List.of(1,2,3);
        var listSingle = Single.just(list);
        var flowable = Flowable.fromSingle(listSingle).flatMap(lst -> Flowable.fromIterable(lst));

        var result = flowable.toList().blockingGet();

        System.out.println(result);

    }
}
