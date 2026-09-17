import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useSelector } from 'react-redux'
import toast from 'react-hot-toast'
import { getAddresses, createAddress, createOrder } from '../api/orderApi'
import { getCart } from '../api/cartApi'
import { createPaymentOrder, verifyPayment } from '../api/paymentApi'
import { validateCoupon } from '../api/couponApi'

export default function Checkout() {
  const [cart, setCart] = useState(null)
  const [addresses, setAddresses] = useState([])
  const [selectedAddressId, setSelectedAddressId] = useState(null)
  const [showNewAddress, setShowNewAddress] = useState(false)
  const [newAddress, setNewAddress] = useState({ label: '', line1: '', line2: '', city: '', state: '', postalCode: '', phone: '' })
  const [placing, setPlacing] = useState(false)
  const [couponCode, setCouponCode] = useState('')
  const [appliedCoupon, setAppliedCoupon] = useState(null)
  const navigate = useNavigate()
  const { user } = useSelector((state) => state.auth)

  const loadAddresses = () => {
    getAddresses().then((res) => {
      setAddresses(res.data)
      if (res.data.length > 0) setSelectedAddressId(res.data[0].id)
      else setShowNewAddress(true)
    })
  }

  useEffect(() => {
    getCart().then((res) => setCart(res.data))
    loadAddresses()
  }, [])

  const handleAddressChange = (e) => setNewAddress({ ...newAddress, [e.target.name]: e.target.value })

  const handleSaveAddress = async (e) => {
    e.preventDefault()
    try {
      const { data } = await createAddress(newAddress)
      toast.success('Address saved')
      setShowNewAddress(false)
      loadAddresses()
      setSelectedAddressId(data.id)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not save address')
    }
  }

  const handleApplyCoupon = async () => {
    if (!couponCode.trim() || !cart) return
    try {
      const { data } = await validateCoupon(couponCode.trim().toUpperCase(), cart.restaurantId, cart.subtotal)
      setAppliedCoupon(data)
      toast.success(`Coupon applied: -₹${data.discountAmount}`)
    } catch (err) {
      setAppliedCoupon(null)
      toast.error(err.response?.data?.message || 'Invalid coupon')
    }
  }

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      toast.error('Add a delivery address first')
      return
    }
    setPlacing(true)
    try {
      const { data: order } = await createOrder(selectedAddressId, appliedCoupon?.code)
      const { data: paymentOrder } = await createPaymentOrder(order.id)

      const options = {
        key: paymentOrder.razorpayKeyId,
        amount: paymentOrder.amountInPaise,
        currency: paymentOrder.currency,
        name: 'RestaurantHub',
        description: `Order #${order.id}`,
        order_id: paymentOrder.razorpayOrderId,
        prefill: { name: user?.name, email: user?.email },
        handler: async (response) => {
          try {
            await verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            })
            toast.success('Payment successful, order confirmed')
            navigate(`/orders/${order.id}`)
          } catch (err) {
            toast.error(err.response?.data?.message || 'Payment verification failed')
          }
        },
        modal: {
          ondismiss: () => {
            toast('Payment cancelled. You can retry from your order history.', { icon: 'ℹ️' })
            navigate(`/orders/${order.id}`)
          }
        },
        theme: { color: '#ea580c' }
      }

      if (!window.Razorpay) {
        toast.error('Payment could not load. Check your internet connection and try again.')
        return
      }

      const razorpay = new window.Razorpay(options)
      razorpay.open()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not place order')
    } finally {
      setPlacing(false)
    }
  }

  if (!cart) {
    return <div className="min-h-screen flex items-center justify-center text-neutral-500">Loading...</div>
  }

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Checkout</h1>
          <Link to="/cart" className="text-brand-600 font-medium">Back to cart</Link>
        </div>

        <div className="bg-white rounded-lg shadow p-6 mb-4">
          <h2 className="font-medium text-neutral-900 mb-3">Delivery address</h2>

          {addresses.map((addr) => (
            <label key={addr.id} className="flex items-start gap-3 mb-3 cursor-pointer">
              <input
                type="radio"
                name="address"
                checked={selectedAddressId === addr.id}
                onChange={() => setSelectedAddressId(addr.id)}
                className="mt-1"
              />
              <span className="text-sm text-neutral-700">
                {addr.label ? <strong>{addr.label}: </strong> : null}
                {addr.line1}, {addr.city} {addr.postalCode}
              </span>
            </label>
          ))}

          {!showNewAddress ? (
            <button onClick={() => setShowNewAddress(true)} className="text-brand-600 text-sm font-medium">
              + Add a new address
            </button>
          ) : (
            <form onSubmit={handleSaveAddress} className="mt-3 space-y-2">
              <input name="label" placeholder="Label (Home, Work)" value={newAddress.label} onChange={handleAddressChange}
                className="w-full border border-neutral-300 rounded-md px-3 py-2 text-sm" />
              <input name="line1" placeholder="Address line 1" required value={newAddress.line1} onChange={handleAddressChange}
                className="w-full border border-neutral-300 rounded-md px-3 py-2 text-sm" />
              <input name="line2" placeholder="Address line 2 (optional)" value={newAddress.line2} onChange={handleAddressChange}
                className="w-full border border-neutral-300 rounded-md px-3 py-2 text-sm" />
              <div className="grid grid-cols-2 gap-2">
                <input name="city" placeholder="City" required value={newAddress.city} onChange={handleAddressChange}
                  className="border border-neutral-300 rounded-md px-3 py-2 text-sm" />
                <input name="postalCode" placeholder="Postal code" value={newAddress.postalCode} onChange={handleAddressChange}
                  className="border border-neutral-300 rounded-md px-3 py-2 text-sm" />
              </div>
              <input name="phone" placeholder="Contact phone" required value={newAddress.phone} onChange={handleAddressChange}
                className="w-full border border-neutral-300 rounded-md px-3 py-2 text-sm" />
              <button type="submit" className="bg-neutral-900 text-white rounded-md px-4 py-2 text-sm">
                Save address
              </button>
            </form>
          )}
        </div>

        <div className="bg-white rounded-lg shadow p-6 mb-4">
          <h2 className="font-medium text-neutral-900 mb-3">Order summary</h2>
          {cart.items.map((item) => (
            <div key={item.id} className="flex justify-between text-sm text-neutral-700 mb-1">
              <span>{item.name} x{item.quantity}</span>
              <span>₹{item.lineTotal}</span>
            </div>
          ))}
          <div className="flex justify-between font-medium text-neutral-900 mt-3 pt-3 border-t">
            <span>Subtotal</span>
            <span>₹{cart.subtotal}</span>
          </div>
          {appliedCoupon && (
            <div className="flex justify-between text-sm text-green-700 mt-1">
              <span>Coupon ({appliedCoupon.code})</span>
              <span>-₹{appliedCoupon.discountAmount}</span>
            </div>
          )}
          <p className="text-xs text-neutral-500 mt-1">Tax and delivery fee are calculated on the server and shown on your order confirmation.</p>
        </div>

        <div className="bg-white rounded-lg shadow p-6 mb-4">
          <h2 className="font-medium text-neutral-900 mb-3">Have a coupon?</h2>
          <div className="flex gap-2">
            <input
              value={couponCode}
              onChange={(e) => setCouponCode(e.target.value)}
              placeholder="Enter coupon code"
              className="flex-1 border border-neutral-300 rounded-md px-3 py-2 text-sm"
            />
            <button onClick={handleApplyCoupon} className="bg-neutral-900 text-white px-4 py-2 rounded-md text-sm">
              Apply
            </button>
          </div>
        </div>

        <button
          onClick={handlePlaceOrder}
          disabled={placing}
          className="w-full bg-brand-600 text-white rounded-md py-3 font-medium hover:bg-brand-700 disabled:opacity-60"
        >
          {placing ? 'Placing order...' : 'Place order and pay'}
        </button>
      </div>
    </div>
  )
}
