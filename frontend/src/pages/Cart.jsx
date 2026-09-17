import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { getCart, updateCartItem, removeCartItem } from '../api/cartApi'

export default function Cart() {
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  const load = () => {
    setLoading(true)
    getCart().then((res) => setCart(res.data)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleQuantity = async (itemId, quantity) => {
    if (quantity < 1) return
    try {
      const { data } = await updateCartItem(itemId, quantity)
      setCart(data)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not update quantity')
    }
  }

  const handleRemove = async (itemId) => {
    try {
      const { data } = await removeCartItem(itemId)
      setCart(data)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not remove item')
    }
  }

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-neutral-500">Loading cart...</div>
  }

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Your cart</h1>
          <Link to="/menu" className="text-brand-600 font-medium">Back to menu</Link>
        </div>

        {!cart || cart.items.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center text-neutral-500">
            Your cart is empty. <Link to="/menu" className="text-brand-600 font-medium">Browse the menu</Link>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow divide-y">
            {cart.items.map((item) => (
              <div key={item.id} className="flex items-center justify-between p-4">
                <div>
                  <p className="font-medium text-neutral-900">{item.name}</p>
                  <p className="text-sm text-neutral-500">₹{item.unitPrice} each</p>
                </div>
                <div className="flex items-center gap-3">
                  <button onClick={() => handleQuantity(item.id, item.quantity - 1)}
                    className="w-8 h-8 rounded border border-neutral-300 text-neutral-700">-</button>
                  <span className="w-6 text-center">{item.quantity}</span>
                  <button onClick={() => handleQuantity(item.id, item.quantity + 1)}
                    className="w-8 h-8 rounded border border-neutral-300 text-neutral-700">+</button>
                  <span className="w-20 text-right font-medium">₹{item.lineTotal}</span>
                  <button onClick={() => handleRemove(item.id)} className="text-red-600 text-sm ml-2">Remove</button>
                </div>
              </div>
            ))}
            <div className="p-4 flex items-center justify-between">
              <span className="font-medium text-neutral-900">Subtotal</span>
              <span className="font-semibold text-neutral-900">₹{cart.subtotal}</span>
            </div>
            <div className="p-4">
              <button
                onClick={() => navigate('/checkout')}
                className="w-full bg-brand-600 text-white rounded-md py-2 font-medium hover:bg-brand-700"
              >
                Proceed to checkout
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
