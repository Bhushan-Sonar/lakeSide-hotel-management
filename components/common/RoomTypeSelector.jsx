import React, { useEffect, useState } from 'react'
import { getRoomTypes } from '../utils/ApiFunctions'


const RoomTypeSelector = ({ handleRoomInputChange, newRoom }) => {

  const [roomTypes, setRoomTypes] = useState([]) 
  const [showNewRoomTypeInput, setShowNewRoomTypesInput] = useState(false)
  const [newRoomType, setNewRoomType] = useState("")

  useEffect(() => { 
    getRoomTypes().then((data) => {
      setRoomTypes(data || []) 
    })
  }, [])

  const handleNewRoomTypeInputChange = (e) => {
    setNewRoomType(e.target.value)
  }

  const handleAddNewRoomType = () => {
    if (newRoomType.trim() !== "") {
      setRoomTypes((prevRoomTypes) => [...prevRoomTypes, newRoomType]) 
      setNewRoomType("") 
      setShowNewRoomTypesInput(false) 
    }
  }

  return (
    <>
      <div className="mb-3">
        <select
          id="roomType"
          name="roomType"
          value={newRoom.roomType || ""}
          onChange={(e) => {
            if (e.target.value === "Add New") {
              setShowNewRoomTypesInput(true)
            } else {
              handleRoomInputChange(e)
            }
          }}
          className="form-control" 
        >
          <option value={""}>Select a room type</option>
          <option value={"Add New"}>Add New</option>
          {roomTypes.length > 0 && roomTypes.map((type, index) => (
            <option key={index} value={type}>{type}</option>
          ))}
        </select>

        {showNewRoomTypeInput && (
          <div className="input-group mt-2">
            <input
              className="form-control"
              type="text"
              placeholder="Enter a new room type"
              onChange={handleNewRoomTypeInputChange}
              value={newRoomType}
            />
            <button className="btn btn-primary" type="button" onClick={handleAddNewRoomType}>
              Add
            </button>
          </div>
        )}
      </div>
    </>
  )
}

export default RoomTypeSelector
