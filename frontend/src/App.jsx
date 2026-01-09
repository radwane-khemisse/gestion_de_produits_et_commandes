import React from 'react'
import { Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import Layout from './components/Layout.jsx'
import Loading from './components/Loading.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Products from './pages/Products.jsx'
import Commands from './pages/Commands.jsx'

function LoginScreen() {
  const { login } = useAuth()

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="badge">secured access</div>
        <h1 className="login-title">Gestion Produits & Commandes</h1>
        <p className="muted">
          Authenticate with Keycloak to manage products, commands, and real-time
          inventory controls through the API Gateway.
        </p>
        <div className="actions">
          <button className="btn" type="button" onClick={login}>
            Login with Keycloak
          </button>
        </div>
        <div className="helper">
          Use ADMIN for catalog management, CLIENT for command creation.
        </div>
      </div>
    </div>
  )
}

export default function App() {
  const { initialized, authenticated } = useAuth()

  if (!initialized) {
    return <Loading />
  }

  if (!authenticated) {
    return <LoginScreen />
  }

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="/products" element={<Products />} />
        <Route path="/commands" element={<Commands />} />
      </Route>
    </Routes>
  )
}
