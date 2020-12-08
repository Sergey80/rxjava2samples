package rxjava3.samples.ff.infrastructure.rx;


import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

import java.io.IOException;
import java.net.SocketException;

public class RxUtils {

    static public void rxPluginSetup() {

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
                //return;
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application

                return;
            }
            if (e instanceof IllegalStateException) {

                return;
            }
            if(e instanceof CompositeException) {
                return;
            }

            //Log.warning("Undeliverable exception received, not sure what to do", e);
        });

    }
}
