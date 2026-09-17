import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import toast from 'react-hot-toast'
import { loginUser } from '../api/authApi'
import { setCredentials } from '../features/auth/authSlice'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await loginUser(form)
      dispatch(setCredentials(data))
      toast.success(`Welcome back, ${data.name}`)
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50 px-4">
      <form onSubmit={handleSubmit} className="w-full max-w-sm bg-white p-8 rounded-lg shadow">
        <h1 className="text-2xl font-semibold mb-6 text-neutral-900">Log in</h1>

        <label className="block text-sm font-medium text-neutral-700 mb-1">Email</label>
        <input type="email" name="email" value={form.email} onChange={handleChange} required
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <label className="block text-sm font-medium text-neutral-700 mb-1">Password</label>
        <input type="password" name="password" value={form.password} onChange={handleChange} required
          className="w-full border border-neutral-300 rounded-md px-3 py-2 mb-6 focus:outline-none focus:ring-2 focus:ring-brand-500" />

        <button type="submit" disabled={loading}
          className="w-full bg-brand-600 text-white rounded-md py-2 font-medium hover:bg-brand-700 disabled:opacity-60">
          {loading ? 'Logging in...' : 'Log in'}
        </button>

        <p className="text-sm text-neutral-600 mt-4 text-center">
          New here? <Link to="/register" className="text-brand-600 font-medium">Create an account</Link>
        </p>
      </form>
    </div>
  )
}
