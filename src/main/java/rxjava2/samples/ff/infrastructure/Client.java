package rxjava2.samples.ff.infrastructure;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.List;

public class Client {

    /*
     *
    */
    public static Single<String> callClient(final String title, final List<Integer> ids) {

        return Single.fromCallable(() -> {

            System.out.println("Calling " + title + " with " + ids);

            Thread.sleep(1000);

            if (ids.contains(5)) {
                throw new ClientCommunicationException(ids);
            }

            return "" + ids;

        }).subscribeOn(Schedulers.io());
    }
}
