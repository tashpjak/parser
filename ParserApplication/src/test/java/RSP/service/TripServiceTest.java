package RSP.service;

import RSP.ParserApplicationBackend;
import RSP.model.Trip;
import generator.Generator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ComponentScan(basePackageClasses = ParserApplicationBackend.class)
public class TripServiceTest {

    @Autowired
    private TripService tripService;

    @Test
    public void AddTrip() throws ParseException {
        final Trip trip = Generator.generateTrip();
        tripService.add(trip);

        assertTrue(tripService.nameExists(trip.getName()));
    }

    @Test
    public void RemoveTrip() throws ParseException, TripNotFoundException {
        final Trip trip = Generator.generateTrip();
        tripService.add(trip);
        assertTrue(tripService.idExists(trip.getId()));

        tripService.remove(trip.getId());
        assertFalse(tripService.idExists(trip.getId()));
    }

    @Test
    public void getByNameTest() throws ParseException, TripNotFoundException {
        Trip t = Generator.generateTrip();
        tripService.add(t);

        assertTrue(tripService.idExists(t.getId()));

        Trip test = tripService.get(t.getId());

        Trip actualName = tripService.getByName(t.getName());

        assertEquals(test.getName(), actualName.getName());

    }
}
