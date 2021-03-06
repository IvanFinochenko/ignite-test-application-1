package ignite;

import entity.Call;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.junit.Before;
import org.junit.Test;
import rdbms.SourceService;
import rdbms.SourceServiceExampleImpl;
import system.Parameters;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IgniteApplicationTest {
    Parameters parameters;
    SourceService sourceService;
    IgniteApplication igniteApplication;
    IgniteSourceService igniteSourceService;

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        String[] args = new String[1];
        args[0] = "2018-03-25";

        parameters = Parameters.getInstance(args);
        sourceService = new SourceServiceExampleImpl();
    }

    @Test
    public void testApp() throws SQLException {
        try (Ignite ignite = Ignition.start()) {
            igniteApplication = new IgniteApplication(ignite, parameters);
            igniteSourceService = new IgniteSourceServiceImpl(ignite, sourceService, parameters);
            igniteSourceService.createCachesAndInsert();

            igniteApplication.setupCashes();
            IgniteCache subscriberCache = igniteApplication.getSubscriberCache();
            IgniteCache callCache = igniteApplication.getCallCache();
            IgniteCache carWashCache = igniteApplication.getCarWashCache();
            IgniteCache carWashUsersCache = igniteApplication.getCarWashUsersCache();

            //igniteApplication.insertData();

            assertFalse("Subscriber table is empty",
                    subscriberCache.size(CachePeekMode.ALL) == 0);

            assertFalse("Call table is empty",
                    callCache.size(CachePeekMode.ALL) == 0);

            assertFalse("CarWash table is empty",
                    carWashCache.size(CachePeekMode.ALL) == 0);

            assertTrue("CarWashUser table isn't empty",
                    carWashUsersCache.size(CachePeekMode.ALL) == 0);

            igniteApplication.insertCarWashUser();
            int cntUsersBefore = carWashUsersCache.size(CachePeekMode.ALL);
            assertFalse("CarWashUser table is empty", cntUsersBefore == 0);

            int cntCallsBefore = callCache.size(CachePeekMode.ALL);
            addValidValueToCallsTable(callCache);
            int cntCallsAfter = callCache.size(CachePeekMode.ALL);
            assertTrue("Number of Calls should be increased by 1",
                    cntCallsBefore + 1 == cntCallsAfter);

            carWashUsersCache.clear();
            igniteApplication.insertCarWashUser();
            int cntUsersAfter = carWashUsersCache.size(CachePeekMode.ALL);
            assertTrue("Number of Carwash users should be increased by 1 after adding valid call",
                    cntUsersBefore + 1 == cntUsersAfter);

            carWashUsersCache.clear();
            igniteApplication.insertCarWashUser();
            int cntUsersAfterWrondCalls = carWashUsersCache.size(CachePeekMode.ALL);
            assertTrue("Number of Carwash users should be equal value before, " +
                            "because wrong values were added into Calls ",
                    cntUsersAfter == cntUsersAfterWrondCalls);
        }
    }

    private void addValidValueToCallsTable(IgniteCache callCache) {
        //valid call
        Call call = new Call(89202550011L, 88002553534L, 61, LocalDateTime.now().minusDays(5));
        callCache.put(call.id, call);
    }

    private void addNotValidValuesToCallsTable(IgniteCache callCache) {
        LocalDateTime dt = LocalDateTime.now().minusDays(5);
        //dur < 60
        Call call1 = new Call(89202550011L, 88002553534L, 59, LocalDateTime.now().minusDays(5));

        //subs_to is wrong
        Call call2 = new Call(89202550011L, 80000000000L, 70, LocalDateTime.now().minusDays(5));

        //future time
        Call call3 = new Call(89202550011L, 80000000000L, 59, LocalDateTime.now().minusDays(5));

        callCache.put(call1.id, call1);
        callCache.put(call2.id, call2);
        callCache.put(call2.id, call2);

    }
}
