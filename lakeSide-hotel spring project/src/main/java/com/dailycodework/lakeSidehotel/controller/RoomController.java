package com.dailycodework.lakeSidehotel.controller;
import com.dailycodework.lakeSidehotel.exception.PhotoRetrievalException;
import com.dailycodework.lakeSidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakeSidehotel.model.BookedRoom;
import com.dailycodework.lakeSidehotel.model.Room;
import com.dailycodework.lakeSidehotel.response.BookingResponse;
import com.dailycodework.lakeSidehotel.response.RoomResponse;
import com.dailycodework.lakeSidehotel.service.BookingService;
import com.dailycodework.lakeSidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5174")
@RequestMapping("/rooms")  // Keep /rooms as the base path
public class RoomController {

    private final IRoomService roomService;
    private final BookingService bookingService;

    // Constructor for dependency injection
    public RoomController(IRoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    // Adding a new room
    @PostMapping("/add/new-room")
    @CrossOrigin(origins = "http://localhost:5174")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);

        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(),
                savedRoom.getRoomPrice(), savedRoom.isBooked(), null, null);

        return ResponseEntity.ok(response);
    }

    // Fetching all room types
    @GetMapping("/room/types")
    @CrossOrigin(origins = "http://localhost:5174")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    // Fetching all rooms
    @GetMapping("/all-rooms")
    @CrossOrigin(origins = "http://localhost:5174")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRoom();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            String base64Photo = null;

            if (photoBytes != null && photoBytes.length > 0) {
                base64Photo = "data:image/jpeg;base64," + Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);  // Correctly setting the base64 photo
                roomResponses.add(roomResponse);
            }
        }

        return ResponseEntity.ok(roomResponses);
    }

    // Deleting a room
    @CrossOrigin(origins = "http://localhost:5174")
    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Updating a room
    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes = (photo != null && !photo.isEmpty()) ? photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = (photoBytes != null && photoBytes.length > 0) ? new SerialBlob(photoBytes) : null;

        Room updatedRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        updatedRoom.setPhoto(photoBlob);  // Set the photo (Blob) to the room

        RoomResponse roomResponse = getRoomResponse(updatedRoom);
        return ResponseEntity.ok(roomResponse);
    }

    // Fetching room by ID
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);

            // If photo exists, convert to base64 and set in response
            if (room.getPhoto() != null) {
                try {
                    byte[] photoBytes = room.getPhoto().getBytes(1, (int) room.getPhoto().length());
                    String base64Photo =  Base64.encodeBase64String(photoBytes);
                    roomResponse.setPhoto(base64Photo);  // Set the base64 encoded photo
                } catch (SQLException e) {
                    throw new PhotoRetrievalException("Error retrieving photo from room.");
                }
            }
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    // Helper method to get RoomResponse
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());

        if (bookings == null) {
            bookings = new ArrayList<>();  // Empty list if null
        }

        List<BookingResponse> bookingsInfo = bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckedInDate(),
                        booking.getCheckedOutDate(),
                        booking.getGuestFullName(),   // Add guestFullName
                        booking.getGuestEmail(),      // Add guestEmail
                        booking.getNumOfAdults(),     // Add numOfAdults
                        booking.getNumOfChildren(),   // Add numOfChildren
                        booking.getTotalNumOfGuest(), // Add totalNumOfGuests
                        booking.getBookingConfirmationCode(), // Add bookingConfirmationCode
                        null // Assuming null for room, as it is handled elsewhere
                ))
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                null, // Assuming null for photo, as it is handled in specific methods
                bookingsInfo
        );
    }

    // Helper method to get bookings by room id
    @CrossOrigin(origins = "http://localhost:5174")
    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }
}
