import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyOrders } from '../api/orderApi'

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getMyOrders().then((res) => setOrders(res.data.content)).finally(() => setLoading(false))
  }, [])

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Your orders</h1>
          <Link to="/menu" className="text-brand-600 font-medium">Order again</Link>
        </div>

        {loading ? (
          <p className="text-neutral-500">Loading...</p>
        ) : orders.length === 0 ? (
          <p className="text-neutral-500">No orders yet.</p>
        ) : (
          <div className="bg-white rounded-lg shadow divide-y">
            {orders.map((order) => (
              <Link key={order.id} to={`/orders/${order.id}`} className="flex justify-between p-4 hover:bg-neutral-50">
                <div>
                  <p className="font-medium text-neutral-900">Order #{order.id}</p>
                  <p className="text-sm text-neutral-500">{order.restaurantName}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium text-neutral-900">{order.status.replace(/_/g, ' ')}</p>
                  <p className="text-sm text-neutral-500">₹{order.totalAmount}</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
