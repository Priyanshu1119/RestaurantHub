import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { fetchMenu, fetchCategories } from '../api/menuApi'
import { addToCart } from '../api/cartApi'

// Hardcoded until restaurant selection/routing exists (Phase 4+).
const RESTAURANT_ID = 1

function DishImage({ item }) {
  if (item.imageUrl) {
    return (
      <img
        src={item.imageUrl}
        alt={item.name}
        className="w-full h-40 object-cover rounded-md mb-3"
        onError={(e) => { e.target.style.display = 'none' }}
      />
    )
  }
  // No image uploaded yet for this item: show a plain placeholder with the
  // dish's initial instead of a broken image icon.
  return (
    <div className="w-full h-40 rounded-md mb-3 bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center">
      <span className="text-white text-3xl font-semibold opacity-90">
        {item.name?.charAt(0).toUpperCase()}
      </span>
    </div>
  )
}

export default function Menu() {
  const [categories, setCategories] = useState([])
  const [items, setItems] = useState([])
  const [categoryId, setCategoryId] = useState(null)
  const [veg, setVeg] = useState(null)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchCategories(RESTAURANT_ID).then((res) => setCategories(res.data)).catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    setLoading(true)
    fetchMenu({ restaurantId: RESTAURANT_ID, categoryId, veg, search: search || undefined, size: 20 })
      .then((res) => setItems(res.data.content))
      .catch(() => setItems([]))
      .finally(() => setLoading(false))
  }, [categoryId, veg, search])

  const handleAdd = async (itemId) => {
    try {
      await addToCart(itemId, 1)
      toast.success('Added to cart')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not add to cart')
    }
  }

  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8">
      <div className="max-w-5xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-neutral-900">Menu</h1>
          <div className="flex gap-4">
            <Link to="/cart" className="text-brand-600 font-medium">Cart</Link>
            <Link to="/" className="text-brand-600 font-medium">Home</Link>
          </div>
        </div>

        <div className="flex flex-wrap gap-3 mb-6">
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search dishes"
            className="border border-neutral-300 rounded-md px-3 py-2 flex-1 min-w-[200px]"
          />
          <select
            value={categoryId ?? ''}
            onChange={(e) => setCategoryId(e.target.value || null)}
            className="border border-neutral-300 rounded-md px-3 py-2"
          >
            <option value="">All categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          <select
            value={veg === null ? '' : String(veg)}
            onChange={(e) => setVeg(e.target.value === '' ? null : e.target.value === 'true')}
            className="border border-neutral-300 rounded-md px-3 py-2"
          >
            <option value="">Veg and non-veg</option>
            <option value="true">Vegetarian only</option>
            <option value="false">Non-vegetarian only</option>
          </select>
        </div>

        {loading ? (
          <p className="text-neutral-500">Loading menu...</p>
        ) : items.length === 0 ? (
          <p className="text-neutral-500">No dishes match your filters.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {items.map((item) => (
              <div key={item.id} className="bg-white rounded-lg shadow p-4 flex flex-col">
                <DishImage item={item} />
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-medium text-neutral-900">{item.name}</h3>
                  <span className={`text-xs px-2 py-0.5 rounded ${item.vegetarian ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {item.vegetarian ? 'Veg' : 'Non-veg'}
                  </span>
                </div>
                <p className="text-sm text-neutral-500 flex-1 mb-3">{item.description}</p>
                <div className="flex items-baseline gap-2">
                  {item.discountPrice ? (
                    <>
                      <span className="text-lg font-semibold text-neutral-900">₹{item.discountPrice}</span>
                      <span className="text-sm text-neutral-400 line-through">₹{item.price}</span>
                    </>
                  ) : (
                    <span className="text-lg font-semibold text-neutral-900">₹{item.price}</span>
                  )}
                </div>
                <button
                  onClick={() => handleAdd(item.id)}
                  className="mt-3 bg-brand-600 text-white rounded-md py-2 text-sm font-medium hover:bg-brand-700"
                >
                  Add to cart
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
