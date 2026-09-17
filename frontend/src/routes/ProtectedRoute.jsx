import { useSelector } from 'react-redux'
import { Navigate, Outlet } from 'react-router-dom'

export default function ProtectedRoute({ allowedRoles }) {
  const { token, user } = useSelector((state) => state.auth)

  if (!token) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
