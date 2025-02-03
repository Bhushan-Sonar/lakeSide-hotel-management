package com.dailycodework.lakeSidehotel.controller;

import com.dailycodework.lakeSidehotel.exception.InvalidBookingRequestException;
import com.dailycodework.lakeSidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakeSidehotel.model.BookedRoom;
import com.dailycodework.lakeSidehotel.model.Room;
import com.dailycodework.lakeSidehotel.response.BookingResponse;
import com.dailycodework.lakeSidehotel.response.RoomResponse;
import com.dailycodework.lakeSidehotel.service.IBookingService;
import com.dailycodework.lakeSidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final IBookingService bookingService;
    private final IRoomService roomService;

    // Manual Constructor Injection
    @Autowired
    public BookingController(IBookingService bookingService, IRoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }


    @GetMapping("all-bookings")
    @CrossOrigin(origins = "http://localhost:5174")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom room : bookings) {
            BookingResponse bookingResponse = getBookingsResponse(room);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);

    }




    @CrossOrigin(origins = "http://localhost:5174")
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable  String confirmationCode) {
        try {
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingsResponse(booking);
            return ResponseEntity.ok(bookingResponse);


        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

         }


    @CrossOrigin(origins = "http://localhost:5174")
    @PostMapping ("/room/{roomId}/booking")
         public ResponseEntity<?> saveBooking(@PathVariable Long roomId,
                                         @RequestBody BookedRoom bookingRequest){
        try{
            String confirmationCode =  bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(
                    "Room  booked successfully, Your booking confirmation code is : "+confirmationCode);


    } catch (InvalidBookingRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @CrossOrigin(origins = "http://localhost:5174")
    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);

    }

    private BookingResponse getBookingsResponse(BookedRoom booking) {
        Room theRoom =roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());

        return new BookingResponse(
                booking.getBookingId(),
                booking.getCheckedInDate(),
                booking.getCheckedOutDate(),
                booking.getGuestFullName(),
                booking.getGuestEmail(),
                booking.getNumOfAdults(),
                booking.getNumOfChildren(),
                booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(),
                room
        );


    }

}









