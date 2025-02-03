import axios from "axios"

export const api = axios.create({
    baseURL: "http://localhost:9192"
})

/* This Function adds a new room to the database */
export async function addRoom(photo, roomType, roomPrice) {

    const formData = new FormData()
    formData.append("photo", photo)
    formData.append("roomType", roomType)
    formData.append("roomPrice", roomPrice)
    
    const response = await api.post("/rooms/add/new-room", formData)
    if (response.status === 201) {
        return true
    } else {
        return false
    }
}

/* This Function gets all room types from the database */
export async function getRoomTypes() {
    try {
        // Corrected the URL here
        const response = await api.get("/rooms/types")  // Corrected URL
        return response.data
    } catch (error) {
        throw new Error("Error fetching room types")
    }
}

/* This function gets all rooms from the database */
export async function getAllRooms() {
    try {
        const result = await api.get("/rooms/all-rooms")
        return result.data
    } catch (error) {
        throw new Error("Error fetching rooms")
    }
}


/* This function delete a room by Id*/

export async function deleteRoom(roomId){
    try {
        const result = await api.delete(`/rooms/delete/room/${roomId}`)
        return result.data
        
    } catch (error) {
        throw new Error(`Error Deleting room ${error.message}`)
        
    }
}
/* This function update a room*/

export async function updateRoom(roomId, roomData){
    const formData = new FormData()
    formData.append("roomType", roomData.roomType)
    formData.append("roomPrice", roomData.roomPrice)
    formData.append("photo", roomData.photo)
    const response = await api.put(`/rooms/update/${roomId}`, formData)
    return response

}

/* This function get a room by the id*/
export async function  getRoomById(roomId){
    try {
        const result = await api.get(`/rooms/room/${roomId}`)
        return result.data
    } catch (error) {
        throw new Error(`Error fetching room${error.message}`)
        
    }


}

/* This function save a new booking to he database*/

export async function bookRoom(roomId, booking){
    try {
        const response = await api.post(`/bookings/room/${roomId}/booking`, booking);
        return response.data;
    } catch (error) {
        if(error.response && error.response.data){
            throw new Error(error.response.data);
        } else {
            throw new Error(`Error booking room: ${error.message}`);
        }
    }
}

/* This Function get all bookings fron the database*/
export async function getAllBookings(){
    try{
        const result = await api.get("/bookings/all-bookings")
        return result.data
    }catch(error){
        throw new Error(`Error fetchingbookings: ${error.message}`)

    }
}

/* This function get booking by the confirmation code*/
export async function getBookingByConfirmationCode(confirmationCode){
    try{

        const result = await api.get(`/bookings/confirmation/${confirmationCode}`) 
        return result.data
    }catch(error){
        if(error.response && error.response.data){
            throw new Error(error.response.data)
    }else{
        throw new Error(`Error finding booking : ${error.message}`)
    }
}
}


/* This function can cancel booking*/
export async function cancelBooking(bookingId){
    try {
        const result = await api.delete(`/bookings/booking/${bookingId}/delete`)
        return result.data
        
    } catch (error) {
        throw new Error(`Error cancelling booking :${error.message}`)
        
    }
}