import { useEffect, useState } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { useNavigate, Link } from 'react-router-dom'
import { logout } from '../features/auth/authSlice'
import { fetchRestaurant } from '../api/menuApi'

const RESTAURANT_ID = 1

export default function Home() {
  const { user } = useSelector((state) => state.auth)
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [restaurant, setRestaurant] = useState(null)

  useEffect(() => {
    fetchRestaurant(RESTAURANT_ID).then((res) => setRestaurant(res.data)).catch(() => setRestaurant(null))
  }, [])

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero */}
      <section className="relative h-[70vh] min-h-[420px] w-full overflow-hidden">
        <video
          className="absolute inset-0 h-full w-full object-cover"
          src="/hero-video.mp4"
          poster="/hero-poster.jpg"
          autoPlay
          muted
          loop
          playsInline
        />
        <div className="absolute inset-0 bg-black/50" />

        <div className="relative z-10 flex h-full flex-col items-center justify-center px-4 text-center text-white">
          <h1 className="text-4xl sm:text-5xl font-bold mb-3">
            {restaurant?.name || 'RestaurantHub'}
          </h1>
          <p className="max-w-xl text-neutral-200 mb-2">
            {restaurant?.description || 'Fresh food, delivered fast.'}
          </p>
          {restaurant?.openingHours && (
            <p className="text-sm text-neutral-300 mb-8">{restaurant.openingHours}</p>
          )}
          <div className="flex gap-3">
            <Link to="/menu" className="bg-brand-600 hover:bg-brand-700 text-white px-6 py-3 rounded-md font-medium">
              Order Now
            </Link>
            <Link to="/menu" className="bg-white/90 hover:bg-white text-neutral-900 px-6 py-3 rounded-md font-medium">
              View Menu
            </Link>
          </div>
        </div>
      </section>

      {/* Account panel */}
      <div className="max-w-2xl mx-auto -mt-10 relative z-10 bg-white rounded-lg shadow p-8">
        <p className="text-neutral-600 mb-6">
          Logged in as {user?.name} ({user?.role})
        </p>
        <div className="flex gap-3 flex-wrap">
          <Link to="/menu" className="bg-brand-600 text-white px-4 py-2 rounded-md">
            View menu
          </Link>
          <Link to="/orders" className="bg-neutral-100 text-neutral-900 px-4 py-2 rounded-md">
            Your orders
          </Link>
          <Link to="/events" className="bg-neutral-100 text-neutral-900 px-4 py-2 rounded-md">
            Events
          </Link>
          <button onClick={handleLogout} className="bg-neutral-900 text-white px-4 py-2 rounded-md">
            Log out
          </button>
        </div>
      </div>
    </div>
  )
}
