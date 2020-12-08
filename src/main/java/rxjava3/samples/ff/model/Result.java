package rxjava3.samples.ff.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {

    public Result() {}
    public List<String> success = new ArrayList<>();
    public List<Integer> failures = Collections.synchronizedList(new ArrayList<>());

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", failures=" + failures +
                '}';
    }
}
