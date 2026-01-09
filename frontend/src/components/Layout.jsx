import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

export default function Layout() {
  const { profile, logout, username } = useAuth()

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">Atlas Control</div>
        <nav className="nav-list">
          <NavLink to="/" className="nav-link">
            Dashboard
          </NavLink>
          <NavLink to="/products" className="nav-link">
            Products
          </NavLink>
          <NavLink to="/commands" className="nav-link">
            Commands
          </NavLink>
        </nav>

      </aside>

      <div className="app-main">
        <div className="topbar">
          <div>
            <div className="tag">Atlas Market</div>
            <h1 className="page-title">Atlas Market</h1>
          </div>
          <div className="actions">
            <span className="chip">{profile?.email || username}</span>
            <button className="btn secondary" type="button" onClick={logout}>
              Logout
            </button>
          </div>
        </div>
        <Outlet />
      </div>
    </div>
  )
}
