package com.dailycodework.lakeSidehotel.service;


import com.dailycodework.lakeSidehotel.exception.InvalidBookingRequestException;
import com.dailycodework.lakeSidehotel.model.BookedRoom;
import com.dailycodework.lakeSidehotel.model.Room;
import com.dailycodework.lakeSidehotel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;


    @Autowired
    public BookingService(BookingRepository bookingRepository, IRoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
    }





    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);

    }

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if(bookingRequest.getCheckedOutDate().isBefore(bookingRequest.getCheckedInDate())){
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");

        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingbookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingbookings );
        if(roomIsAvailable){
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        }else {
            throw  new InvalidBookingRequestException(" Sorry, This room is available for the selected dates;");
        }

        return bookingRequest.getBookingConfirmationCode();
    }



    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode);
    }




    public List<BookedRoom> getAllBookingsByRoomId(Long roomId){
        return bookingRepository.findByRoomId(roomId);
    }



    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckedInDate().equals(existingBooking.getCheckedInDate()) ||
                                bookingRequest.getCheckedOutDate().isBefore(existingBooking.getCheckedInDate()) ||
                                (bookingRequest.getCheckedInDate().isAfter(existingBooking.getCheckedInDate()) &&
                                        bookingRequest.getCheckedInDate().isBefore(existingBooking.getCheckedOutDate())) ||
                                (bookingRequest.getCheckedOutDate().isAfter(existingBooking.getCheckedInDate()) &&
                                        bookingRequest.getCheckedOutDate().isBefore(existingBooking.getCheckedOutDate())) ||
                                (bookingRequest.getCheckedInDate().isBefore(existingBooking.getCheckedInDate()) &&
                                        bookingRequest.getCheckedOutDate().isAfter(existingBooking.getCheckedOutDate())) ||
                                (bookingRequest.getCheckedInDate().equals(existingBooking.getCheckedOutDate()) &&
                                        bookingRequest.getCheckedOutDate().isBefore(existingBooking.getCheckedInDate())) ||
                                (bookingRequest.getCheckedInDate().equals(existingBooking.getCheckedOutDate()) &&
                                        bookingRequest.getCheckedOutDate().isBefore(existingBooking.getCheckedInDate()))
                );
    }

}
