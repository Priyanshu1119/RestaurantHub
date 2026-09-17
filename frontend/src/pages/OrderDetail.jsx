import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getOrder } from '../api/orderApi'

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED']

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)

  useEffect(() => {
    getOrder(id).then((res) => setOrder(res.data))
  }, [id])

  if (!order) {
    return <div className="min-h-screen flex items-center justify-center text-neutral-500">Loading order...</div>
  }

  const currentStepIndex = STATUS_STEPS.indexOf(order.status)
  const isCancelled = order.status === 'CANCELLED'

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Order #{order.id}</h1>
          <Link to="/orders" className="text-brand-600 font-medium">All orders</Link>
        </div>

        <div className="bg-white rounded-lg shadow p-6 mb-4">
          {isCancelled ? (
            <p className="text-red-600 font-medium">This order was cancelled.</p>
          ) : (
            <div className="flex justify-between text-xs text-neutral-500">
              {STATUS_STEPS.map((step, i) => (
                <div key={step} className={`flex-1 text-center ${i <= currentStepIndex ? 'text-brand-600 font-medium' : ''}`}>
                  {step.replace(/_/g, ' ')}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow p-6 mb-4">
          <h2 className="font-medium text-neutral-900 mb-3">Items</h2>
          {order.items.map((item) => (
            <div key={item.id} className="flex justify-between text-sm text-neutral-700 mb-1">
              <span>{item.itemName} x{item.quantity}</span>
              <span>₹{item.lineTotal}</span>
            </div>
          ))}
          <div className="border-t mt-3 pt-3 space-y-1 text-sm">
            <div className="flex justify-between"><span>Subtotal</span><span>₹{order.subtotal}</span></div>
            <div className="flex justify-between"><span>Tax</span><span>₹{order.taxAmount}</span></div>
            <div className="flex justify-between"><span>Delivery fee</span><span>₹{order.deliveryFee}</span></div>
            <div className="flex justify-between font-semibold text-neutral-900"><span>Total</span><span>₹{order.totalAmount}</span></div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="font-medium text-neutral-900 mb-2">Delivery to</h2>
          <p className="text-sm text-neutral-700">{order.deliveryAddress}</p>
          <p className="text-sm text-neutral-500">{order.contactPhone}</p>
        </div>
      </div>
    </div>
  )
}
