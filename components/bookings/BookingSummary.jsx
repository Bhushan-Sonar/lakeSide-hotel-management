import moment from "moment";
import React, { useEffect, useState } from 'react';
import { Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // Import axios to make HTTP requests

const BookingSummary = ({ booking, payment, isFormValid, onConfirm }) => {
    const checkInDate = moment(booking.checkInDate);
    const checkOutDate = moment(booking.checkOutDate);
    const numberOfDays = checkOutDate.diff(checkInDate, "days");
    const [isBookingConfirmed, setIsBookingConfirmed] = useState(false);
    const [isProcessingPayment, setIsProcessingPayment] = useState(false);
    const [paymentError, setPaymentError] = useState(null);  // New state to handle errors
    const [apiError, setApiError] = useState(null);  // New state for API errors
    const navigate = useNavigate();

    // Validate the check-in/check-out dates and show the error if needed
    useEffect(() => {
        if (!checkOutDate.isAfter(checkInDate)) {
            setPaymentError("Check-Out date must be after Check-In date.");
        } else {
            setPaymentError(null);
        }
    }, [checkInDate, checkOutDate]);

    const handleConfirmBooking = () => {
        if (paymentError) return; // Prevent confirm if there's a date error

        setIsProcessingPayment(true);
        setTimeout(() => {
            setIsProcessingPayment(false);
            setIsBookingConfirmed(true);
            onConfirm();  // Calls the onConfirm function passed down from BookingForm
        }, 3000);
    };

    useEffect(() => {
        if (isBookingConfirmed) {
            // Make API call to update the booking in the backend
            axios.post(`http://localhost:9192/update-booking`, {
                bookingId: booking.id,
                status: 'confirmed',
                guestFullName: booking.guestFullName,
                guestEmail: booking.guestEmail,
                checkInDate: booking.checkInDate,
                checkOutDate: booking.checkOutDate,
                numberOfAdults: booking.numberOfAdults,
                numberOfChildren: booking.numberOfChildren,
            })
            .then((response) => {
                console.log('Booking updated successfully:', response);
                // Navigate to the success page with a success message
                navigate("/booking-success", { state: { message: "Booking Confirmed!" } });
            })
            .catch((error) => {
                console.error('Error updating booking:', error);
                setApiError('There was an issue updating your booking. Please try again.');
            });
        }
    }, [isBookingConfirmed, booking, navigate]);

    return (
        <div className="card card-body mt-5">
            <h4>Reservation Summary</h4>
            <p>Full Name: <strong>{booking.guestFullName}</strong></p>
            <p>Email: <strong>{booking.guestEmail}</strong></p>
            <p>Check-In Date: <strong>{moment(booking.checkInDate).format("MMM Do YYYY")}</strong></p>
            <p>Check-Out Date: <strong>{moment(booking.checkOutDate).format("MMM Do YYYY")}</strong></p>
            <p>Number of Days: <strong>{numberOfDays}</strong></p>
            <div>
                <h5>Number of Guests</h5>
                <strong>
                    Adult{booking.numberOfAdults > 1 ? 's' : ''}: {booking.numberOfAdults}
                </strong>
                <strong> Children: {booking.numberOfChildren}</strong>
            </div>

            {/* Check for payment and render buttons */}
            {paymentError ? (
                <p className="text-danger">{paymentError}</p>
            ) : (
                <>
                    {payment() > 0 ? (
                        <>
                            <p>Total Payment: <strong>${payment()}</strong></p>
                            {isFormValid && !isBookingConfirmed ? (
                                <Button variant="success" onClick={handleConfirmBooking}>
                                    {isProcessingPayment ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
                                            Booking Confirmed, redirecting to payment...
                                        </>
                                    ) : (
                                        "Confirm Booking and Proceed to Payment"
                                    )}
                                </Button>
                            ) : isBookingConfirmed ? (
                                <div className="d-flex justify-content-center align-items-center">
                                    <div className="spinner-border text-primary" role="status">
                                        <span className="sr-only">Loading...</span>
                                    </div>
                                </div>
                            ) : null}
                        </>
                    ) : (
                        <p className="text-danger">Please ensure that the payment amount is valid.</p>
                    )}
                </>
            )}

            {/* Show API error if there's an issue updating the booking */}
            {apiError && <p className="text-danger">{apiError}</p>}
        </div>
    );
};

export default BookingSummary;
