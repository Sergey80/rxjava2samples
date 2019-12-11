package rxjava2.samples.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LoadTestMetricsData {

    public List<Response> responseList = new ArrayList<>();
    public Duration duration;

    @Override
    public String toString() {
        return "LoadTestMetricsData{" +
                "responseList=" + responseList +
                ", duration=" + duration +
                '}';
    }
}
