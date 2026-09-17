import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { fetchEvents, purchaseTicket } from '../api/eventApi'

const RESTAURANT_ID = 1

export default function Events() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    fetchEvents(RESTAURANT_ID).then((res) => setEvents(res.data.content)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleBuy = async (eventId) => {
    try {
      await purchaseTicket(eventId, 1)
      toast.success('Ticket booked')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not book ticket')
    }
  }

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Upcoming events</h1>
          <Link to="/" className="text-brand-600 font-medium">Home</Link>
        </div>

        {loading ? (
          <p className="text-neutral-500">Loading...</p>
        ) : events.length === 0 ? (
          <p className="text-neutral-500">No events scheduled right now.</p>
        ) : (
          <div className="space-y-4">
            {events.map((event) => (
              <div key={event.id} className="bg-white rounded-lg shadow p-6 flex justify-between items-start">
                <div>
                  <h3 className="font-medium text-neutral-900">{event.name}</h3>
                  <p className="text-sm text-neutral-500">{event.eventDate} · {event.location}</p>
                  <p className="text-sm text-neutral-600 mt-1">{event.description}</p>
                  <p className="text-xs text-neutral-400 mt-2">{event.remainingSeats} of {event.totalCapacity} seats left</p>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-neutral-900 mb-2">₹{event.ticketPrice}</p>
                  <button
                    onClick={() => handleBuy(event.id)}
                    disabled={event.remainingSeats === 0}
                    className="bg-brand-600 text-white px-4 py-2 rounded-md text-sm disabled:opacity-50"
                  >
                    {event.remainingSeats === 0 ? 'Sold out' : 'Book ticket'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
