import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import toast from 'react-hot-toast'
import { registerUser } from '../api/authApi'
import { setCredentials } from '../features/auth/authSlice'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [loading, setLoading] = useState(false)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await registerUser(form)
      dispatch(setCredentials(data))
      toast.success('Account created')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50 px-4">
      <form onSubmit={handleSubmit} className="w-full max-w-sm bg-white p-8 rounded-lg shadow">
        <h1 className="text-2xl font-semibold mb-6 text-neutral-900">Create your account</h1>

        <label className="block text-sm font-medium text-neutral-700 mb-1">Name</label>
        <input name="name" value={form.name} onChange={handleChange} required
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <label className="block text-sm font-medium text-neutral-700 mb-1">Email</label>
        <input type="email" name="email" value={form.email} onChange={handleChange} required
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <label className="block text-sm font-medium text-neutral-700 mb-1">Phone</label>
        <input name="phone" value={form.phone} onChange={handleChange}
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <label className="block text-sm font-medium text-neutral-700 mb-1">Password</label>
        <input type="password" name="password" value={form.password} onChange={handleChange} required minLength={8}
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-6 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <button type="submit" disabled={loading}
          className="w-full bg-brand-600 text-white rounded-md py-2 font-medium hover:bg-brand-700 disabled:opacity-60">
          {loading ? 'Creating account...' : 'Create account'}
        </button>

        <p className="text-sm text-neutral-600 mt-4 text-center">
          Already have an account? <Link to="/login" className="text-brand-600 font-medium">Log in</Link>
        </p>
      </form>
    </div>
  )
}
