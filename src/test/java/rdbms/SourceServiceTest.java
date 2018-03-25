package rdbms;

import entity.Call;
import entity.CarWash;
import entity.Subscriber;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SourceServiceTest {
    private SourceService sourceService = new SourceServiceExampleImpl();

    @Test
    public void testCalls() {
        LocalDateTime dt = LocalDateTime.now();

        ArrayList<Call> calls = (ArrayList<Call>) sourceService.getCalls(dt.minusDays(10), dt);

        assertNotNull("calls shouldn't be null", calls);
        assertFalse("calls shouldn't be empty with correct date", calls.isEmpty());

        calls = (ArrayList<Call>) sourceService.getCalls(dt.minusYears(100), dt.minusYears(99));
        assertTrue("calls should be empty with incorrect date", calls.isEmpty());
    }

    @Test
    public void testSubscribers() {
        List<Subscriber> subscribers = sourceService.getSubscribers();

        assertNotNull("subscribers shouldn't be null", subscribers);
        assertFalse("subscribers shouldn't be empty", subscribers.isEmpty());
    }

    @Test
    public void testCarWash() {
        List<CarWash> carWashes = sourceService.getCarWashes();

        assertNotNull("carWashes shouldn't be null", carWashes);
        assertFalse("carWashes shouldn't be empty with correct date", carWashes.isEmpty());
    }
}
