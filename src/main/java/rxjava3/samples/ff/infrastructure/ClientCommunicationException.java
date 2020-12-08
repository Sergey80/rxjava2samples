package rxjava3.samples.ff.infrastructure;

import java.util.List;

public class ClientCommunicationException extends RuntimeException {

    List<Integer> failedIds;

    public ClientCommunicationException(final List<Integer> ids) {
        super("");
        this.failedIds = ids;
    }

    public List<Integer> getFailedIds() {
        return failedIds;
    }
}
