package RSP.REST;

import RSP.dto.SortAttribute;
import RSP.dto.SortOrder;
import RSP.dto.TripsQueryCriteria;
import RSP.model.Trip;
import RSP.service.InconsistentQueryException;
import RSP.service.InvalidQueryException;
import RSP.service.TripNotFoundException;
import RSP.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/trips")
public class TripController {

    TripService tripService;

    private final static Logger log = Logger.getLogger(TripController.class.getName());

    TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<Trip> getAll(TripsQueryCriteria criteria)
            throws InvalidQueryException, InconsistentQueryException {
        log.info(() -> "REST GET /trips invoked with " + criteria);
        List<Trip> results = tripService.getSome(criteria);
        log.info(() -> "REST GET /trips returned OK with " + results.size() + " trips");
        return results;
    }

    @GetMapping(value = "/sort", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Trip> getAllSorted(@RequestParam SortAttribute by, @RequestParam SortOrder order) {
        log.info("path: /trips/sort GET method getAllSorted is invoked by " + by + " order " + order);
        return tripService.getAllSorted(by, order);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Trip get(@PathVariable int id) throws TripNotFoundException {
        log.info("path: /trips/{id} GET method get is invoked where id = " + id);
        return tripService.get(id);
    }

    @GetMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    Trip get(@PathVariable String name) throws TripNotFoundException {
        log.info("path: /trips/name/{name} GET method get is invoked where name = " + name);
        return tripService.getByName(name);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Trip> add(@RequestBody Trip trip) throws URISyntaxException {
        log.info("path: /trips POST method add is invoked");
        Trip old = tripService.add(trip);
        if (old == null) {
            return ResponseEntity
                    .created(new URI("/trips/" + trip.getId()))
                    .body(trip);
        } else {
            log.info("path: /trips POST method add is invoked with error CONFLICT");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .header("Content-Location", "/trips/" + old.getId())
                    .body(old);
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    void remove(@PathVariable int id) throws TripNotFoundException {
        log.info("path: /trips DELETE method remove is invoked with id = " + id);
        tripService.remove(id);
    }

    // BULK OPERATIONS

    /**
     * Returns an array with all trips in database.
     */
    @GetMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Trip> getBulk() {
        log.info("REST GET /trips/bulk invoked");
        List<Trip> result = tripService.getAll();
        log.info(() -> "REST GET /trips/bulk returned OK with " + result.size() + " trips");
        return result;
    }

    /**
     * Adds all trips from array into database.
     *
     * @param trips array of trips without IDs
     * @return OK with an array of added trips including their assigned IDs
     *         or CONFLICT with an array of existing trips with conflicting names.
     */
    @PostMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Trip>> postBulk(@RequestBody List<Trip> trips) {
        log.info(() -> "REST POST /trips/bulk invoked with " + trips.size() + " trips");
        List<Trip> old = tripService.addAll(trips);
        if (old.isEmpty()) {
            log.info(() ->
                    "REST POST /trips/bulk returned OK with " + trips.size() + " trips");
            return ResponseEntity
                    .created(URI.create("/trips"))
                    .body(trips);
        } else {
            log.info(() ->
                    "REST POST /trips/bulk returned CONFLICT with " + old.size() + "trips");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .header("Content-Location", "/trips")
                    .body(old);
        }
    }

    /**
     * Replaces content of database with trips from array.
     *
     * @param trips array of trips without IDs
     * @return OK with an array of all trips including their assigned IDs
     *         or CONFLICT with an array of requested trips that could not be added.
     */
    @PutMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Trip>> putBulk(@RequestBody List<Trip> trips) {
        log.info(() -> "REST PUT /trips/bulk invoked with " + trips.size() + " trips");
        List<Trip> bad = tripService.setAll(trips);
        if (bad.isEmpty()) {
            log.info(() -> "REST PUT /trips/bulk returned OK with " + trips.size() + " trips");
            return ResponseEntity
                    .created(URI.create("/trips"))
                    .body(trips);
        } else {
            log.info(() ->
                    "REST PUT /trips/bulk returned CONFLICT with " + bad.size() + " trips");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(bad);
        }
    }

    /**
     * Update content of database with trips from array.
     * Trips from array replace existing trips with same name or are added into database
     * if database does not contain trip with such name.
     *
     * @param trips array of trips without IDs
     * @return OK with an array of added or updated trips with IDs
     */
    @PatchMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Trip> patchBulk(@RequestBody List<Trip> trips) {
        log.info(() -> "REST PATCH /trips/bulk invoked with " + trips.size() + " trips");
        List<Trip> result = tripService.updateAll(trips);
        log.info(() -> "REST PATCH /trips/bulk returned OK with " + result.size() + " trips");
        return result;
    }

    /**
     * Delete database of trips.
     */
    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBulk() {
        log.info("REST DELETE /trips/bulk invoked");
        tripService.removeAll();
        log.info("REST DELETE /trips/bulk returned NO_CONTENT");
    }

    // EXCEPTIONS

    @ExceptionHandler(TripNotFoundException.class)
    void handleTripNotFound(HttpServletResponse response, Exception exception)
            throws IOException {
        log.info(() -> "REST returned NOT_FOUND with error: " + exception.getMessage());
        response.sendError(HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler({InconsistentQueryException.class, InvalidQueryException.class})
    void handleInvalidQuery(HttpServletResponse response, Exception exception)
            throws IOException {
        log.info("REST returned UNPROCESSABLE_ENTITY with error: " + exception.getMessage());
        response.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
